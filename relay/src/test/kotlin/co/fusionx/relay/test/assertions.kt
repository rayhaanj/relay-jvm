package co.fusionx.relay.test

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.AbstractIterableAssert

public class ObjectAssert<T>(actual: T) : org.assertj.core.api.ObjectAssert<T>(actual)

public fun assertThat<T>(obj: T): AbstractAssert<*, T> = ObjectAssert<T>(obj)
public fun assertThat<T>(iterable: Iterable<T>): AbstractIterableAssert<*, out Iterable<T>, T> =
    org.assertj.core.api.Assertions.assertThat(iterable)