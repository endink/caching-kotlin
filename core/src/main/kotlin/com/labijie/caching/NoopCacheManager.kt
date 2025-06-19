package com.labijie.caching

import java.lang.reflect.Type

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

    override fun set(key: String, data: Any, expireMills: Long?, timePolicy: TimePolicy, region: String?) {

    }

    override fun setMulti(
        keyAndValues: Map<String, Any>,
        expireMills: Long?,
        timePolicy: TimePolicy,
        region: String?
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
}