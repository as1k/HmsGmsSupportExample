package com.asik.hmsgmssupportexample2

import java.util.concurrent.ConcurrentLinkedDeque

inline fun justTry(block: () -> Unit) {
    try {
        block()
    } catch (e: Exception) {
        // catch any exception but ignore it
    }
}

fun <T> ConcurrentLinkedDeque<T>.addWithLimitCheck(item: T, limit: Int = 100) {
    if (this.size == limit) {
        this.removeFirst()
    }
    this.addLast(item)
}
