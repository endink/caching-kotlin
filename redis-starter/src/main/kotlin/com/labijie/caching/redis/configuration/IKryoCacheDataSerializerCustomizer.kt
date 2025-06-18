package com.labijie.caching.redis.configuration

import com.labijie.caching.redis.serialization.KryoOptions
import org.springframework.core.Ordered

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-09-09
 */
interface IKryoCacheDataSerializerCustomizer : Ordered {
    override fun getOrder(): Int = 0
    fun customize(options: KryoOptions)
}