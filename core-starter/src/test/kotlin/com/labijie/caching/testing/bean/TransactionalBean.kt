package com.labijie.caching.testing.bean

import com.labijie.caching.annotation.Cache
import com.labijie.caching.annotation.CacheRemove
import com.labijie.caching.testing.model.ArgumentObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-25
 */
@Component
class TransactionalBean {

    @Autowired
    private lateinit var jdbcTemplate:JdbcTemplate

    @Transactional
    @Cache("#arg.stringValue", 30000)
    fun getCached(arg: ArgumentObject): ArgumentObject {
        return ArgumentObject()
    }

    @Transactional
    @Cache("#arg.stringValue", 30000)
    fun getCachedOptionalArgs(longValue:Long? = null, arg: ArgumentObject): ArgumentObject {
        return ArgumentObject()
    }

    @Transactional
    @CacheRemove("#key", delayMills = 2000)
    fun removeCacheDelay2s(key:String){

    }
}