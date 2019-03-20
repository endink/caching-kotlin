package com.labijie.caching.redis

import kotlin.reflect.KClass

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-20
 */
interface ICacheDataSerializer {
    fun <T:Any> deserializeData(type: KClass<T>, data: String): T?
    fun serializeData(data: Any): String
}