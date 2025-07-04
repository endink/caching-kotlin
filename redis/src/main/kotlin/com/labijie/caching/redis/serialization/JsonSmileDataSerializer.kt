package com.labijie.caching.redis.serialization

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.smile.databind.SmileMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.labijie.caching.redis.CacheDataDeserializationException
import com.labijie.caching.redis.CacheDataSerializationException
import com.labijie.caching.redis.ICacheDataSerializer
import java.lang.reflect.Type
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType

/**
 *
 * @Author: Anders Xiao
 * @Date: 2021/12/10
 * @Description:
 */
class JsonSmileDataSerializer@JvmOverloads constructor(mapper: SmileMapper? = null) : ICacheDataSerializer {
    companion object {
        const val NAME = "json-smile"

        fun createObjectMapper(): SmileMapper {
            return SmileMapper().apply {
                configure(
                    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false
                )

                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                configure(SerializationFeature.WRITE_ENUMS_USING_INDEX, true)
                registerKotlinModule()
            }
        }
    }

    override val name: String = NAME

    private val smileMapper: SmileMapper = mapper ?: createObjectMapper()


    override fun deserializeData(type: Type, data: ByteArray): Any? {
        if (data.isEmpty()) {
            return null
        }
        try {
            val javaType = this.smileMapper.typeFactory.constructType(type)
            return this.smileMapper.readValue(data, javaType)
        } catch (ex: Throwable) {
            val error = "Redis cache manager serialize fault ( ser:$NAME class: $type )."
            throw CacheDataDeserializationException(error, ex)
        }
    }

    override fun serializeData(data: Any, kotlinType: KType?): ByteArray {
        try {
            return smileMapper.writeValueAsBytes(data)
        } catch (ex: Throwable) {
            val error = "Redis cache manager serialize fault ( ser:$NAME class: ${data::class.java} )."
            throw CacheDataSerializationException(error, ex)
        }

    }

}