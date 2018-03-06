package ru.spbstu.tip.lattice

import ru.spbstu.tip.algorithms.FulltableFunction


enum class SignDomain { POS, NEG, ZERO }

class SignLattice: FlatLattice<SignDomain>(), Evaluator<FlatLattice.Element<SignDomain>> {

    val pos = lift(SignDomain.POS)
    val neg = lift(SignDomain.NEG)
    val zero = lift(SignDomain.ZERO)

    private val allValues = listOf(top, bottom) + SignDomain.values().map(this::lift)
    private val plusMapping = FulltableFunction(allValues) { l, r ->
        when {
            l == bottom || r == bottom -> bottom
            l == top || r == top -> top
            l == r -> l
            else -> top
        }
    }
    override fun plus(lhv: Element<SignDomain>, rhv: Element<SignDomain>): Element<SignDomain> =
            plusMapping(lhv, rhv)

    private val minusMapping = FulltableFunction(allValues) { l, r ->
        when {
            l == bottom || r == bottom -> bottom
            l == top || r == top -> top
            l == pos && r != pos -> pos
            l == neg && r != neg -> neg
            l == zero && r == neg -> pos
            l == zero && r == pos -> neg
            l == zero && r == zero -> l
            else -> top
        }
    }
    override fun minus(lhv: Element<SignDomain>, rhv: Element<SignDomain>): Element<SignDomain> =
            minusMapping(lhv, rhv)

    private val timesMapping = FulltableFunction(allValues) { l, r ->
        when {
            l == bottom || r == bottom -> bottom
            l == top || r == top -> top
            l == zero || r == zero -> zero
            l == r -> pos
            l != r -> neg
            else -> top
        }
    }
    override fun times(lhv: Element<SignDomain>, rhv: Element<SignDomain>): Element<SignDomain> =
            timesMapping(lhv, rhv)

    private val divMapping = FulltableFunction(allValues) { l, r ->
        when {
            l == bottom || r in listOf(bottom, top, zero) -> bottom
            l == zero -> zero
            l == r -> pos
            l != r -> neg
            else -> top
        }
    }
    override fun div(lhv: Element<SignDomain>, rhv: Element<SignDomain>): Element<SignDomain> =
            divMapping(lhv, rhv)

    private val eqMapping = FulltableFunction(allValues) { l, r ->
        when {
            l == bottom || r == bottom -> bottom
            l == zero && r == zero -> pos
            else -> top
        }
    }
    override fun eq(lhv: Element<SignDomain>, rhv: Element<SignDomain>): Element<SignDomain> =
            eqMapping(lhv, rhv)

    private val gtMapping = FulltableFunction(allValues) { l, r ->
        when {
            l == bottom || r == bottom -> bottom
            l == top || r == top -> top
            l == pos && r != pos -> pos
            r == pos && l != pos -> zero
            l == zero && r == neg -> pos
            r == zero && l != pos -> zero
            else -> top
        }
    }
    override fun gt(lhv: Element<SignDomain>, rhv: Element<SignDomain>): Element<SignDomain> =
            gtMapping(lhv, rhv)
}