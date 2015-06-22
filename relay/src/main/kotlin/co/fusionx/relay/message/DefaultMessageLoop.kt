package co.fusionx.relay.message

import co.fusionx.relay.message
import java.util.concurrent.BlockingQueue
import kotlin.properties.Delegates

public class DefaultMessageLoop(
        private val queue: BlockingQueue<DefaultMessageLoop.Message>) : MessageLoop {

    private val thread = Thread { run() }
    private var messageHandler by Delegates.notNull<message.MessageHandler>()

    private val poolLock = Object()
    private var pool: Message? = null
    private var poolSize = 0

    public override fun start(messageHandler: message.MessageHandler) {
        this.messageHandler = messageHandler
        thread.start()
    }

    public override fun isOnLoop(): Boolean = thread.id == Thread.currentThread().id

    override fun post(type: Int, obj: Any?) {
        queue.put(obtain(type, obj))
    }

    private fun run() {
        while (true) {
            // If message is null here something terrible must have happened.
            val message = queue.take() ?: continue
            val type = message.type
            val obj = message.obj
            release(message)

            messageHandler.handle(type, obj)
        }
    }

    private fun obtain(type: Int, obj: Any?): Message {
        synchronized(poolLock) {
            if (pool == null) {
                return Message()
            }
            val message = pool!!
            pool = message.next
            message.next = null

            message.type = type
            message.obj = obj

            return message
        }
    }

    private fun release(message: Message) {
        synchronized(poolLock) {
            if (poolSize < POOL_SIZE) {
                message.reset()

                message.next = pool
                pool = message
                poolSize++
            }
        }
    }

    data class Message {
        var type: Int = -1
        var obj: Any? = null

        var next: Message? = null

        fun reset() {
            type = -1
            obj = null
        }
    }

    companion object {
        private val POOL_SIZE = 50
    }
}