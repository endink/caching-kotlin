package com.labijie.caching.redis

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

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

    private val jacksonMapper: ObjectMapper = mapper ?: ObjectMapper()


    override fun <T : Any> deserializeData(type: KClass<T>, data: String): T? {
        if (data.isEmpty()) {
            @Suppress("CAST_NEVER_SUCCEEDS")
            return null as? T
        }
        try {
            return this.jacksonMapper!!.readValue(data, type.java)
        } catch (ex: Exception) {
            LOGGER.error("Redis cache manager serialize fault ( class: ${type.java.simpleName} ).", ex)
            throw RuntimeException(ex)
        }
    }

    override fun serializeData(data: Any): String {
        try {
            return jacksonMapper!!.writeValueAsString(data)
        } catch (ex: Exception) {
            LOGGER.error("Redis cache manager serialize fault ( class: ${data::class.java.simpleName} ).", ex)
            throw RuntimeException(ex)
        }

    }


}