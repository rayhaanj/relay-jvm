package co.fusionx.relay.internal.network

import co.fusionx.relay.ConnectionConfiguration
import co.fusionx.relay.Status
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import rx.Observable
import rx.subjects.PublishSubject

public class TCPNettyConnection private constructor(override val rawSource: PublishSubject<String>,
                                                      override val rawStatusSource: PublishSubject<Status>,
                                                      private val rawSink: Observable<String>,
                                                      private val connectConfig: ConnectionConfiguration) :
    NetworkConnection {

    private val clientBuilder = Bootstrap()
        .group(NioEventLoopGroup())
        .channel(javaClass<NioSocketChannel>())
        .handler(object : ChannelInitializer<SocketChannel>() {
            override fun initChannel(ch: SocketChannel) {
                ch.pipeline().addLast(LineBasedFrameDecoder(512))
                ch.pipeline().addLast(StringDecoder())
                ch.pipeline().addLast(StringEncoder())
                ch.pipeline().addLast(object : SimpleChannelInboundHandler<String>() {
                    override fun channelRead0(ctx: ChannelHandlerContext, msg: String) {
                        rawSource.onNext(msg)
                    }
                })
            }
        })

    private var channel: Channel? = null

    companion object {
        fun create(configuration: ConnectionConfiguration,
                   rawOutput: Observable<String>): NetworkConnection {
            /* Create the main three flows of data in the system */
            val input = PublishSubject.create<String>()
            val status = PublishSubject.create<Status>()

            return TCPNettyConnection(input, status, rawOutput, configuration)
        }
    }

    override synchronized fun connect() {
        if (channel != null) {
            /* TODO - this is an error */
        }

        val future = clientBuilder.connect(connectConfig.hostname, connectConfig.port)
        channel = future.channel()

        future.addListener(object : ChannelFutureListener {
            override fun operationComplete(completedFuture: ChannelFuture) {
                /* Create a link between output stream and connection */
                rawSink.subscribe {
                    val localChannel = channel
                    if (localChannel != null && localChannel.isOpen()) {
                        localChannel.writeAndFlush(it + "\r\n")
                    } else {
                        /* TODO - this is an error */
                    }
                }

                /* Report the status */
                rawStatusSource.onNext(Status.SOCKET_CONNECTED)
            }
        })
    }

    override fun disconnect() {


        channel.disconnect()
    }
}