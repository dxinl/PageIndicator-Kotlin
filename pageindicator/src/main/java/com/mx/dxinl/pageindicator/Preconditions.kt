package com.mx.dxinl.pageindicator

/**
 * Created by dxinl on 2017/07/30.
 */
fun checkNotNull(any: Any?, argumentName: String) {
    any ?: throw IllegalArgumentException(argumentName + " cannot be null")
}