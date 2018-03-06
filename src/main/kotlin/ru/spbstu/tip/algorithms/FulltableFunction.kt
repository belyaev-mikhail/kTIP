package ru.spbstu.tip.algorithms

class FulltableFunction<E: Any>(domain: List<E>, calc: (E, E) -> E): (E, E) -> E {
    val encoding = domain.mapIndexed { i, e -> e to i }.toMap()
    val table: Array<Array<E>> =
            Array(domain.size) {
                Array(domain.size) {
                    domain.first() as Any
                } as Array<E>
            }

    init {
        for(l in domain) {
            for(r in domain) {
                table[encoding[l]!!][encoding[r]!!] = calc(l, r)
            }
        }
    }

    override fun invoke(l: E, r: E): E = table[encoding[l]!!][encoding[r]!!]
}
