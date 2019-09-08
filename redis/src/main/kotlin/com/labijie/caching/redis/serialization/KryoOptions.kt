package com.labijie.caching.redis.serialization

import kotlin.reflect.KClass

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-09-08
 */
class KryoOptions {
    var writeBufferSizeBytes = 1024 * 4
    var poolSize = -1
    var registeredClasses:MutableMap<Int, KClass<*>> = mutableMapOf()
    var isRegistrationRequired:Boolean = false
    var warnUnregisteredClasses:Boolean = false
}