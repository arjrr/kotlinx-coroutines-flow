import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

// Flows are cold https://kotlinlang.org/docs/flow.html#flows-are-cold
/**
 * Flows are cold streams similar to sequences — the code inside a flow builder does not run until the flow is collected.
 * This becomes clear in the following example: */

/**
 * This is a key reason the simple function (which returns a flow) is not marked with suspend modifier. The simple() call
 * itself returns quickly and does not wait for anything. The flow starts afresh every time it is collected and that is why we
 * see "Flow started" every time we call collect again. */

/**
 * Creates a cold flow from the given suspendable block. The flow being cold means that the block is called every time a
 * terminal operator is applied to the resulting flow. */

fun getIntFlow(): Flow<Int> = flow {
    println("Flow started")
    for (i in 1..3) {
        delay(100)
        emit(i)
    }
}

fun main() = runBlocking {
    println("Calling simple function...")
    val flow = getIntFlow()

    println("Calling collect...")
    flow.collect { value -> println(value) }

    println("Calling collect again...")
    flow.collect { value -> println(value) }
}
