# OData Snowflake Service based on Payara Docker deployment

This fork extends the base code with:
- deployment in Docker image with Payara server
- configured basic authentication (secrets are based on environment variables)


# oData v4 for JDBC

Using this library is simple: Enable JAX-RS, write a class with the `@Path` annotations extending the `JDBCoDataService` class.
The only custom coding needed is in the first method `getConnection()` which must produce the JDBC connection.
see [src/test/java/JDBCoDataServiceFacade.java](https://github.com/rtdi/JDBCoData/blob/master/src/test/java/JDBCoDataServiceFacade.java) for an  example.

The aim of this oData v4 implementation is to query any JDBC table or view as oData entity so it can be used for UI applications, in particular [UI5](https://github.com/SAP/openui5). This starts with getting the list of oData entities (=tables) from the database, their structure and supporting different types of queries including client-side pagination.

## Motivation

The oData standard truly is the "best way to rest" for database driven applications. It provides rich metadata about the services, different types of services like queries, insert operations, procedure calls and other must haves like pagination.

What makes it relatively powerful as an interaction between frontend and backend adds complexity in the implementation.

To cope with the complexity, Apache provides the oLingo client and server library. It is written to work with any kind of data storage in mind and thus requires a lot from the implementor. Especially with databases, pretty much nothing is solved.

* How to implement pagination in a consistent way? The first call returns 1000 records, the next call must return the next batch of the previously executed query. Executing the same query again would be a waste of resource and might return different data as the underlying table got more records meanwhile.
* Why implement all from scratch - let's use JDBC as the foundation.
* The oLingo library expects to write individual service endpoints, one per table. With databases you want to expose probably all tables. (There is a trick to enable that in oLingo though)
* Datatype conversions must be implemented manually.

Further more there are two technical limitations in the oLingo library:
1. Does not work with `jakarta` based projects, so none of the recent versions of servlet containers like Tomcat 10 are supported
2. Cannot expose Swagger metadata


## Limitations

- At the moment it is limited to reading. No actions, no functions, no insert, no $batch operations. For these I personally rather use classic rest as oData does not provide any advantages.
- While the library is implemented with enterprise readiness in mind and supports large datasets, it does not aim at Data Integration clients. oData as such is not well suited for high performance, mass data, parallel processing required for data integration.
- No nested data - one table = one entity.
- The $filter, $select, $order is limited to simple cases like selecting a few columns with simple filters. Full support will take some time and its adoption will not be very high. Better to do all complex transformations inside database views.


## Keys

Although the oData standard requires a key for all entites, many operations work without. This library is built to treat keys as optional.


## Pagination

Summary:

 - A query with `$skip=0` does execute the query always and stores the entire result set. $top does limit the data returned by oData but is not added to the select statement.
 - A query with `$skip` greater than zero tries to find the query in the resultset cache. If it cannot, the entire query (without the skip/top is executed again. Either way, it returns the rows between $skip and $skip+$top.
 - If `$top` is larger than the `Prefer: maxpagesize` http header, server side paging kicks in. Only maxpagesize many records are returned and the returned data contains a `@odata.nextLink` to fetch the next page.
 - To avoid excessive cache sizes the executed query always has a row limit. This row limit is set by the server - see `JDBCoDataService#getSQLResultSetLimit()` and cannot  be exceeded. The client can reduce it further by setting the http header `resultsetlimit` to any number &gt;=1000. The `$top` is not used in the generated SQL select statement as it controls the client side paging.
 
Above caches are only used if the http connection has a session assigned. The caches are stored in the session context! Otherwise this would pose a security concern as a fraudulent user might use the client side paging to access data produced by another's user JDBC connection. Because the cache is stored in the session context, each http session/user has its own cache.

In order to correlate the first and the subsequent queries via the cache, each query gets a resultsetid assigned when being executed.
If the URL has a `skiptoken`, its format is `resultsetid || pagenumber` and the resultsetid is taken from here. But that applies to cases where client side caching is disabled.
For all other cases, the best option is setting the http header `ContextId` (or `SAP-ContextId`) with a random number/string. Example: If the same UI page is opened twice, each instance gets different values and this identifier points to the correct query.
If such http header is not found, the resultsetid is derived from the request URL such that different queries have different resultsetids (ignoring top/skip). This is how UI5 expects servers to work.


Details:

A huge problem for Rest in general is that data is requested and returned as a whole. Streaming results are the exception.
The way out is to read the data in pages. Each call returning a page of records, e.g. 100 and the UI can render those. If the user scrolls down, the next 100 rows are requested.
This is called pagination and oData supports it as client and server side paging.

Client side paging means the first call is using `$skip=0&$top=100` and the second call `$skip=100&$top=100`. 
If these requests are individual SQL queries, performance and consistency is an issue. The database would prefer executing the query once and read the data via multiple fetch calls, each returning an array of 100 rows.
How can this be implemented in the server?

 - [ ] Option 1: Use the skip/top literally and execute one SQL per request. This is obviously in the true spirit of a stateless Restful call. But if each statement is treated independently the same query is executed multiple times, each skipping over the `$skip` many records (either in the fetch call or by using the `OFFSET` SQL select option) and potentially returning inconsistent data as the database got new/changed/deleted records between two query executions.
 - [ ] Option 2: Keep the database statement open and when the client asks for the next page, fetch the next $top many records from that statement.
 - [x] Option 3: Read the entire data and cache the resultset.

Option 2 & 3 pose multiple problems:
1. How should the query remain open - where to store the statement or the data between calls?
2. How to associate the two calls? Worst case, the user has the same UI page open twice, thus the same oData rest calls are made but to produce correct data, both must be distinguishable from each other.
3. When to close the statement/evict the data from the cache? A UI page might be open for a long time until the user requests more data. Thus a large number of open connections would accumulate. Or the connections get closed rather early and the user has only a small time window within more data can be requested.
4. If the http connection between the UI and the oData service has an issue, data might be requested again.

For the vast majority of the cases this will not be a problem as the requested data set will be small. List of countries. List of items ordered within a sales order. So one point is clear, the statement should be closed immediately after all data has been read, to keep the number of connections against the database low. If then the same data is requested again, the database cursor is not sufficient, the data itself must be cached.
This leads to the conclusion Option 3 is preferred over Option 2.


Server side paging works be requesting all the desired data, e.g. `$top=5000` but use the http header `Prefer: maxpagesize` to limit the number of rows within a single call. Then the server sends back 100 rows plus a `@odata.nextLink` and using this URL the client can request the next 100 rows. The server can add resultsetid identifier to that `nextLink` and thus the problem 2) - which oData query belongs to what query is solved.

Note that a combination of both is used in UI5. If the `maxpagesize` is 1000 and `$top=10000`, then this request will return 1000 rows and a nextLink until there is no more data. But UI5 is using client side paging always and hence the top will be 100 rows, thus server side paging will never kick in.

The implementation of this oData library therefore does the following:

1. To identify a statement it goes through the following sequence
   - Is a `$skiptoken` in the URL? If yes, it contains the requestid as it was produced by the previous request itself.
   - Is the http header `SAP-ContextId` set? If yes, this is the requestid.
   - Is the http header `ContextId` set? If yes, this is the requestid.
   - Use the concat of `schema + name + select + filter + order` (its hashCode) as the requestid. Thus in worst case all queries selecting the same data reuse its data.
2. If the `$skip=0` (or not provided) and no `$skiptoken` provided either, this is a fresh query. Hence execute this query and store it under the requestid.
3. In all other cases try to find the statement in the session cache and get the data from there. If no statement is found, execute it again.


## Object lifecycle

When new data is requested, a second thread is started (see [AsyncResultSet](https://github.com/rtdi/JDBCoData/blob/master/src/main/java/io/rtdi/appcontainer/odata/AsyncResultSet.java)) and its task it to execute the SQL statement and collecting the data. The main program calls the fetch for the first time and this waits up to 60 seconds for the AsyncResultSet to produce the `$top` many records. I then returns this subset and the oData Rest call is completed. 
The reader thread keeps collecting the data though and subsequent oData Restcalls will use the meanwhile collected data.


