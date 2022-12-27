# OData v4 for JDBC

Using this library is simple: Enable JAX-RS and write a class with the `@Path` annotations extending the `JDBCODataService` or `JDBCODataServiceForSchema` class.
The only custom coding needed is in the first method `getConnection()` which must produce a new JDBC connection on each invocation (usually this means getting a connection from a connection pool, not establishing a new connection with the database).
see [src/test/java/JDBCODataServiceFacade.java](https://github.com/rtdi/JDBCOData/blob/master/src/test/java/JDBCODataServiceFacade.java) for an example.

The aim of this OData v4 implementation is to query any JDBC table or view as OData entity so it can be used for UI applications, in particular [UI5](https://github.com/SAP/openui5). This starts with getting the list of OData entities (=tables) from the database, their structure and supporting different types of queries including client-side pagination.

## OData Endpoints

Note: A OData endpoint has a trailing slash according to the standard.
see [odata-v4.0-part2-url-conventions.html](http://docs.oasis-open.org/odata/odata/v4.0/odata-v4.0-part2-url-conventions.html) for uris
see [odata-v4.0-part3-csdl.html](http://docs.oasis-open.org/odata/odata/v4.0/odata-v4.0-part3-csdl.html) for response formats

In the JUnit tests, the embedded Tomcat used for testing gets a new web application under the application name `""`.

```
tomcat.setPort(8080);
Context context = tomcat.addWebapp("", basedir.toString());
Tomcat.addServlet(context, "odata-servlet",
    new ServletContainer(new ResourceConfig(WebApplication.class)));
```

Do the url of the web application is `localhost:8080/`.


Above JAX-RS `WebApplication.class` is annotated with

```
@ApplicationPath("/api")
public class WebApplication extends ResourceConfig {
```

Thus the URL of all OData endpoints is `localhost:8080/api`.

The [src/test/java/JDBCODataServiceFacade.java](https://github.com/rtdi/JDBCOData/blob/master/src/test/java/JDBCODataServiceFacade.java) class exposes two types of oData endpoints:

- `localhost:8080/api/odata/tables/<schemaname>/<tablename>/` is one endpoint for this exact table name.
- `localhost:8080/api/odata/schemas/<schemaname>/` is one endpoint for the entire schema with all its tables and views

For convenience the [src/test/java/JDBCoDataServiceListFacade.java](https://github.com/rtdi/JDBCOData/blob/master/src/test/java/JDBCoDataServiceListFacade.java) class exposes the list of all tables/views of the schema and contains an url property pointing to the first URL pattern.

- `localhost:8080/api/odata/tables/` is one endpoint for the entire schema with all its tables and views

The oData endpoints are documented in the OpenAPI (aka Swagger) standard and its information available at

- `localhost:8080/api/openapi.json`


## Rest Endpoints

Each OData endpoint provides multiple rest endpoints that are called via OData clients. But as OData is based on rest, they can be called directly as well.
In this example the library is connected with a database and the table `PROD.MYTABLE1` should be used.

`http://localhost:8080/api/odata/tables/PROD/MYTABLE1/` returns the service document. That is a list of all data exposed by this endpoint. Here a single entity set - the table.

`http://localhost:8080/api/odata/tables/PROD/MYTABLE1/$metadata` returns extensive information about the OData service. As this endpoint exposes a single table, it contains the table metadata including all columns. This $metadata document is read by UI5 to know what fields and their data types exist.

`http://localhost:8080/api/odata/tables/PROD/MYTABLE1/RS` returns the results set of a table, the table data itself. This is the main endpoint used to query the data and supports multiple query parameters.

`http://localhost:8080/api/odata/tables/PROD/MYTABLE1/RS(2523)` returns a single record with this primary key value. Requires the table to have a primary key set, else returns an error.



For the `/schemas` endpoint the syntax is the same, with the main difference that the entity set is no longer the constant `RS` but the table name itself.

`http://localhost:8080/api/odata/schemas/PROD/` returns the service document with all tables of the schema.
`http://localhost:8080/api/odata/schemas/PROD/$metadata` returns the list of all tables in the database schema and all columns of each table. So this can be huge!
`http://localhost:8080/api/odata/schemas/PROD/` returns the service document with all tables of the schema.
`http://localhost:8080/api/odata/schemas/PROD/MYTABLE1` returns the data of this table.
`http://localhost:8080/api/odata/schemas/PROD/MYTABLE1(2523)` returns a single record from the table.


This also answers the question why there are two patterns for endpoints, per table and per schema. A typical database I connect to has 100k tables with 100 columns each in average. Such a metadata document would be multiple GB in size. Because this document is read by the UI5 library for each control, performance would be really bad. There is currently no way to make the document smaller, thus providing the two options.

## Motivation

The OData standard truly is the "best way to rest" for database driven applications. It provides rich metadata about the services, different types of services like queries, insert operations, procedure calls and other must haves like pagination.

What makes it relatively powerful as an interaction between frontend and backend also adds complexity in the implementation.

To cope with the complexity, Apache provides the oLingo client and server library. It is written to work with any kind of data storage in mind and thus requires a lot from the implementer. Especially with databases, pretty much nothing is solved.

* How to implement pagination in a consistent way? The first call returns 1000 records, the next call must return the next batch of the previously executed query. Executing the same query again would be a waste of resource and might return different data as the underlying table got more records meanwhile.
* Why implement all from scratch - let's use JDBC as the foundation.
* The oLingo library expects to write individual service endpoints, one per table. With databases you want to expose probably all tables. (There is a trick to enable that in oLingo though)
* Datatype conversions must be implemented manually.

Further more there are two technical limitations in the oLingo library:
1. Does not work with `jakarta` based projects, so none of the recent versions of servlet containers like Tomcat 10 are supported
2. Cannot expose Swagger metadata


## Limitations of the JDBCoData library

- At the moment it is limited to reading. No actions, no functions, no insert, no $batch operations. For these I personally rather use classic rest as OData does not provide any advantages.
- While the library is implemented with enterprise readiness in mind and supports large datasets, it does not aim at Data Integration clients. OData as such is not well suited for high performance, mass data, parallel processing required for data integration.
- No nested data - one table = one entity.
- The $filter, $select, $order is limited to simple cases like selecting a few columns with simple filters. Full support will take some time and its adoption will not be very high. Better to do all complex transformations inside database views.


## Primary Keys

Although the OData standard requires a key for all entities, many operations work without. This library is built to treat keys as optional.

The OData standard assumes that every table does have a key. This is used for navigation between entities, e.g. read one sales order and then expand all line items of the sales order, for accessing a single row of a table and more. If the database table does have a single or a combined primary key that is just fine. But it raises the question what the OData key should be for those tables without a primary key.
One proposal is to add a counter to the result set, first returned record is one etc. But in that case the key value does not identify a row in a stable manner. First the database can return the data in different order, so the key=1 is a different row with every execution. Granted, can be solved by adding an order by clause always. But then the query adds a filter and again the key does not identify a record as such.

A slightly better method is to use the database internal rowid. Not all databases expose that and for those that do, the rowid has different meanings. In Oracle the rowid is relatively stable, in SAP Hana the rowid is changed with each update statement.

For most OData operations the key is actually not used and not required. Hence this library does expose the key metadata for tables that have a primary key only.


## Pagination

Summary:

 - A query with `$skip=0` does execute the query always and stores the entire result set. $top does limit the data returned by OData but is not added to the select statement.
 - A query with `$skip` greater than zero tries to find the query in the resultset cache. If it cannot, the entire query (without the skip/top is executed again. Either way, it returns the rows between $skip and $skip+$top.
 - If `$top` is larger than the `Prefer: maxpagesize` http header, server side paging kicks in. Only maxpagesize many records are returned and the returned data contains a `@odata.nextLink` to fetch the next page.
 - To avoid excessive cache sizes the executed query always has a row limit. This row limit is set by the server - see `JDBCODataService#getSQLResultSetLimit()` and cannot  be exceeded. The client can reduce it further by setting the http header `resultsetlimit` to any number &gt;=1000. The `$top` is not used in the generated SQL select statement as it controls the client side paging.
 
Above caches are only used if the http connection has a session assigned. The caches are stored in the session context! Otherwise this would pose a security concern as a fraudulent user might use the client side paging to access data produced by another's user JDBC connection. Because the cache is stored in the session context, each http session/user has its own cache.

To correlate the first and the subsequent queries via the cache, each query gets a resultsetid assigned when being executed.
If the URL has a `skiptoken`, its format is `resultsetid || pagenumber` and the resultsetid is taken from here. But that applies to cases where client side caching is disabled.
For all other cases, the best option is setting the http header `ContextId` (or `SAP-ContextId`) with a random number/string. Example: If the same UI page is opened twice, each instance gets different values and this identifier points to the correct query.
If such http header is not found, the resultsetid is derived from the request URL such that different queries have different resultsetids (ignoring top/skip). This is how UI5 expects servers to work.


Details:

see [odata-v4.0-part1-protocol.html](http://docs.oasis-open.org/odata/odata/v4.0/odata-v4.0-part1-protocol.html)


A huge problem for Rest in general is that data is requested and returned as a whole. Streaming results are the exception.
The way out is to read the data in pages. Each call returning a page of records, e.g. 100 and the UI can render those. If the user scrolls down, the next 100 rows are requested.
This is called pagination and OData supports it as client and server side paging.

Client side paging means the first call is using `$skip=0&$top=100` and the second call `$skip=100&$top=100`. 
If these requests are individual SQL queries, performance and consistency is an issue. The database would prefer executing the query once and read the data via multiple fetch calls, each returning an array of 100 rows.
How can this be implemented in the server?

 - [ ] Option 1: Use the skip/top literally and execute one SQL per request. This is obviously in the true spirit of a stateless Restful call. But if each statement is treated independently the same query is executed multiple times, each skipping over the `$skip` many records (either in the fetch call or by using the `OFFSET` SQL select option) and potentially returning inconsistent data as the database got new/changed/deleted records between two query executions.
 - [ ] Option 2: Keep the database statement open and when the client asks for the next page, fetch the next $top many records from that statement.
 - [x] Option 3: Read the entire data and cache the resultset.

Option 2 & 3 pose multiple problems:
1. How should the query remain open - where to store the statement or the data between calls?
2. How to associate the two calls? Worst case, the user has the same UI page open twice, thus the same OData rest calls are made but to produce correct data, both must be distinguishable from each other.
3. When to close the statement/evict the data from the cache? A UI page might be open for a long time until the user requests more data. Thus a large number of open connections would accumulate. Or the connections get closed early and the user has only a small time window within more data can be requested.
4. If the http connection between the UI and the OData service has an issue, the same data might be requested again.

For the vast majority of the cases this will not be a problem as the requested data set is small. List of countries. List of items ordered within a sales order. So one point is clear, the statement should be closed immediately after all data has been read, to keep the number of connections against the database low. If then the same data is requested again, the database cursor is not sufficient, the data itself must be cached.
This leads to the conclusion Option 3 is preferred over Option 2.


Server side paging works be requesting all the desired data, e.g. `$top=5000` but use the http header `Prefer: maxpagesize` to limit the number of rows within a single call. Then the server sends back 100 rows plus a `@odata.nextLink` and using this URL the client can request the next 100 rows. The server can add resultsetid identifier to that `nextLink` and thus the problem 2) - which OData query belongs to what query is solved.

Note that a combination of both is used in UI5. If the `maxpagesize` is 1000 and `$top=10000`, then this request will return 1000 rows and a nextLink until there is no more data. But UI5 is using client side paging always and hence the `$top` will be 100 rows, thus server side paging eould never kick in.

The implementation of this OData library does the following to deal with the possible combinations:

1. To identify a statement it goes through the following sequence
   - Is a `$skiptoken` in the URL? If yes, it contains the requestid as it was produced by the previous request itself.
   - Is the http header `SAP-ContextId` set? If yes, this is the requestid.
   - Is the http header `ContextId` set? If yes, this is the requestid.
   - Use the concat of `schema + name + select + filter + order` (its hashCode) as the requestid. Thus in worst case all queries selecting the same data reuse its data.
2. If the `$skip=0` (or not provided) and no `$skiptoken` provided either, this is a fresh query. Hence execute this query and store it under the requestid. This ensures that refreshing the page - executing the same query again - truly does execute the query returning new data. 
3. In all other cases try to find the statement in the session cache and get the data from there. If no statement is found, execute it.


## Object lifecycle

When new data is requested, a second thread is started (see [AsyncResultSet](https://github.com/rtdi/JDBCOData/blob/master/src/main/java/io/rtdi/appcontainer/odata/AsyncResultSet.java)) and its task it to execute the SQL statement and to collect the data. The main program calls the fetch for the first time and this waits up to 60 seconds for the AsyncResultSet to produce the `$top` many records. I then returns this subset and the OData Rest call is completed. 
The reader thread keeps collecting the data though and subsequent OData Restcalls will use the meanwhile collected data.


