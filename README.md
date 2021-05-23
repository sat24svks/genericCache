Generic Cache V0.1
==================

This cache api supports the following cache services

* add object
* remove object
* get object

Cache can be created using CacheBuilder:
----------------------------------------
```java
GenericCache<String, String> genericCache = new GenericCacheImpl.CacheBuilder(3, (short) 1)
				.cleanUpInterval((short) 1).build();
```
CacheBuilder is used to build the generic cache object, its constructor have 2 params
1 -- Max size of cache
2 -- Global Timeout

cleanupInterval method - It is used to set the optional field cleanUpInterval (default value is 3 seconds).
This value is used to run the clean method to remove expired objects at configured interval