package com.labijie.caching.redis.serialization

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.labijie.caching.redis.CacheDataDeserializationException
import com.labijie.caching.redis.CacheDataSerializationException
import com.labijie.caching.redis.ICacheDataSerializer
import java.io.IOException
import java.lang.reflect.Type

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-20
 */
class JacksonCacheDataSerializer @JvmOverloads constructor(mapper: ObjectMapper? = null) :
    ICacheDataSerializer {

    companion object {
        const val NAME = "json"

        fun createObjectMapper(): ObjectMapper {
            return ObjectMapper().apply {
                configure(
                    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false
                )

                setSerializationInclusion(JsonInclude.Include.NON_NULL)

                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                configure(SerializationFeature.WRITE_ENUMS_USING_INDEX, true)
                registerKotlinModule()
            }
        }
    }

    override val name: String = NAME

    private val jacksonMapper: ObjectMapper = mapper ?: createObjectMapper()


    override fun deserializeData(type: Type, data: ByteArray): Any? {
        if (data.isEmpty()) {
            return null
        }
        try {
            val javaType = this.jacksonMapper.typeFactory.constructType(type)
            return this.jacksonMapper.readValue(data, javaType)
        } catch (ex: IOException) {
            val error = "Redis cache manager serialize fault ( ser:$NAME class: $type )."
            throw CacheDataDeserializationException(error, ex)
        }
    }

    override fun serializeData(data: Any): ByteArray {
        try {
            return jacksonMapper.writeValueAsBytes(data)
        } catch (ex: IOException) {
            val error = "Redis cache manager serialize fault ( ser:$NAME class: ${data::class.java} )."
            throw CacheDataSerializationException(error, ex)
        }

    }


}