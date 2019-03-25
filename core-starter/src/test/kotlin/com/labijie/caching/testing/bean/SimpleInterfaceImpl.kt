package com.labijie.caching.testing.bean

import com.labijie.caching.testing.model.ArgumentObject

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-25
 */
class SimpleInterfaceImpl:ISimpleInterface {
    override fun getCached(arg: ArgumentObject): ArgumentObject {
        return ArgumentObject()
    }

    override fun removeCacheDelay2s(key: String) {

    }
}