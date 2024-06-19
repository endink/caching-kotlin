/**
 * @author Anders Xiao
 * @date 2024-06-19
 */
package com.labijie.caching.kryo

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import java.time.Instant


object InstantSerializer : Serializer<Instant>() {
    override fun write(kryo: Kryo, output: Output, instant: Instant) {
        output.writeLong(instant.toEpochMilli())
    }

    override fun read(kryo: Kryo, input: Input, instantClass: Class<out Instant>): Instant {
        return Instant.ofEpochMilli(input.readLong())
    }
}