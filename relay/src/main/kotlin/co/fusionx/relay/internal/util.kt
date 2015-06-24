package co.fusionx.relay.internal

import co.fusionx.relay.CapType
import co.fusionx.relay.Capability
import co.fusionx.relay.UserLevel
import rx.Observable

private val CHANNEL_FIRST_CHARACTERS = setOf('&', '#', '+', '!')

internal fun UserLevel.Companion.parse(char: Char): UserLevel? =
    UserLevel.values()
        .filter { it.char == char.toString() }
        .firstOrNull()

internal fun String.isChannel(): Boolean = CHANNEL_FIRST_CHARACTERS.contains(charAt(0))

internal fun Capability.Companion.parse(cap: String): Capability {
    val split = cap.split('=')
    return Capability(split[0], split.getOrNull(1))
}

internal fun CapType.Companion.parse(capString: String): CapType? =
    CapType.values()
        .filter { it.asString == capString }
        .firstOrNull()

internal fun <E : Any> List<E>.getOrNull(index: Int): E? = if (index < size()) this[index] else null