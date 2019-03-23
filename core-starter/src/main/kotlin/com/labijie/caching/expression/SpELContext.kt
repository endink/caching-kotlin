package com.labijie.caching.expression

import java.lang.reflect.Method

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-23
 */
class SpELContext(val method:Method, val methodArgs:Array<Any?>, val returnValue:Any? = null)