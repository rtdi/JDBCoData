# oData v4 for JDBC

The oData project truly is the "best way to rest" for database driven applications. It provides rich metadata about the services, different types of services like queries, insert operations, procedure calls and other must haves like pagination.

What makes it relatively powerful as an interaction between frontend and backend adds complexity in the implementation.

## Motivation

To cope with that Apache provides the oLingo client and server but frankly, it is very bare knuckles. It is written to work with any kind of data storage in mind and thus rather adds even more complexity for the implementer.

Especially with databases, pretty much nothing is solved.

* How to implement pagination in a consistent way? The first call returns 1000 records, the next call must return the next batch of the previously executed query. Executing the same query again would be a waste of resource and might return different data as the underlying table got more records meanwhile, the sort order of database queries is not guaranteed unless specified.
* Why implement all from scratch - let's use JDBC as the foundation.
* The oLingo library expects to write individual service endpoints, one per table. With databases you want to expose probably all tables. (There is a trick to enable that in oLingo though)
* Datatype conversions.

Further more there are two technical limitations in the oLingo library:
1. Does not work with `jakarta` based projects, so none of the recent versions of servlet containers like tomcat 10
2. Cannot expose Swagger metadata



## Keys

Although oData requires a key, many operations work without. This library is built to treat keys optional.


## Pagination

oData supports client and server side paging.

Client side paging means the first call is using `$skip=0&$top=100` and the second call `$skip=100&$top=100`. 
The problem with that is performance and consistency. From a database point of view the query is started and multiple fetch calls each return an array of 100 rows. But that requires the statement to remain open between calls.
If each statement is treated independently the same query is executed multiple times, skipping over the records (either in the fetch call or by using the `OFFSET` SQL select option) and potentially returning inconsistent data as the database got new/changed/deleted records meanwhile.

Retaining the query open between multiple rest calls poses multiple problems
1. How should the query remain open - where to store the statement between calls?
2. How to associate the two calls? Worst case, the user has the same UI page open twice, thus the same oData rest calls are made but to produce correct data, both must be distinguishable from each other.
3. When to close the statement? A UI page might be open for a long time until the user requests more data. Thus a large number of open connections would accumulate. Or the connections get closed rather early and the user has only a small time window within more data can be requested.


Server side paging works be requesting all the desired data, e.g. `$top=5000` but limit the `maxpagesize` to 100. Then the server sends back 100 rows plus a `nextLink` and using this URL the client can request the next 100 rows. The server can add a statement identifier to that `nextLink` and thus the problem 2) - which oData query belongs to what query is solved. The other two points are open still.

Note that a combination of both can be used in UI5. If the `maxpagesize` is 20 and `$top=100`, then this request will return 20 rows and a nextLink.

The implementation of this oData library therefore does the following:

1. To identify a statement it goes through the following sequence
   - Is a `$skiptoken` in the URL? If yes, it contains the requestid as it was produced by the previous request itself.
   - Is the http header `SAP-ContextId` set? If yes, this is the requestid.
   - Is the http header `ContextId` set? If yes, this is the requestid.
   - Use the concat of `schema + name + select + filter + order` (its hashCode) as the requestid. Thus in worst case all queries selecting the same data reuse its data.
2. If the `$skip=0` (or not provided) and no `$skiptoken` provided either, this is a fresh query. Hence execute this query and store it under the requestid.
3. In all other cases try to find the statement in the session cache and get the data from there. If no statement is found, execute it again.

