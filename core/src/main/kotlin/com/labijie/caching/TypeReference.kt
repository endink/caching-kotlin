/**
 * @author Anders Xiao
 * @date 2025-06-16
 */
package com.labijie.caching

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


abstract class TypeReference<T> {

    val type: Type

    init {
        val superClass = javaClass.genericSuperclass
        require(superClass is ParameterizedType) {
            "TypeReference must be created with actual type parameters."
        }
        this.type = superClass.actualTypeArguments[0]
    }

    override fun toString(): String = type.typeName
}