package com.labijie.caching.bean

import com.labijie.caching.CacheOperation
import com.labijie.caching.annotation.SuppressCache
import com.labijie.caching.model.ArgumentObject
import org.springframework.stereotype.Component

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-25
 */
@Component
open class SimpleScopedBean(private val simple: SimpleTestingBean) {


    @SuppressCache(operations = [CacheOperation.Get])
    fun getWithoutGet(args: ArgumentObject): ArgumentObject {
        return simple.getCached(args)
    }
}