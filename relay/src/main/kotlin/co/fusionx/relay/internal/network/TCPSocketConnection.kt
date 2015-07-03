package co.fusionx.relay.internal.network

import co.fusionx.relay.ConnectionConfiguration
import co.fusionx.relay.Status
import okio.Okio
import rx.Observable
import rx.subjects.PublishSubject
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

public class TCPSocketConnection private constructor(override val rawSource: PublishSubject<String>,
                                                     override val rawStatusSource: PublishSubject<Status>,
                                                     private val rawSink: Observable<String>,
                                                     private val connectConfig: ConnectionConfiguration) :
    NetworkConnection {

    /* Socket to connect to */
    private val socket = Socket()

    /* Stores whether this connected is invalid */
    private val atomicInvalid = AtomicBoolean(false)

    /* Public value of whether this connection is invalid or not */
    override val invalid: Boolean
        get() = atomicInvalid.get()

    override fun connect() {
        try {
            socket.connect(InetSocketAddress(connectConfig.hostname, connectConfig.port))
        } catch (ex: IOException) {
            /* TODO - error handling here */
            atomicInvalid.set(true)
            return
        }

        /* Create the sink and source */
        val sink = Okio.buffer(Okio.sink(socket))
        val source = Okio.buffer(Okio.source(socket))

        /* Create a link between output stream and connection */
        val sinkSubscription = rawSink.subscribe {
            /* Write the bytes and flush them */
            sink.writeUtf8(it + "\r\n")
            sink.flush()
        }

        /* Report the status */
        rawStatusSource.onNext(Status.SOCKET_CONNECTED)

        /* Start the source reading loop */
        var line = source.readUtf8Line()
        while (line != null) {
            rawSource.onNext(line)
            line = source.readUtf8Line()
        }

        /* Unsubscribe from the sink */
        sinkSubscription.unsubscribe()

        /* Invalidate this connection */
        atomicInvalid.set(true)
    }

    companion object {
        fun create(configuration: ConnectionConfiguration,
                   rawOutput: Observable<String>): NetworkConnection {
            /* Create the main three flows of data in the system */
            val input = PublishSubject.create<String>()
            val status = PublishSubject.create<Status>()

            return TCPSocketConnection(input, status, rawOutput, configuration)
        }
    }
}