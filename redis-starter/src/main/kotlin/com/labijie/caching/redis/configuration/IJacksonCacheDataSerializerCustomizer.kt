package com.labijie.caching.redis.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.Ordered

interface IJacksonCacheDataSerializerCustomizer: Ordered {
    override fun getOrder(): Int = 0

    fun customize(objectMapper: ObjectMapper)
}