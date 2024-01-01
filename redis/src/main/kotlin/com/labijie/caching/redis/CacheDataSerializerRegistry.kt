package com.labijie.caching.redis

import com.labijie.caching.CacheException
import com.labijie.caching.redis.serialization.JacksonCacheDataSerializer
import com.labijie.caching.redis.serialization.JsonSmileDataSerializer
import com.labijie.caching.redis.serialization.KryoCacheDataSerializer
import com.labijie.caching.redis.serialization.KryoOptions
import org.slf4j.LoggerFactory

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-20
 */
object CacheDataSerializerRegistry {
    private val serializers: MutableMap<String, ICacheDataSerializer> = mutableMapOf()
    private val logger = LoggerFactory.getLogger(CacheDataSerializerRegistry::class.java)

    fun getSerializer(name: String): ICacheDataSerializer {
        val serializerName = name.lowercase()
        return serializers.getOrPut(serializerName) {
            logger.warn("Unable to find caching data serializer named: '${serializerName}', jackson serializer was used.")
            return JacksonCacheDataSerializer()
        }
    }

    fun registerSerializer(serializer: ICacheDataSerializer) {
        if (serializer.name.isBlank()) {
            throw CacheException("${ICacheDataSerializer::class.java.simpleName} name must not be null or empty string.")
        }

        val existed = this.serializers[serializer.name]
        if (existed != null) {
            logger.warn(
                """
                Cache data serializer named '${serializer.name}' will be replace !!!
                existed: ${existed::class.java.simpleName}, replaced: ${serializer::class.java.simpleName}
            """.trimIndent()
            )
        }
        this.serializers[serializer.name] = serializer
    }
}