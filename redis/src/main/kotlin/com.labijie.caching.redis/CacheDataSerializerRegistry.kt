package com.labijie.caching.redis

import java.util.HashMap
import java.util.Map

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-20
 */
object CacheDataSerializerRegistry {
    private val serializers: MutableMap<String, ICacheDataSerializer> = mutableMapOf()
    init {
        this.registerSerializer(JacksonCacheDataSerializer.NAME, JacksonCacheDataSerializer())
    }

    fun getSerializer(serializerName: String): ICacheDataSerializer {
        return serializers.getOrDefault(serializerName, null)
            ?: throw RuntimeException("Cant find cache data serializer with name '$serializerName'")
    }

    fun registerSerializer(serializerName: String, serializer: ICacheDataSerializer) {
        this.serializers[serializerName] = serializer
    }
}