# Labijie Caching ( kotlin )
>for java see this: https://github.com/endink/caching

### This project is in the process of development....

A cache structure that supports expiration on a per-key basis.


![Hex.pm](https://img.shields.io/hexpm/l/plug.svg)
[![Maven metadata URL](https://img.shields.io/maven-metadata/v/http/central.maven.org/maven2/com/labijie/caching-kotlin//maven-metadata.xml.svg)](http://central.maven.org/maven2/com/labijie/caching-kotlin/)

All of the jar packages has been uploaded to the maven central.


## add depenedency in gradle project 

Use memory cahce only:
```groovy
dependencies {
    compile "com.labijie:caching-kotlin:1.0.0"
}
```

for redis:
```groovy
dependencies {
    compile "com.labijie:caching-kotlin-redis:1.0.0"
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

for memory:
```groovy
dependencies {
    compile "com.labijie:caching-kotlin-core-starter:1.0"
}
```

for redis:

```groovy
dependencies {
    compile "com.labijie:caching-kotlin-redis-starter:1.0"
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

### Work with redis

#### Configuration

The redis cache supports multiple regions, different regions can be different redis db, or they can be different redis server.

The following example shows the Redis configuration with two regions ( region1, region2 ):

```groovy
infra:
  caching:
    redis:
      regions:
        region1:
          url: redis://localhost:6379/1
        region2:
          url: redis://localhost:6379/2
```
The Redis cache also supports the master-slave redis servers (region name: default):

```groovy

infra:
  caching:
    redis:
      regions:
        default: 
          url: redis://host1:6379, redis://host2:6379, redis://host3:6379

```

Since we use lettuce as a redis client, the URLs in all of the above examples are "lettuce schema", and to learn more about redis url, see here: https://github.com/lettuce-io/lettuce-core/wiki/Redis-URI-and-connection-details

> Current version read preferred from slaves and fall back to master if no slave is not available by default.
> Specified operation to read master will be supported in the future but not now ..

#### Redis Data Serializer

Jackson is used as a serializer by default in the redis implementation, so the objects you want to cache must can be serialized and deserialized by Jackson (for example, it must contains a none args constructor).

Caching-kotlin also provide the ability to customize serializer:

```kotlin
@Component
class KryoSerializer : ICacheDataSerializer {
    override val name: String = "kryo"
    
    override fun serializeData(data: Any): String {
        //...
    }

    override fun <T : Any> deserializeData(type: KClass<T>, data: String): T? {
        //...
    }
}
```
A serializer has only 2 conditions: 
1. It must be an implement of ICacheDataSerializer interface
2. It must be a Spring bean

> Caching-kotlin will automatically load all beans that implement the ICacheDataSerializer interface,
> they will be auto registered when application startup.

After you define a serializer, you can use it like follow:

```groovy

infra:
  caching:
    redis:
      regions:
        default: 
          url: redis://localhost:6379
          serializer: kryo

```




