package ru.spbstu.tip.algorithms

import kotlinx.Warnings

val Int.Companion.SIZE inline get() = 32
val Long.Companion.SIZE inline get() = 64
val Float.Companion.SIZE inline get() = 32
val Double.Companion.SIZE inline get() = 64
val Short.Companion.SIZE inline get() = 16
val Byte.Companion.SIZE inline get() = 8 // duh!

inline fun<reified T> sizeOf() = when {
    0 is T -> Int.SIZE / Byte.SIZE
    0L is T -> Long.SIZE / Byte.SIZE
    0.0 is T -> Double.SIZE / Byte.SIZE
    0.0f is T -> Float.SIZE / Byte.SIZE
    0.toShort() is T -> Short.SIZE / Byte.SIZE
    0.toByte() is T -> Byte.SIZE / Byte.SIZE
    else -> Long.SIZE / Byte.SIZE
}

inline fun<reified T> sizeOf(value: T): Int = sizeOf<T>()

@Suppress(Warnings.NOTHING_TO_INLINE)
inline fun Long.getBit(index: Int) = (this ushr index) and 1
@Suppress(Warnings.NOTHING_TO_INLINE)
inline fun Int.getBit(index: Int) = (this ushr index) and 1

@Suppress(Warnings.NOTHING_TO_INLINE)
inline fun Long.setBit(index: Int) = this or (1L shl index)
@Suppress(Warnings.NOTHING_TO_INLINE)
inline fun Int.setBit(index: Int) = this or (1 shl index)

@Suppress(Warnings.NOTHING_TO_INLINE)
inline fun Long.getBits(from: Int, to: Int): Long {
    require(from >= 0)
    require(to < Long.SIZE)
    val mask = ((1L shl (to - from + 1)) - 1) shl from
    return (this and mask) ushr from
}
@Suppress(Warnings.NOTHING_TO_INLINE)
inline fun Int.getBits(from: Int, to: Int): Int {
    require(from >= 0)
    require(to < Int.SIZE)
    val mask = ((1 shl (to - from + 1)) - 1) shl from
    return (this and mask) ushr from
}

val Int.numberOfLeadingZeros get() = Integer.numberOfLeadingZeros(this)
val Int.bitsSet get() = Integer.bitCount(this)
