import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect

/**
 * SharedFlow is an example of hot Flow. A hot Flow that shares emitted values among all its collectors in a broadcast fashion,
 * so that all collectors get all emitted values. A shared flow is called hot because its active instance exists independently
 * of the presence of collectors. This is opposed to a regular Flow, such as defined by the flow { ... } function, which is cold
 * and is started separately for each collector.
 */

class EventBus(
    externalScope: CoroutineScope,
    private val tickIntervalMs: Long = 5000,
) {
    private var counter = 0

    private val _events = MutableSharedFlow<Int>() // private mutable shared flow
    val events = _events.asSharedFlow() // publicly exposed as read-only shared flow

    init {
        externalScope.launch {
            while (true) {
                _events.emit(value = counter++) // suspends until all subscribers receive it
                delay(tickIntervalMs)
            }
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun main(): Unit = runBlocking {
    val eventBus = EventBus(GlobalScope)

    launch {
        eventBus.events.collect { value -> println("Collected from SharedFlow: $value") }
    }
    eventBus.events.collect { value -> println("Collected from SharedFlow: $value") }
}
