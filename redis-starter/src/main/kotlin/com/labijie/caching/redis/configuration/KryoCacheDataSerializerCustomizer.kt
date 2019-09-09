package com.labijie.caching.redis.configuration

import com.labijie.caching.CacheException
import com.labijie.caching.redis.serialization.KryoOptions
import org.springframework.core.Ordered
import kotlin.reflect.KClass

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-09-09
 */
abstract class KryoCacheDataSerializerCustomizer : Ordered {

    companion object {
        internal val kryoOptions: KryoOptions = KryoOptions()
    }

    override fun getOrder(): Int {
        return 0
    }

    protected fun registerClass(id: Int, type: KClass<*>) {
        val existed = kryoOptions.registeredClasses.getOrDefault(id, null)
        if (existed != null) {
            throw CacheException("Class registered to kryo with id '$id' was existed. ( existed: ${existed.simpleName}, registering: ${type.simpleName} )")
        }
        kryoOptions.registeredClasses[id] = type
    }

    protected fun poolSize(size: Int) {
        kryoOptions.poolSize = size
    }

    protected fun isRegistrationRequired(required: Boolean) {
        kryoOptions.isRegistrationRequired = required
    }

    protected fun warnUnregisteredClasses(warn: Boolean) {
        kryoOptions.warnUnregisteredClasses = warn
    }

    abstract fun configure()
}