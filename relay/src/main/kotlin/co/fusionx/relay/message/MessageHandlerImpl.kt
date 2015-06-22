package co.fusionx.relay.message

import co.fusionx.relay.Connection
import co.fusionx.relay.parser.PlainParser
import nl.komponents.kovenant.Deferred
import okio.BufferedSink
import java.io.IOException

public class MessageHandlerImpl(private val parser: PlainParser) : MessageHandler {

    private var bufferedSink: BufferedSink? = null

    override fun handle(type: Int, obj: Any?) {
        when (type) {
            MessageType.STATUS_REQUEST -> onStatusRequest(obj as Deferred<Boolean, Exception>)

            MessageType.INITIAL -> onInitial(obj as Connection)
            MessageType.SOCKET_CONNECT -> onSocketConnect(obj as BufferedSink)

            MessageType.SOCKET_READ -> parser.parse(obj as String)
            MessageType.SOCKET_WRITE -> onSocketWrite(obj as String)
        }
    }

    private fun onStatusRequest(deferred: Deferred<Boolean, Exception>) {
        deferred.resolve(bufferedSink == null)
    }

    private fun onInitial(networkConnection: Connection) {
        networkConnection.connect()
    }

    private fun onSocketConnect(bufferedSink: BufferedSink) {
        this.bufferedSink = bufferedSink
    }

    private fun onSocketWrite(line: String) {
        try {
            bufferedSink?.writeUtf8("$line\r\n")
        } catch (ex: IOException) {
            // Something bad has happened. There will probably be a DISCONNECT event very soon.
            // TODO(tilal6991) - investigate if this should be handled.
        }
    }
}