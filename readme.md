# Labijie Caching ( kotlin )
>for java see this: https://github.com/endink/caching

### This project is in the process of development....

A cache structure that supports expiration on a per-key basis.


![Hex.pm](https://img.shields.io/hexpm/l/plug.svg)
[![Maven metadata URL](https://img.shields.io/maven-metadata/v/http/central.maven.org/maven2/com/labijie/labijie-caching-kotlin/maven-metadata.xml.svg)](http://central.maven.org/maven2/com/labijie/labijie-caching)

All of the jar packages has been uploaded to the maven central.


## add depenedency in gradle project (coming soon...)

Use memory cahce only:
```groovy
dependencies {
    compile "com.labijie:caching-kotlin:1.0"
}
```

for redis:
```groovy
dependencies {
    compile "com.labijie:caching-kotlin-redis:1.0"
}
```

## Memeory Quick Start
You only need to use the ICacheManager interface, which can be easily integrated into the IOC container, such as spring.
use 

```kotlin
val memoryCache = MemoryCacheManager(MemoryCacheOptions());

//sliding time expires
memoryCache.set("2", Any(), 3000L, TimePolicy.Sliding);

//absolute time expires
memoryCache.set("a", Any(), 1000L, TimePolicy.Absolute);

//get
memoryCache.get("a")

```

## Spring Integration

import package:
```groovy
dependencies {
    compile "com.labijie:caching-kotlin-core-starter:1.0"
}
```

Declare method cache using @Cache annotation.
expireMills = 5000 indicates that the cache data will expires in 5 seconds after set.

```kotlin

public interface IData {

     fun getData(): Something
}

@Service
public class Data : IData {

    @Cache(key="'mykey'", expireMills = 5000, region="a")
     fun getData(): Something {
        //...
     }
}

```

SpEL was supported for key attribute and region attribute:


```kotlin

public interface IUserService {

     fun getUserById(userId: Long):User

    fun updateUser(user: User)
}

@Service
public class UserService : IUserService {

    @Cache(key="#userId", expireMills = 5000, region="'user-' + #userId % 4")
     fun getUserById(userId: Long):User{
         //...
     }

    @CacheRemove(key="#user.userId", region="'user-' + (#user.userId % 4)")
    fun updateUser(user: User){
        //...
    }
}

```

Sliding expiration time is also easy to use:

```kotlin

public interface ISessionService {
     fun getUserSession(userId: Long):UserSession
}

public interface ISessionService {

    @Cache(key="#userId", expireMills = 3600000, timePolicy = TimePolicy.Sliding)
     fun getUserSession(userId: Long):UserSession{
        //...
     }
}

```
>:bell:**Important:**
>
> Caching-kotlin will not provide the way annotations are used on the interface method any more, because the annotations on the interface break the rule that the interface should not care about details of implementing, and we think caching is also a detail of the implementation.
>So, all @CacheXXX have to annotation on implement class method


In a nested method, you might want to prevent the cache operation. for example, if you are using JPA to get data for updates, so you might want to get data directly from the database, this action can also be done easily:


```kotlin

fun withoutCache(){

    cacheScope(prevent = CacheOperation.Get){
        val user = userService.getUserById(123456)  
        //...
    }
}

```

or

```kotlin
@CacheScope(prevent = [CacheOperation.Get])
fun withoutCache(){

    val user = userService.getUserById(123456)  
    //...
}

```



