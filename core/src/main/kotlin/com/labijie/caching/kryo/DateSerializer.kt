package com.labijie.caching.kryo

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.KryoException
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import java.util.*


/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-09-09
 */
object DateSerializer : Serializer<Date>() {
    /**
     * {@inheritDoc}
     */
    override fun read(kryo: Kryo, input: Input, type: Class<out Date>): Date {
        try {
            return Date(input.readLong(true))
        } catch (e: Exception) {
            throw KryoException(e)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun write(kryo: Kryo, output: Output, obj: Date) {
        output.writeLong(obj.time, true)
    }

    override fun copy(kryo: Kryo, original: Date): Date {
        return original.clone() as Date
    }

}