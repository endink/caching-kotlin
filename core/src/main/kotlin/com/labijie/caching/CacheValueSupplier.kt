/**
 * @author Anders Xiao
 * @date 2025-06-16
 */
package com.labijie.caching

import java.lang.reflect.Type

interface TypedCacheValueSupplier<T> {
    val supplier: (key: String) -> T?
    val returnType: Type
}