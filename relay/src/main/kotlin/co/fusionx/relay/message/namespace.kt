package co.fusionx.relay.message

public interface MessageLoop {
    public fun start(messageHandler: MessageHandler)
    public fun isOnLoop(): Boolean
    public fun post(type: Int, obj: Any? = null)
}

interface MessageHandler {
    public fun handle(type: Int, obj: Any?)
}

public object MessageType {
    public val STATUS_REQUEST: Int = 0

    public val INITIAL: Int = 1

    public val SOCKET_CONNECT: Int = 10
    public val DISCONNECT: Int = 11
    public val RECONNECT: Int = 12

    public val SOCKET_READ: Int = 100
    public val SOCKET_WRITE: Int = 101
}