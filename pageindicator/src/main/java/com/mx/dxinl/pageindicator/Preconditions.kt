package com.mx.dxinl.pageindicator

/**
 * Fail-fast
 */
fun checkNotNull(any: Any?, argumentName: String) {
    any ?: throw IllegalArgumentException(argumentName + " cannot be null")
}