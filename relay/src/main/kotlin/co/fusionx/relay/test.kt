package co.fusionx.relay

public fun main(args: Array<String>) {
    val connector = Relay.create()
    val connectionConfig = Configurations.connection {
        hostname = "irc.freenode.net"
        port = 6667
    }
    val userConfig = Configurations.user {
        username = "tilal6992"
        nick = "tilal6992"
        realName = "Lalit"
    }
    val client = connector.connect(connectionConfig, userConfig)
}