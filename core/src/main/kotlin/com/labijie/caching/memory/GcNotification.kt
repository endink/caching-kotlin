package com.labijie.caching.memory

import com.sun.management.GarbageCollectionNotificationInfo
import java.lang.management.ManagementFactory
import java.util.*
import java.util.function.Consumer
import javax.management.NotificationEmitter
import javax.management.NotificationListener
import javax.management.openmbean.CompositeData

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-02
 */
object GcNotification {
    private val OLDGEN_COLLECTOR_NAMES = arrayOf(
        // Oracle (Sun) HotSpot
        // -XX:+UseSerialGC
        "MarkSweepCompact",
        // -XX:+UseParallelGC and (-XX:+UseParallelOldGC or -XX:+UseParallelOldGCCompacting)
        "PS MarkSweep",
        // -XX:+UseConcMarkSweepGC
        "ConcurrentMarkSweep",
        // Oracle (BEA) JRockit
        // -XgcPrio:pausetime
        "Garbage collection optimized for short pausetimes Old Collector",
        // -XgcPrio:throughput
        "Garbage collection optimized for throughput Old Collector",
        // -XgcPrio:deterministic
        "Garbage collection optimized for deterministic pausetimes Old Collector"
    )

    fun register(state: Any?, callback: Consumer<Any?>) {
        ManagementFactory.getGarbageCollectorMXBeans().forEach { bean ->
            val notification = bean as NotificationEmitter
            notification.addNotificationListener(NotificationListener { n, _ ->
                val notifyType = n.type

                if (notifyType == GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION) {
                    val cd = n.userData as CompositeData
                    val info = GarbageCollectionNotificationInfo.from(cd)
                    val gcName = info.gcName
                    if (Arrays.binarySearch(OLDGEN_COLLECTOR_NAMES, gcName) >= 0) {

                        //System.out.println("FULL gc was notified !");
                        callback.accept(state)
                    }
                }
            }, null, null)
        }
    }
}