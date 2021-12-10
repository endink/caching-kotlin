package com.labijie.caching.redis.configuration

import com.fasterxml.jackson.dataformat.smile.databind.SmileMapper
import org.springframework.core.Ordered

/**
 *
 * @Author: Anders Xiao
 * @Date: 2021/12/10
 * @Description:
 */
interface IJsonSmileCacheDataSerializerCustomizer: Ordered {
    override fun getOrder(): Int = 0

    fun customize(smileMapper: SmileMapper)
}