package ru.spbstu.tip.algorithms

interface Queue<T> {
    val top: T
    val empty: Boolean

    fun add(value: T)
    fun addAll(values: Iterable<T>) = values.forEach { add(it) }
    fun pop(): T

    fun clear()
}

operator fun<T> Queue<T>.plusAssign(value: T) = add(value)
operator fun<T> Queue<T>.plusAssign(value: Iterable<T>) = addAll(value)

class LinkedHashSetQueue<T> : Queue<T> {
    private val data = LinkedHashSet<T>()

    override val empty get() = data.isEmpty()
    override val top: T = data.first()

    override fun add(value: T) { data.add(value) }
    override fun addAll(values: Iterable<T>) { data.addAll(values) }

    override fun pop(): T = top.also { data.remove(top) }
    override fun clear() = data.clear()
}