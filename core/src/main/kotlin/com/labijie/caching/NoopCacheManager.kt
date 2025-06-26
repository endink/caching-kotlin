package com.labijie.caching

import java.lang.reflect.Type
import kotlin.reflect.KType

/**
 *
 * @Author: Anders Xiao
 * @Date: 2021/12/10
 * @Description:
 */
class NoopCacheManager private constructor() : ICacheManager {
    companion object {
        val INSTANCE: NoopCacheManager = NoopCacheManager()
    }

    override fun set(
        key: String,
        data: Any,
        kotlinType: KType?,
        expireMills: Long?,
        timePolicy: TimePolicy,
        region: String?,
        serializer: String?
    ) {

    }

    override fun setMulti(
        keyAndValues: Map<String, ICacheItem>,
        expireMills: Long?,
        timePolicy: TimePolicy,
        region: String?,
        serializer: String?
    ) {

    }

    override fun removeMulti(keys: Iterable<String>, region: String?): Int {
        return 0
    }

    override fun remove(key: String, region: String?): Boolean {
        return false
    }

    override fun refresh(key: String, region: String?): Boolean {
        return false
    }

    override fun clearRegion(region: String) {

    }

    override fun clear() {

    }

    override fun get(key: String, valueType: Type, region: String?): Any? {
        return null
    }

    override fun get(key: String, valueType: KType, region: String?): Any? {
        return null
    }
}