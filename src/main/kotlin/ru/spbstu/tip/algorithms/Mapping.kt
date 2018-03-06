package ru.spbstu.tip.algorithms

data class Mapping<A, out E>(val default: E, val storage: Map<A, E> = mapOf()) {
    operator fun get(a: A): E = storage[a] ?: default
    override fun toString(): String {
        val def = ", * -> $default"
        return storage.entries.joinToString(
                prefix = "{",
                postfix = def + "}"
        ) { (k, v) -> "$k -> $v" }
    }

    companion object {
        fun<A, E> default(e: E) = Mapping<A, E>(e)
    }
}

fun<A, E> Mapping<A, E>.zip(that: Mapping<A, E>, f: (E, E) -> E): Mapping<A, E> {
    val result = mutableMapOf<A, E>()
    for((k, v) in this.storage) {
        result[k] = f(v, that[k])
    }
    for((k, v) in that.storage) if (k !in result) {
        result[k] = f(this[k], v)
    }

    return Mapping(f(this.default, that.default), result)
}

operator fun<A, E> Mapping<A, E>.plus(pair: Pair<A, E>) = copy(storage = storage + pair)
