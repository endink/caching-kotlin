package com.labijie.caching.redis

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.slf4j.LoggerFactory
import java.lang.reflect.Type

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-20
 */
class JacksonCacheDataSerializer @JvmOverloads constructor(mapper: ObjectMapper? = null) : ICacheDataSerializer {

    companion object {
        const val NAME = "json"
        private val LOGGER = LoggerFactory.getLogger(JacksonCacheDataSerializer::class.java)
    }

    override val name: String = NAME

    private val jacksonMapper: ObjectMapper = mapper ?: ObjectMapper().apply {
        this.configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false);
        //在序列化时日期格式默认为 yyyy-MM-dd'T'HH:mm:ss.SSSZ
        this.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,false)
        this.configure(SerializationFeature.INDENT_OUTPUT, true)
        this.configure(SerializationFeature.WRITE_ENUMS_USING_INDEX, true)
    }


    override fun deserializeData(type: Type, data: ByteArray): Any? {
        if (data.isEmpty()) {
            return null
        }
        try {
            val javaType= this.jacksonMapper.typeFactory.constructType(type)
            return this.jacksonMapper.readValue(data, javaType)
        } catch (ex: Exception) {
            LOGGER.error("Redis cache manager serialize fault ( class: $type ).", ex)
            throw RuntimeException(ex)
        }
    }

    override fun serializeData(data: Any): ByteArray {
        try {
            return jacksonMapper.writeValueAsBytes(data)
        } catch (ex: Exception) {
            LOGGER.error("Redis cache manager serialize fault ( class: ${data::class.java.simpleName} ).", ex)
            throw RuntimeException(ex)
        }

    }


}