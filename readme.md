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

Declare method cache using @Cached annotation.
expireMills = 5000 indicates that the cache data will expires in 5 seconds after set.

```kotlin

public interface IData {
    @Cached(key="xxx", expireMills = 5000, region="a")
     fun getData(): Something
}

```

SpEL was supported for key attribute and region attribute:


```kotlin

public interface IUserService {

    @Cache(key="#userId", expireMills = 5000, region="user-#{userId % 4}")
     fun getUserById(userId: Long):User

    @CacheRemove(key="#user.userId", region = region="user-#{userId % 4}")
    fun updateUser(user: User)
}

```

Sliding expiration time is also easy to use:

```kotlin

public interface ISessionService {

    @Cache(key="#userId", expireMills = 3600000, timePolicy = TimePolicy.Sliding)
     fun getUserSession(userId: Long):UserSession
}

```

In a nested method, you might want to prevent the cache operation. for example, if you are using JPA to get data for updates, so you might want to get data directly from the database, this action can also be done easily:


```kotlin

fun withoutCache(){

    cacheScope(prevent = CacheOperation.Get){
        val user = userService.getUserById(123456)  
        //...
    }
}

```



