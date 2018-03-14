package ru.spbstu.tip.solvers

sealed class Term<T>() {
    abstract val freeVars: Set<Var<T>>

    abstract fun subst(v: Var<T>, t: Term<T>): Term<T>
}

sealed class Var<T>(): Term<T>() {
    override val freeVars: Set<Var<T>> by lazy { setOf(this) }

    override fun subst(v: Var<T>, t: Term<T>): Term<T> = when {
        v == this -> t
        else -> this
    }
}

data class FreeVar<T>(val name: T): Var<T>()
data class BoundVar<T>(val original: Var<T>): Var<T>()

fun <T> mkBound(v: Var<T>) = when(v) {
    is FreeVar -> BoundVar(v)
    is BoundVar -> v
}

data class App<T>(val name: String, val arguments: List<Term<T>>): Term<T>() {
    override val freeVars: Set<Var<T>> by lazy { arguments.flatMapTo(mutableSetOf()) { it.freeVars } }

    override fun subst(v: Var<T>, t: Term<T>): Term<T> = copy(arguments = arguments.map { it.subst(v, t) })
}

data class Mu<T>(val parameter: BoundVar<T>, val body: Term<T>): Term<T>() {
    override fun subst(v: Var<T>, t: Term<T>): Term<T> = when {
        v == parameter -> throw IllegalArgumentException("Cannot substitute bound variable")
        else -> copy(body = body.subst(v, t))
    }

    override val freeVars: Set<Var<T>> by lazy { body.freeVars - parameter }

}

class UnionFindSolver<T> {
    private val parents = mutableMapOf<Term<T>, Term<T>>()
    private val ranks = mutableMapOf<Term<T>, Int>()

    private var Term<T>.rank
        get() = ranks.getOrPut(this) { 0 }
        set(value) { ranks[this] = value }
    private var Term<T>.parent
        get() = parents.getOrPut(this) { this }
        set(value) { parents[this] = value }
    private val Term<T>.isRoot: Boolean
        get() = parent == this
    private val Term<T>.root: Term<T> get() = when {
        this.isRoot -> this
        else -> parent.root.also { parent = it }
    }

    private infix fun Term<T>.union(that: Term<T>): Term<T> = run {
        val thisRoot = this.root
        val thatRoot = that.root

        when {
            thisRoot == thatRoot -> thisRoot
            // this generally breaks the ranking optimization, but whatever
            thisRoot is Var && thatRoot !is Var -> {
                thisRoot.parent = thatRoot
                thatRoot.rank = maxOf(thatRoot.rank, thisRoot.rank + 1)
                thatRoot
            }
            thisRoot !is Var && thatRoot is Var -> {
                thatRoot.parent = thisRoot
                thisRoot.rank = maxOf(thisRoot.rank, thatRoot.rank + 1)
                thisRoot
            }
            thisRoot.rank < thatRoot.rank -> thatRoot.also { thisRoot.parent = thatRoot }
            thisRoot.rank > thatRoot.rank -> thisRoot.also { thatRoot.parent = thisRoot }
            else -> thisRoot.also {
                thatRoot.parent = thisRoot
                thisRoot.rank++
            }
        }
    }

    public class UnificationException(vararg terms: Term<*>):
            Exception("Cannot unify terms: ${terms.joinToString(", ")}")

    private fun expect(predicate: Boolean, vararg terms: Term<*>) = when {
        predicate -> {}
        else -> throw UnificationException(*terms)
    }

    public fun unify(lhv: Term<T>, rhv: Term<T>) {
        val lRoot = lhv.root
        val rRoot = rhv.root
        if(lRoot == rRoot) return

        when {
            lRoot is App && rRoot is App -> {
                expect(lRoot.name == rRoot.name, lRoot, rRoot)
                expect(lRoot.arguments.size == rRoot.arguments.size, lRoot, rRoot)
                lRoot union rRoot
                lRoot.arguments.forEachIndexed { index, lt ->
                    unify(lt, rRoot.arguments[index])
                }
            }
            lRoot !is Var && rRoot !is Var -> expect(false, lRoot, rRoot)
            else -> lRoot union rRoot
        }
    }

    val variables: List<Var<T>> get() = parents.keys.filterIsInstance<Var<T>>()

    private fun closeRecursion(t: Term<T>,  visited: Set<Var<T>> = setOf()): Term<T> = run {
        when(t) {
            is Var -> {
                if(t !in visited && !t.isRoot) {
                    val cterm = closeRecursion(t.root, visited + t)
                    val newV = mkBound(t)
                    if(newV in cterm.freeVars) Mu(newV, cterm.subst(t, newV))
                    else cterm
                } else {
                    mkBound(t)
                }
            }
            is App -> {
                t.freeVars.fold(t as Term<T>) { acc, v -> acc.subst(v, closeRecursion(v, visited)) }
            }
            is Mu -> t.copy(body = closeRecursion(t.body, visited))
        }
    }

    fun calculateSolution(): Map<Var<T>, Term<T>> = variables.map { it to closeRecursion(it) }.toMap()

}



