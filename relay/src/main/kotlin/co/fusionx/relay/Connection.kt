package co.fusionx.relay

import co.fusionx.relay.message.MessageLoop
import co.fusionx.relay.message.MessageType
import okio.Okio
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

public class Connection constructor(
        private val connectConfig: ConnectionConfiguration,
        private val messageLoop: MessageLoop) {

    private val socket = Socket()

    public fun connect() {
        try {
            socket.connect(InetSocketAddress(connectConfig.hostname, connectConfig.port))
            val source = Okio.buffer(Okio.source(socket))
            val sink = Okio.buffer(Okio.sink(socket))
            messageLoop.post(MessageType.SOCKET_CONNECT, sink)

            Thread {
                try {
                    var line = source.readUtf8Line()
                    while (line != null) {
                        messageLoop.post(MessageType.SOCKET_READ, line)
                        line = source.readUtf8Line()
                    }
                } catch (ex: IOException) {
                    messageLoop.post(MessageType.DISCONNECT)
                }
            }.start()
        } catch (ex: IOException) {
            messageLoop.post(MessageType.DISCONNECT)
        }
    }
}