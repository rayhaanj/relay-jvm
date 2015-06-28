package co.fusionx.relay

import co.fusionx.relay.internal.ClientImpl
import kotlin.platform.platformStatic

public class Relay private constructor(private val hooks: Hooks) {

    companion object {
        platformStatic jvmOverloads public fun create(hooks: Hooks = DefaultHooks): Relay = Relay(hooks)
    }

    public fun connect(configuration: ConnectionConfiguration, userConfig: UserConfiguration): Client =
        ClientImpl.start(hooks, configuration, userConfig)
}