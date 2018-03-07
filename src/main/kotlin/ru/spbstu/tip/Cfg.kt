package ru.spbstu.tip

import ru.spbstu.tip.ast.Expr
import ru.spbstu.tip.ast.Function
import ru.spbstu.tip.ast.Stmt
import ru.spbstu.tip.ast.Variable
import ru.spbstu.tip.lattice.Dependencies
import ru.spbstu.tip.lattice.WithDomain
import java.util.concurrent.atomic.AtomicInteger

class CfgFragment(
        val nodes: List<CfgNode> = listOf(),
        val edgesForward: Map<CfgNode, List<CfgNode>> = mapOf()
): WithDomain<CfgNode>, Dependencies<CfgNode> {
    val edgesBackward: Map<CfgNode, List<CfgNode>> =
            edgesForward
                    .asSequence()
                    .flatMap { (k, v) -> v.asSequence().map { it to k } }
                    .groupBy ({ it.first }, { it.second })

    override val domain get() = nodes
    override fun incoming(e: CfgNode)= edgesForward[e] ?: listOf()
    override fun outgoing(e: CfgNode) = edgesBackward[e] ?: listOf()
}

private var currentId: AtomicInteger = AtomicInteger(0)
internal fun freshId() : Int { return currentId.getAndIncrement() }

sealed class CfgNode(val id: Int = freshId()) {
    override fun hashCode(): Int = id.hashCode()
    override fun equals(other: Any?) = when(other) {
        is CfgNode -> id == other.id
        else -> false
    }
}
class VarDeclNode(val variable: Variable): CfgNode() {
    override fun toString() = "[var ${variable.name}]"
}
class FlowNode(val stmt: Stmt): CfgNode() {
    override fun toString() = "[${stmt.pprint()}]"
}
class BranchNode(val condition: Expr): CfgNode() {
    override fun toString() = "[CONDITION(${condition.pprint()})]"
}
class ReturnNode(val expr: Expr): CfgNode() {
    override fun toString() = "[return ${expr.pprint()}]"
}
class FunEntryNode(val func: Function): CfgNode() {
    override fun toString() = "[<entry> ${func.name}]"
}
class FunExitNode(val func: Function): CfgNode() {
    override fun toString() = "[<exit> ${func.name}]"
}
