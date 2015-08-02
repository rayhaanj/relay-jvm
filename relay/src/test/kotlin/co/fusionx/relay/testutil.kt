package co.fusionx.relay

import org.mockito.Mockito
import rx.observers.TestSubscriber

public fun uninitialized<T>(): T = null as T

public inline fun mock<reified T>(): T = Mockito.mock(javaClass<T>())

public fun <T> TestSubscriber<T>.assertValuesCompletedNoErrors(vararg values: T) {
    assertValues(*values)
    assertCompleted()
    assertNoErrors()
}

public fun <T> TestSubscriber<T>.assertValuesNoErrors(vararg values: T) {
    assertValues(*values)
    assertNoErrors()
}