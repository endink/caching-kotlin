package com.labijie.caching.redis

import java.lang.reflect.Type

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-20
 */
interface ICacheDataSerializer {
    val name:String
    fun deserializeData(type: Type, data: String): Any?
    fun serializeData(data: Any): String
}