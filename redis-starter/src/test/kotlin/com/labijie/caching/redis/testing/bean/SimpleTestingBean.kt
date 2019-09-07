package com.labijie.caching.redis.testing.bean

import com.labijie.caching.annotation.Cache
import com.labijie.caching.annotation.CacheRemove
import com.labijie.caching.redis.testing.model.ArgumentObject
import org.springframework.stereotype.Component

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-25
 */
@Component
open class SimpleTestingBean {

    companion object {
        const val SIMPLE_CACHE_KEY = "simple-test"
        const val LIST_CACHE_KEY = "list-test"
        const val MAP_CACHE_KEY = "map-test"
    }

    @Cache("'$LIST_CACHE_KEY'", 30000)
    fun getCachedSimple(): ArgumentObject {
        return ArgumentObject()
    }

    @Cache("'$LIST_CACHE_KEY'", 30000)
    fun getCachedList(): List<ArgumentObject> {
        return listOf(
            ArgumentObject(),
            ArgumentObject(),
            ArgumentObject()
        )
    }

    @Cache("'$MAP_CACHE_KEY'", 30000)
    fun getCachedMap(): Map<String, ArgumentObject> {
        return mapOf(
            "a" to ArgumentObject(),
            "b" to ArgumentObject(),
            "c" to ArgumentObject()
        )
    }

    @CacheRemove("'$SIMPLE_CACHE_KEY'", delayMills = 0)
    fun removeCachedSimple() {
    }

    @CacheRemove("'$LIST_CACHE_KEY'", delayMills = 0)
    fun removeCachedList() {
    }

    @CacheRemove("'$MAP_CACHE_KEY'", delayMills = 0)
    fun removeCachedMap() {
    }
}