package ru.spbstu.tip.utility

import java.util.concurrent.atomic.AtomicInteger

abstract class UidGenerator {
    private val state = AtomicInteger(0)

    fun next(): Int = state.getAndIncrement()
}
