package com.labijie.caching.component

import io.netty.util.HashedWheelTimer
import org.springframework.beans.factory.DisposableBean
import java.util.concurrent.TimeUnit

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-23
 */
class HashedWheelDelayTimer : IDelayTimer, DisposableBean {
    override fun destroy() {
        timerValue?.stop()
    }

    companion object {
        private val syncRoot: Any = Any()
        private var timerValue: HashedWheelTimer? = null

        @JvmStatic
        private val timer: HashedWheelTimer
            get() {
                if (timerValue == null) {
                    synchronized(syncRoot) {
                        if (timerValue == null) {
                            timerValue = HashedWheelTimer(500, TimeUnit.MILLISECONDS, 512).apply {
                                this.start()
                            }
                        }
                    }
                }
                return timerValue!!
            }
    }


    override fun delay(mills: Long, action: () -> Unit) {
        val delay = Math.max(500, mills)
        timer.newTimeout({_->
            action()
        }, delay, TimeUnit.MILLISECONDS)
    }
}