package co.fusionx.relay

interface ConnectionConfiguration {
    val hostname: String
    val port: Int
}

interface UserConfiguration {
    val nick: String
    val username: String
    val realName: String
}

public object Configurations {
    public fun connection(init: ConnectionConfigurationBuilder.() -> Unit): ConnectionConfiguration {
        val builder = ConnectionConfigurationBuilder()
        builder.init()
        return builder.build()
    }

    public fun user(init: UserConfigurationBuilder.() -> Unit): UserConfiguration {
        val builder = UserConfigurationBuilder()
        builder.init()
        return builder.build()
    }
}

public class ConnectionConfigurationBuilder internal constructor() : ConnectionConfiguration {
    override var hostname: String = ""
    override var port: Int = -1
}

public class UserConfigurationBuilder internal constructor() : UserConfiguration {
    override var nick: String = ""
    override var username: String = ""
    override var realName: String = ""
}

private fun ConnectionConfigurationBuilder.build(): ConnectionConfiguration {
    if (hostname == "" || port < 0 || port > 65535) {
        throw IllegalArgumentException("Invalid value for connection information")
    }
    return this
}

private fun UserConfigurationBuilder.build(): UserConfiguration {
    if (nick == "") {
        throw IllegalArgumentException("Invalid value for user information")
    }
    return this
}