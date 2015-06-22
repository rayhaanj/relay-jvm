package co.fusionx.relay

public interface ConnectionConfiguration {
    val hostname: String
    val port: Int

    companion object {
        public fun create(
                init: ConnectionConfigurationBuilder.() -> Unit): ConnectionConfiguration {
            val builder = ConnectionConfigurationBuilder()
            builder.init()
            return builder.build()
        }
    }
}

public class AssertionHelper {
    public fun log(line: String) {
    }
}

public class ConnectionConfigurationBuilder : ConnectionConfiguration {
    override var hostname: String = ""
    override var port: Int = -1
}

private fun ConnectionConfigurationBuilder.build(): ConnectionConfiguration {
    if (hostname == "" || port < 0 || port > 65535) {
        throw IllegalArgumentException("Invalid value for connection information")
    }
    return this
}