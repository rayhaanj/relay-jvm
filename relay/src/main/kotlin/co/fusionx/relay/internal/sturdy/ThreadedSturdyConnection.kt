package co.fusionx.relay.internal.sturdy

import co.fusionx.relay.internal.network.NetworkConnection

public class ThreadedSturdyConnection(private val network: NetworkConnection) : SturdyConnection {

    /* TODO add some double start checks in here */
    override fun start() {
        val thread: Thread = Thread { network.connect() }
        thread.start()
    }

    companion object {
        public fun create(networkConnection: NetworkConnection): SturdyConnection =
            ThreadedSturdyConnection(networkConnection)
    }
}