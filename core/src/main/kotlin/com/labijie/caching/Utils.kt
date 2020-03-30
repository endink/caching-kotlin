package com.labijie.caching

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

fun getGenericType(rowType: Type, vararg actualTypeArguments:Type): ParameterizedType {
    return object : ParameterizedType {
        override fun getActualTypeArguments(): Array<Type> {
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