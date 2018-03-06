package ru.spbstu.tip.algorithms

class UnionFind<T> {
    inner class Node(private var value: T,
                     private var parent: Node? = null,
                     private var rank: Int = 0) {

        fun swap(that: Node) {
            require(that.root === root)

            val tmp = that.value
            that.value = value
            value = tmp
        }

        val root: Node
            get() = parent?.root?.also { parent = it } ?: this

        infix fun unite(that: Node): Node = run {
            val lhv: Node = this.root
            val rhv: Node = that.root
            when {
                lhv.rank < rhv.rank -> {
                    lhv.parent = rhv
                    rhv
                }
                rhv.rank > lhv.rank -> {
                    rhv.parent = lhv
                    lhv
                }
                else -> {
                    rhv.parent = lhv
                    lhv.rank++
                    lhv
                }
            }
        }

        override fun equals(other: Any?): Boolean = when(other) {
            !is UnionFind<*>.Node -> false
            other.root === root -> true
            else -> false
        }

        override fun hashCode(): Int = root.value?.hashCode() ?: 0

        override fun toString(): String {
            return "Node(value=$value, rank=$rank)"
        }
    }

    fun single(value: T) = Node(value)
    fun unite(lhv: Node, rhv: Node) = lhv unite rhv
}
