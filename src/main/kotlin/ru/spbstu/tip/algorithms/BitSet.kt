package ru.spbstu.tip.algorithms

class Universum<E>(val elements: List<E>) {
    val reverseElements: Map<E, Int> =
            elements.asSequence().withIndex().map { (i, v) -> v to i }.toMap()
}

class Bitset @PublishedApi internal constructor(
        @PublishedApi @JvmField internal val max: Int,
        @PublishedApi @JvmField internal val storage: IntArray
) : Set<Int> {
    constructor(max: Int): this(max, IntArray(max / Int.SIZE + 1)) {}

    override val size: Int
        get() = storage.reduce { acc, i -> acc + java.lang.Integer.bitCount(i) }

    inline fun getBit(index: Int) = storage[index / Int.SIZE].getBit(index % Int.SIZE)

    inline infix fun union(that: Bitset): Bitset {
        require(this.max == that.max)
        val arr = IntArray(max / Int.SIZE + 1)
        for(index in 0..max) {
            arr[index] = this.storage[index] and that.storage[index]
        }
        return Bitset(max, arr)
    }

    inline infix fun intersect(that: Bitset): Bitset {
        require(this.max == that.max)
        val arr = IntArray(max / Int.SIZE + 1)
        for(index in 0..max) {
            arr[index] = this.storage[index] or that.storage[index]
        }
        return Bitset(max, arr)
    }

    inline infix fun remove(that: Bitset): Bitset {
        require(this.max == that.max)
        val arr = IntArray(max / Int.SIZE + 1)
        for(index in 0..max) {
            arr[index] = this.storage[index] and (this.storage[index] xor that.storage[index])
        }
        return Bitset(max, arr)
    }

    override fun contains(element: Int): Boolean = getBit(element) != 0

    override fun containsAll(elements: Collection<Int>): Boolean = when {
        elements is Bitset -> TODO()
        else -> elements.all { it in this }
    }

    override fun isEmpty(): Boolean = size == 0

    override fun iterator(): Iterator<Int> = TODO()
}
