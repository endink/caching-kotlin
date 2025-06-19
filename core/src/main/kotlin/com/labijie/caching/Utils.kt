package com.labijie.caching

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

fun getGenericType(rowType: Type, vararg actualTypeArguments:Type): ParameterizedType {
    return object : ParameterizedType {
        override fun getActualTypeArguments(): Array<Type> {
            @Suppress("UNCHECKED_CAST")
            return actualTypeArguments as Array<Type>
        }

        override fun getRawType(): Type {
            return rowType
        }

        override fun getOwnerType(): Type? {
            return null
        }
    }
}

fun isNativeImage(): Boolean {
    return try {
        Class.forName("org.graalvm.nativeimage.ImageInfo")
            .getMethod("inImageCode")
            .invoke(null) as Boolean
    } catch (ex: Throwable) {
        false
    }
}