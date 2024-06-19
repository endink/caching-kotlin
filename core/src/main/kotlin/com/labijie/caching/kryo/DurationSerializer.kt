/**
 * @author Anders Xiao
 * @date 2024-06-19
 */
package com.labijie.caching.kryo

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import java.time.Duration


object DurationSerializer: Serializer<Duration>() {
    override fun write(kryo: Kryo, output: Output, duration: Duration) {
        output.writeLong(duration.toNanos())
    }

    override fun read(kryo: Kryo, input: Input, durationClass: Class<out Duration>): Duration {
        val value = input.readLong()
        return Duration.ofNanos(value)
    }
}