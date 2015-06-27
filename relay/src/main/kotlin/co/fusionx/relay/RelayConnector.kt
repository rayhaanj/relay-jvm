package co.fusionx.relay

import co.fusionx.relay.internal.ClientImpl

public class RelayConnector private constructor() {

    companion object {
        public fun create(): RelayConnector = RelayConnector()
    }

    public fun connect(configuration: ConnectionConfiguration, userConfig: UserConfiguration): Client =
        ClientImpl.start(configuration, userConfig)
}