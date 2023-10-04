import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

// SharedFlow (a hot Flow) https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-shared-flow/
/**
 * A hot Flow that shares emitted values among all its collectors in a broadcast fashion, so that all collectors get all
 * emitted values. A shared flow is called hot because its active instance exists independently of the presence of collectors.
 * This is opposed to a regular Flow, such as defined by the flow { ... } function, which is cold and is started separately for
 * each collector.
 *
 * Shared flow never completes. A call to Flow.collect on a shared flow never completes normally, and neither does a coroutine
 * started by the Flow.launchIn function. An active collector of a shared flow is called a subscriber.
 *
 * State flow is a shared flow
 * State flow is a special-purpose, high-performance, and efficient implementation of SharedFlow for the narrow, but widely
 * used case of sharing a state. See the SharedFlow documentation for the basic rules, constraints, and operators that are
 * applicable to all shared flows.
 *
 * State flow always has an initial value, replays one most recent value to new subscribers, does not buffer any more values,
 * but keeps the last emitted one, and does not support resetReplayCache. A state flow behaves identically to a shared flow when
 * it is created with the following parameters and the distinctUntilChanged operator is applied to it:
 */

class TickHandler(
    externalScope: CoroutineScope,
    private val tickIntervalMs: Long = 5000,
) {
    private var flowCounter = 0
    private var sharedFlowCounter = 0

    // Flow
    val latestNews: Flow<Int> = flow {
        while (true) {
            emit(flowCounter++) // Emits the result of the request to the flow
            delay(tickIntervalMs) // Suspends the coroutine for some time
        }
    }

    // SharedFlow
    private val _tickFlow = MutableSharedFlow<Int>()
    val tickFlow: SharedFlow<Int> = _tickFlow

    // StateFlow
    private val _uiState = MutableStateFlow(0)
    val uiState: StateFlow<Int> = _uiState // The UI collects from this StateFlow to get its state updates

    init {
        externalScope.launch {
            while (true) {
                _tickFlow.emit(sharedFlowCounter++)
                _uiState.update { count -> count + 1 }
                delay(tickIntervalMs)
            }
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun main(): Unit = runBlocking {
    // TickHandler instance
    val tickHandler = TickHandler(GlobalScope)

    // Concurrent coroutines to collect data from the Flow data holder
    launch {
        tickHandler.latestNews.collect { value -> println("Collect Flow: $value") }
    }
    launch {
        tickHandler.latestNews.collect { value -> println("Collect Flow: $value") }
    }

    // Concurrent coroutines to collect data from the SharedFlow data holder
    launch {
        tickHandler.tickFlow.collect { value -> println("Collect SharedFlow: $value") }
    }
    launch {
        tickHandler.uiState.collect { value -> println("Collect StateFlow: $value") }
    }

    // Concurrent coroutines to collect data from the StateFlow data holder
    launch {
        tickHandler.uiState.collect { value -> println("Collect StateFlow: $value") }
    }
    launch {
        tickHandler.tickFlow.collect { value -> println("Collect SharedFlow: $value") }
    }
}
