package ru.spbstu.tip.lattice

import ru.spbstu.tip.algorithms.Mapping
import ru.spbstu.tip.algorithms.zip

open class FlatLattice<X> : Lattice<FlatLattice.Element<X>> {
    override val bottom: Element<X>
        get() = Element.Bottom
    override val top: Element<X>
        get() = Element.Top

    sealed class Element<out X> {
        data class Simple<out X>(val value: X): Element<X>()
        object Top: Element<Nothing>()
        object Bottom: Element<Nothing>()
    }

    fun lift(value: X): Element<X> = Element.Simple(value)
}

open class PairLattice<A, B>(val left: Lattice<A>, val right: Lattice<B>) : Lattice<Pair<A, B>> {
    override val bottom: Pair<A, B> = left.bottom to right.bottom
    override val top: Pair<A, B> = left.top to right.top
    override fun Pair<A, B>.lub(that: Pair<A, B>): Pair<A, B> =
            left.lub(this.first, that.first) to right.lub(this.second, that.second)
    override fun Pair<A, B>.leq(that: Pair<A, B>): Boolean =
            left.leq(this.first, that.first) && right.leq(this.second, that.second)
}

open class UniformProductLattice<T>(val sub: Lattice<T>, n: Int) : Lattice<List<T>> {
    override val bottom: List<T> = List(n){ sub.bottom }
    override val top: List<T> = List(n){ sub.top }

    override fun List<T>.lub(that: List<T>): List<T> = when {
        size != that.size -> throw IllegalStateException("Lists of different sizes")
        else -> this@lub.zip(that, sub::lub)
    }
}

open class MapLattice<A, E>(val sub: Lattice<E>): Lattice<Mapping<A, E>> {
    override val bottom: Mapping<A, E> = Mapping.default(sub.bottom)
    override val top: Mapping<A, E> = Mapping.default(sub.top)
    override fun Mapping<A, E>.lub(that: Mapping<A, E>): Mapping<A, E> =
            if(this === that) this else this.zip(that, sub::lub)
}

inline fun<A, E> MapLattice(sub: Lattice<E>, witness: () -> Iterable<A>): MapLattice<A, E> = MapLattice(sub)

open class PowersetLattice<A>(val universe: Set<A>): Lattice<Set<A>> {
    override val bottom: Set<A> = emptySet()
    override val top: Set<A> = universe
    override fun Set<A>.lub(that: Set<A>): Set<A> = this + that
    override fun Set<A>.leq(that: Set<A>): Boolean = this.containsAll(that)
    override fun contains(element: Set<A>): Boolean = universe.containsAll(element)
}

abstract class ReverseLattice<E>(val original: Lattice<E>): Lattice<E> {
    override val bottom: E = original.top
    override val top: E = original.bottom
    override fun E.leq(that: E): Boolean = with(original) { that leq this@leq }
    override fun contains(element: E): Boolean = original.contains(element)
}

open class ReversePowersetLattice<A>(universe: Set<A>): ReverseLattice<Set<A>>(PowersetLattice(universe)) {
    override fun Set<A>.lub(that: Set<A>): Set<A> = this intersect that
}
