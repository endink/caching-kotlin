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
import java.time.LocalDateTime
import java.time.ZoneOffset


object LocalDateTimeSerializer : Serializer<LocalDateTime>() {
    override fun write(kryo: Kryo, output: Output, datetime: LocalDateTime) {
        output.writeLong(datetime.toInstant(ZoneOffset.UTC).toEpochMilli())
    }

    override fun read(kryo: Kryo, input: Input, datetime: Class<out LocalDateTime>): LocalDateTime {
        val instant = Instant.ofEpochMilli(input.readLong())
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
    }
}