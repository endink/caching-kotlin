package com.labijie.caching.redis

import com.labijie.caching.CacheException
import com.labijie.caching.redis.serialization.JacksonCacheDataSerializer

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-20
 */
object CacheDataSerializerRegistry {
    private val serializers: MutableMap<String, ICacheDataSerializer> = mutableMapOf()
    init {
        this.registerSerializer(JacksonCacheDataSerializer())
    }

    fun getSerializer(serializerName: String): ICacheDataSerializer {
        return serializers.getOrDefault(serializerName, null)
            ?: throw RuntimeException("Cant find cache data serializer with name '$serializerName'")
    }

    fun registerSerializer(serializer: ICacheDataSerializer) {
        if(serializer.name.isBlank()){
            throw CacheException("${ICacheDataSerializer::class.java.simpleName} name must not be null or empty string.")
        }
        this.serializers[serializer.name] = serializer
    }
}