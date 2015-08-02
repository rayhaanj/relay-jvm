package co.fusionx.relay.internal

import co.fusionx.relay.*
import co.fusionx.relay.test.assertThat
import org.junit.Test as test

public class ClientImplTest {

    private val connectionConfig = mock<ConnectionConfiguration>()
    private val userConfig = mock<UserConfiguration>()
    private val client: ClientImpl

    init {
        on(connectionConfig.hostname).thenReturn("irc.freenode.net")
        on(connectionConfig.port).thenReturn(6667)

        on(userConfig.nick).thenReturn("relay")
        on(userConfig.username).thenReturn("relay-username")
        on(userConfig.realName).thenReturn("relay-realname")

        client = ClientImpl(DefaultHooks, connectionConfig, userConfig)
    }

    public test fun testClientWorks() {
        assertThat(client.channelTracker).isNotNull()
        assertThat(client.server).isNotNull()
        assertThat(client.session).isNotNull()
    }
}