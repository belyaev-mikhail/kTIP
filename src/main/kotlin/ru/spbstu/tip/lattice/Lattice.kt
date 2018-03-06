package ru.spbstu.tip.lattice;

import kotlinx.Warnings

interface Lattice<Element> {
    operator fun contains(element: Element): Boolean = true
    val bottom: Element
    val top: Element

    infix fun Element.lub(that: Element): Element = when {
        this == bottom -> that
        that == bottom -> this
        this == that -> this
        else -> top
    }
    infix fun Element.leq(that: Element): Boolean = this lub that == this
    infix fun Element.geq(that: Element): Boolean = that leq this
    infix fun Element.eq(that: Element): Boolean = this leq that && that leq this

}

@Suppress(Warnings.NOTHING_TO_INLINE)
inline fun <E> Lattice<E>.lub(lhv: E, rhv: E) = lhv lub rhv
@Suppress(Warnings.NOTHING_TO_INLINE)
inline fun <E> Lattice<E>.leq(lhv: E, rhv: E) = lhv leq rhv
