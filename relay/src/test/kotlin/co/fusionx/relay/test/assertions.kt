package co.fusionx.relay.test

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.AbstractIterableAssert

public fun assertThat<T>(iterable: T): AbstractAssert<*, T> =
    org.assertj.core.api.Assertions.assertThat(iterable)
public fun assertThat<T>(iterable: Iterable<T>): AbstractIterableAssert<*, out Iterable<T>, T> =
    org.assertj.core.api.Assertions.assertThat(iterable)