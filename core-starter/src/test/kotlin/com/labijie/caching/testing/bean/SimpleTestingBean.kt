package com.labijie.caching.testing.bean

import com.labijie.caching.annotation.Cache
import com.labijie.caching.annotation.CacheRemove
import com.labijie.caching.testing.model.ArgumentObject
import org.springframework.stereotype.Component

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-25
 */
@Component
class SimpleTestingBean {

    @Cache("#arg.stringValue", 30000)
    fun getCached(arg: ArgumentObject): ArgumentObject {
        return ArgumentObject()
    }

    @Cache("#arg.stringValue", 30000)
    fun getCachedOptionalArgs(longValue:Long? = null, arg: ArgumentObject): ArgumentObject {
        return ArgumentObject()
    }

    @CacheRemove("#key", delayMills = 2000)
    fun removeCacheDelay2s(key:String){

    }

    @CacheRemove("#key", delayMills = 5000)
    fun removeCacheDelay5s(key:String){

    }
}