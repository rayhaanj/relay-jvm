package co.fusionx.relay

import rx.observers.TestSubscriber

public fun uninitialized<T>(): T = null as T

public fun <T> TestSubscriber<T>.assertValuesCompletedNoErrors(vararg values: T) {
    assertValues(*values)
    assertNoErrors()
    assertCompleted()
}

public fun <T> TestSubscriber<T>.assertValueCompletedNoErrors(value: T) {
    assertValue(value)
    assertNoErrors()
    assertCompleted()
}