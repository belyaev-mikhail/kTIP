package ru.spbstu.tip.solvers

import ru.spbstu.tip.algorithms.*
import ru.spbstu.tip.lattice.Lattice
import ru.spbstu.tip.lattice.MapLattice
import ru.spbstu.tip.lattice.lub

interface Dependencies<Element> {
    fun incoming(e: Element): Iterable<Element>
    fun outgoing(e: Element): Iterable<Element>
}

interface WithDomain<K> {
    val domain: Iterable<K>
}

interface WithInit<K> {
    val init: Iterable<K>
}

interface LatticeSolver<Element> {
    val lattice: Lattice<Element>
    fun solve(): Element
}

val <Element> LatticeSolver<Element>.top get() = lattice.top
val <Element> LatticeSolver<Element>.bottom get() = lattice.bottom

interface SimpleFixpointSolver<Element>: LatticeSolver<Element> {
    override val lattice: Lattice<Element>
    fun f(e: Element): Element

    override fun solve(): Element {
        var x = bottom
        var t = x
        do {
            t = x
            x = f(x)
        } while (x != t)
        return x
    }
}

interface MapLatticeSolver<Key, ValueElement>: LatticeSolver<Mapping<Key, ValueElement>>, Dependencies<Key> {
    override val lattice: MapLattice<Key, ValueElement>

    fun transfer(e: Key, s: ValueElement): ValueElement

    fun funsub(e: Key, s: Mapping<Key, ValueElement>): ValueElement = transfer(e, join(e, s))

    fun join(e: Key, o: Mapping<Key, ValueElement>) = run {
        val states = incoming(e).map { o[it] }
        states.fold(lattice.sub.bottom, lattice.sub::lub)
    }
}

interface SimpleMapLatticeFixpointSolver<Key, ValueElement>:
        SimpleFixpointSolver<Mapping<Key, ValueElement>>,
        MapLatticeSolver<Key, ValueElement>,
        WithDomain<Key> {

    override fun f(e: Mapping<Key, ValueElement>): Mapping<Key, ValueElement> =
            domain.fold(bottom) { m, a ->
                m + (a to funsub(a, e))
            }
}

interface WithWorkList<N> {
    val workList: Queue<N>
}
@JvmName("defaultWithWorkList")
fun<N> default() = object : WithWorkList<N> {
    override val workList: Queue<N> = LinkedHashSetQueue()
}

interface WorklistSolver<N> : WithWorkList<N> {
    fun add(n: N) { workList += n }
    fun add(n: Iterable<N>) { workList += n }

    fun run(init: Iterable<N>) {
        workList.clear()
        workList += init
        while(!workList.empty) {
            process(workList.pop())
        }
    }

    fun process(n: N)
}

interface WorklistFixpointSolver<Key, ValueElement> : MapLatticeSolver<Key, ValueElement>, WorklistSolver<Key> {
    var currentElement: Mapping<Key, ValueElement>

    override fun process(n: Key) {
        val xn = currentElement[n]
        val y = funsub(n, currentElement)
        if(y != xn) {
            currentElement += n to y
            add(outgoing(n))
        }
    }
}

interface SimpleWorklistFixpointSolver<Key, ValueElement> :
        WorklistFixpointSolver<Key, ValueElement>,
        WithDomain<Key> {
    override fun solve() = run {
        currentElement = bottom
        run(domain)
        currentElement
    }
}

interface WorklistFixpointSolverWithInit<Key, ValueElement> :
        WorklistFixpointSolver<Key, ValueElement>,
        WithInit<Key> {
    override fun solve() = run {
        currentElement = bottom
        run(init)
        currentElement
    }
}

interface WorklistFixpointSolverWithInitAndSimpleWidening<Key, ValueElement>:
        WorklistFixpointSolverWithInit<Key, ValueElement> {

    fun widen(s: ValueElement): ValueElement

    fun isBackedge(src: Key, dst: Key): Boolean

    override fun process(n: Key) {
        val xn = currentElement[n]
        val y = funsub(n, currentElement)
        if(y != xn) {
            val out = outgoing(n)
            val yy = if (out.any { isBackedge(n, it) }) widen(y) else y
            currentElement += n to yy
            add(out)
        }
    }
}

interface WorklistFixpointSolverWithInitAndSimpleWideningAndNarrowing<Key, ValueElement>:
        WorklistFixpointSolverWithInitAndSimpleWidening<Key, ValueElement>,
        SimpleMapLatticeFixpointSolver<Key, ValueElement> {

    val narrowingSteps: Int

    fun narrow(x: Mapping<Key, ValueElement>, i: Int): Mapping<Key, ValueElement> =
            if(i <= 0) x else narrow(f(x), i - 1)

    override fun solve(): Mapping<Key, ValueElement> =
        narrow(super<WorklistFixpointSolverWithInitAndSimpleWidening>.solve(), narrowingSteps)
}

