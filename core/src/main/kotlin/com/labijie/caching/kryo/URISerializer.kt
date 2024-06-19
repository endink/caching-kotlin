package com.labijie.caching.kryo

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import java.net.URI


/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-09-09
 */
object URISerializer : Serializer<URI>() {
    init {
        isImmutable = true
    }

    override fun write(kryo: Kryo, output: Output, uri: URI) {
        output.writeString(uri.toString())
    }

    override fun read(kryo: Kryo, input: Input, uriClass: Class<out URI>): URI {
        return URI.create(input.readString())
    }
}