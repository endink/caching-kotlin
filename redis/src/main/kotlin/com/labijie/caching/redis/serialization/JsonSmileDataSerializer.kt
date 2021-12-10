package com.labijie.caching.redis.serialization

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.smile.databind.SmileMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.labijie.caching.redis.CacheDataDeserializationException
import com.labijie.caching.redis.CacheDataSerializationException
import com.labijie.caching.redis.ICacheDataSerializer
import org.slf4j.LoggerFactory
import java.io.IOException
import java.lang.reflect.Type
import java.util.*

/**
 *
 * @Author: Anders Xiao
 * @Date: 2021/12/10
 * @Description:
 */
class JsonSmileDataSerializer@JvmOverloads constructor(mapper: SmileMapper? = null) : ICacheDataSerializer {
    companion object {
        const val NAME = "json-smile"
        private val LOGGER = LoggerFactory.getLogger(JacksonCacheDataSerializer::class.java)

        fun createObjectMapper(): SmileMapper {
            return SmileMapper().apply {
                configure(
                    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false
                )
                //在序列化时日期格式默认为 yyyy-MM-dd'T'HH:mm:ss.SSSZ
                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                configure(SerializationFeature.WRITE_ENUMS_USING_INDEX, true)
                registerKotlinModule()
            }
        }
    }

    override val name: String = NAME

    private val smileMapper: SmileMapper = mapper ?: createObjectMapper()


    override fun deserializeData(type: Type, data: String): Any? {
        if (data.isEmpty()) {
            return null
        }
        try {
            val javaType = this.smileMapper.typeFactory.constructType(type)
            val byteArray = Base64.getDecoder().decode(data)
            return this.smileMapper.readValue(byteArray, javaType)
        } catch (ex: Throwable) {
            val error = "Redis cache manager serialize fault ( ser:$NAME class: $type )."
            throw CacheDataDeserializationException(error, ex)
        }
    }

    override fun serializeData(data: Any): String {
        try {
            val bytes= smileMapper.writeValueAsBytes(data)
            return Base64.getEncoder().encodeToString(bytes)
        } catch (ex: Throwable) {
            val error = "Redis cache manager serialize fault ( ser:$NAME class: ${data::class.java} )."
            throw CacheDataSerializationException(error, ex)
        }

    }
}