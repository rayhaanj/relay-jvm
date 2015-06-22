package co.fusionx.relay

import co.fusionx.relay.message.DefaultMessageLoop
import co.fusionx.relay.message.MessageHandler
import co.fusionx.relay.message.MessageHandlerImpl
import co.fusionx.relay.message.MessageLoop
import co.fusionx.relay.message.MessageType
import co.fusionx.relay.parser.CoreCodeParser
import co.fusionx.relay.parser.CoreCommandParser
import co.fusionx.relay.parser.PlainParser
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.deferred
import java.util.concurrent.LinkedBlockingQueue

public class RelayClient private constructor(
        private val messageLoop: MessageLoop,
        private val messageHandler: MessageHandler,
        private val networkConnection: Connection,
        private val dispatcher: EventDispatcher) {

    public val connected: Promise<Boolean, Exception>
        get() {
            val deferred = deferred<Boolean, Exception>()
            messageLoop.post(MessageType.STATUS_REQUEST, deferred)
            return deferred.promise
        }

    public fun start() {
        messageLoop.start(messageHandler)

        // Kick off processing on event loop thread.
        messageLoop.post(MessageType.INITIAL, networkConnection)
    }

    public fun send(line: String) {
        messageLoop.post(MessageType.SOCKET_WRITE, line)
    }

    public fun addEventListener(listener: EventListener) {
        dispatcher.addEventListener(listener)
    }

    public fun removeEventListener(listener: EventListener) {
        dispatcher.removeEventListener(listener)
    }

    companion object {
        @JvmOverloads public fun create(
                connection: ConnectionConfiguration,
                messageLoop: MessageLoop = DefaultMessageLoop(LinkedBlockingQueue()),
                assertion: AssertionHelper = AssertionHelper()): RelayClient {
            val dispatcher = EventDispatcher()

            val commandParser = CoreCommandParser()
            val codeParser = CoreCodeParser(dispatcher)
            val plainParser = PlainParser(commandParser, codeParser)

            val messageHandler = MessageHandlerImpl(plainParser)

            val networkConnection = Connection(connection, messageLoop)

            return RelayClient(messageLoop, messageHandler, networkConnection, dispatcher)
        }
    }
}