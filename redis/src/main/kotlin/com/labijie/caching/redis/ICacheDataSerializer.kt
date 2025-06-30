package com.labijie.caching.redis

import java.lang.reflect.Type
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-20
 */
@JvmDefaultWithCompatibility
interface ICacheDataSerializer {

    val name:String
    fun serializeData(data: Any, kotlinType: KType?): ByteArray

    fun deserializeData(type: Type, data: ByteArray): Any?
    fun deserializeData(type: KType, data: ByteArray): Any? {
        return deserializeData(type.javaType, data)
    }
}