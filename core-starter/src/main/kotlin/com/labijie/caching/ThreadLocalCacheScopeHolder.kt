package com.labijie.caching

import com.labijie.caching.ICacheScopeHolder

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-22
 */
class ThreadLocalCacheScopeHolder() : ICacheScopeHolder {
    
    init {
        ICacheScopeHolder.Current = this
    }

    private val threadSettings: ThreadLocal<ICacheScopeHolder.ScopeSettings?> = ThreadLocal()


    override val isInScope: Boolean
        get() {
            return (threadSettings.get() != null)
        }

    override var settings: ICacheScopeHolder.ScopeSettings?
        get() = threadSettings.get()
        set(value) {
            if(value != null) {
                this.threadSettings.set(value)
            }else{
                this.threadSettings.remove()
            }
        }

}