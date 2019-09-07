package com.labijie.caching.model

import java.math.BigDecimal

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-23
 */
class MethodObject {
    fun primitiveArgMethod(intValue:Int, stringValue:String, decimalValue:BigDecimal): ArgumentObject {
        return ArgumentObject()
    }

    fun objectArgMethod(obj: ArgumentObject): ArgumentObject {
        return obj
    }

    fun compositeArgMethod(arg1:Int, arg2: ArgumentObject, arg3:Long?): ArgumentObject {
        return arg2
    }
}