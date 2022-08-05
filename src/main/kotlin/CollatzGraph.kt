import java.util.Stack
import kotlin.math.max

/**
 * The Collatz conjecture or `3n+1` problem states that you can take any positive number and apply the following:
 * If the number is even, then divide it by 2, otherwise tripple it and add 1.
 * Repeating this process, you get a sequence of numbers which eventually will reach 1. Or won't it?
 * If you know the answer and can prove it, congratulations, you are now famous.
 *
 * This class assumes that each sequence will eventually reach 1 and it constructs a directed acyclic graph to take
 * into account numbers that were previously calculated.
 * You can look at [this image](https://xkcd.com/710) to see how the graph is constructed.
 *
 * This class maintains both the graph and the list of numbers (node ids) in the graph.
 * Use [addNodes] or [addNode] to add additional Collatz sequences to the graph.
 */
class CollatzGraph {
    private val root = Node(1)
    private val nodes = mutableMapOf(1L to root)
    private var maxNode = 1L

    /**
     * Returns the largest number of the graph.
     */
    fun maxNodeValue(): Double {
        return maxNode.toDouble()
    }

    /**
     *  Adds all [numbers] into the graph and calculates their sequences.
     *  After each inserted node, [progressUpdate] is called with the progress.
     *
     *  @param numbers List of numbers to insert into the graph
     *  @param progressUpdate Update callback that returns a number between 0.0 and 1.0 that represents the
     *  progress from start to finish
     */
    fun addNodes(numbers: List<Long>, progressUpdate: (Double) -> Unit) {
        val length = numbers.size.toDouble() - 1.0
        var iter = 0
        numbers.forEach { addNode(it); progressUpdate((iter++ / length)) }
    }

    /**
     * Adds all [numbers] to the graph and calculates their sequences.
     */
    fun addNodes(numbers: List<Long>) {
        numbers.forEach { addNode(it) }
    }

    /**
     * Add [number] to the graph and calculates its sequences.
     */
    private fun addNode(number: Long) {
        if (number < 1) {
            throw RuntimeException("A Collatz sequence can only contain positive integers.")
        }
        if (nodes.contains(number)) {
            return
        }
        val seq = calculateSequence(number).onEach {
            nodes.putIfAbsent(it, Node(it))
        }
        var initialDepth = nodes[seq[0]]?.depth ?: return
        for (i in 0 until seq.size - 1) {
            val n1 = nodes[seq[i]] ?: throw RuntimeException("Node 1 is null. Should never happen.")
            val n2 = nodes[seq[i + 1]] ?: throw RuntimeException("Node 2 is null. Should never happen.")
            n1.children.add(n2)
            n2.parent = n1
            n2.depth = ++initialDepth
        }
        maxNode = max(maxNode, initialDepth.toLong())
    }

    /**
     * Visits each node of the graph in a breadth-first manner.
     *
     * @param nodeFunc function to call on each inner node
     * @param leafFunc function to call on each leaf node
     */
    fun visitGraph(nodeFunc: (Node) -> Unit, leafFunc: ((Node) -> Unit) = nodeFunc) {
        val s = Stack<Node>()
        s.push(root)
        while (!s.empty()) {
            val n = s.pop()
            if (n != null) {
                if (n.children.isEmpty()) {
                    leafFunc(n)
                } else {
                    nodeFunc(n)
                    n.children.forEach { s.push(it) }
                }
            }
        }
    }

    /**
     * Helper function to calculate the Collatz sequence of a number down until a number that was already calculated.
     * This only calculates the numbers (node IDs) and does not insert the corresponding nodes into the graph.
     */
    private fun calculateSequence(number: Long): List<Long> {
        val result = ArrayDeque<Long>()
        var n = number
        while (!nodes.contains(n)) {
            result.addFirst(n)
            n = if (n % 2L == 0L) {
                n / 2
            } else {
                3 * n + 1
            }
        }
        // We add the number n that already exists in the graph as well,
        // because we need to establish the parent/child edges to the existing nodes later.
        result.addFirst(n)
        return result
    }
}

/**
 * Represents the nodes of a Collatz graph.
 * We keep track of the node's [parent] and its [children].
 * For later visualization purposes, we also keep track of [depth] which reflects the depth of the node within the
 * whole graph.
 */
class Node(val id: Long) {
    var depth = 0
    var parent: Node = NULL_NODE
    val children = mutableListOf<Node>()

    companion object {
        val NULL_NODE = Node(0)
    }

}