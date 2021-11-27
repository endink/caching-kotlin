package com.labijie.caching.redis.configuration

import com.esotericsoftware.kryo.Registration
import com.esotericsoftware.kryo.Serializer
import com.labijie.caching.CacheException
import com.labijie.caching.redis.serialization.KryoOptions
import org.springframework.core.Ordered
import kotlin.reflect.KClass

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-09-09
 */
interface IKryoCacheDataSerializerCustomizer : Ordered {
    fun customize(options: KryoOptions)
}