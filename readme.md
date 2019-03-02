# Labijie Caching ( kotlin )
>for java see this: https://github.com/endink/caching

A cache structure that supports expiration on a per-key basis.


![Hex.pm](https://img.shields.io/hexpm/l/plug.svg)
[![Maven metadata URL](https://img.shields.io/maven-metadata/v/http/central.maven.org/maven2/com/labijie/labijie-caching-kotlin/maven-metadata.xml.svg)](http://central.maven.org/maven2/com/labijie/labijie-caching)

All of the jar packages has been uploaded to the maven central.


## add depenedency in gradle project (coming soon...)

Use memory cahce only:
```groovy
dependencies {
    compile "com.labijie:labijie-caching-kotlin:1.0"
}
```

for redis:
```groovy
dependencies {
    compile "com.labijie:labijie-caching-kotlin-redis:1.0"
}
```

## Memeory Quick Start
You only need to use the ICacheManager interface, which can be easily integrated into the IOC container, such as spring.
use 

```kotlin
val memoryCache = MemoryCacheManager(MemoryCacheOptions());

//sliding time expires
memoryCache.set("2", Any(), 3000L, true);

//absolute time expires
memoryCache.set("a", Any(), 1000L, false);

//get
memoryCache.get("a")

```


## Redis Quick Start
coming soon ...

