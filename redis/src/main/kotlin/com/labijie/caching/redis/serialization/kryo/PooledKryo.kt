package com.labijie.caching.redis.serialization.kryo

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.*
import com.esotericsoftware.kryo.util.Pool
import com.labijie.caching.kryo.IKryoSerializer
import java.nio.ByteBuffer

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-02-16
 */
internal abstract class PooledKryo(poolSize: Int, outputBufferSizeBytes:Int = 4 * 1024): IKryoSerializer {

    class Pooled<TValue> internal constructor(val instance:TValue, private val returnObject:()->Unit):AutoCloseable {
        override fun close() {
            this.returnObject()
        }
    }

    constructor(poolSize: Int = Runtime.getRuntime().availableProcessors() * 2) : this(poolSize, 4 * 1024)

    private val size = if(poolSize <= 0) Runtime.getRuntime().availableProcessors() * 2 else poolSize
    private val kryoPool = newPool(size)
    private val outputBufferPool = newByteBufferOutputPool(size)
    private val outputPool = newOutputPool(size)
    private val outputInitSizeBytes = outputBufferSizeBytes.coerceAtLeast(512)



    private fun newPool(poolSize: Int): Pool<Kryo> {
        return object : Pool<Kryo>(true, false, poolSize) {
            override fun create(): Kryo {
                return createKryo()
            }
        }
    }

    private fun newInputPool(poolSize: Int): Pool<Input> {
        return object : Pool<Input>(true, true, poolSize) {
            override fun create(): Input {
                return Input()
            }
        }
    }

    private fun newOutputPool(poolSize: Int): Pool<Output> {
        return object : Pool<Output>(true, true, poolSize) {
            override fun create(): Output {
                return Output(outputInitSizeBytes, -1)
            }
        }
    }

    private fun newByteBufferOutputPool(poolSize: Int): Pool<ByteBufferOutput> {
        return object : Pool<ByteBufferOutput>(true, true, poolSize) {
            override fun create(): ByteBufferOutput {
                return ByteBufferOutput(outputInitSizeBytes, -1)
            }
        }
    }

    private fun newByteBufferInputPool(poolSize: Int): Pool<ByteBufferInput> {
        return object : Pool<ByteBufferInput>(true, true, poolSize) {
            override fun create(): ByteBufferInput {
                return ByteBufferInput()
            }
        }
    }

    protected abstract fun createKryo(): Kryo

    fun serializeBuffer(data: Any): Pooled<ByteBuffer> {
        val output = outputBufferPool.obtain()
        try {
            output.byteBuffer.clear()
            val kryo = kryoPool.obtain()
            try {
                kryo.writeObject(output, data)
            } finally {
                kryoPool.free(kryo)
            }
            output.flush()

            output.byteBuffer.run {
                this.limit(output.total().toInt())
                this.position(0)
            }
        } catch (ex:Throwable) {
            outputBufferPool.free(output)
            throw ex
        }
        return Pooled(output.byteBuffer){ outputBufferPool.free(output) }
    }

    fun deserializeByteBuffer(data: ByteBuffer, clazz:Class<*>): Any {
        val input = ByteBufferInput(data)
        val kryo = kryoPool.obtain()
        try {
            return kryo.readObject(input, clazz)
        } finally {
            kryoPool.free(kryo)
            input.close()
        }
    }

    override fun serialize(data: Any): ByteArray {
        val output = outputPool.obtain()
        try {
            output.reset()
            val kryo = kryoPool.obtain()
            try {
                kryo.writeObject(output, data)
            } finally {
                kryoPool.free(kryo)
            }
            output.flush()
            return output.toBytes()
        } catch (ex:Throwable) {
            outputPool.free(output)
            throw ex
        }finally {
            outputPool.free(output)
        }
    }

    override fun deserialize(data: ByteArray, clazz: Class<*>): Any {
        Input(data).use {
            val kryo = kryoPool.obtain()
            try {
                return kryo.readObject(it, clazz)
            } finally {
                kryoPool.free(kryo)
            }
        }

    }
}