package co.fusionx.relay.internal.tcp.netty

import co.fusionx.relay.ConnectionConfiguration
import co.fusionx.relay.Status
import co.fusionx.relay.internal.Connection
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

public class NettyConnection private constructor(override val input: PublishSubject<String>,
                                                 private val rawOutput: Observable<String>,
                                                 override val status: PublishSubject<Status>,
                                                 val connectConfig: ConnectionConfiguration) : Connection {

    val clientBuilder = Bootstrap()
        .group(NioEventLoopGroup())
        .channel(javaClass<NioSocketChannel>())
        .handler(object : ChannelInitializer<SocketChannel>() {
            override fun initChannel(ch: SocketChannel) {
                ch.pipeline().addLast(LineBasedFrameDecoder(512))
                ch.pipeline().addLast(StringDecoder())
                ch.pipeline().addLast(StringEncoder())
                ch.pipeline().addLast(object : SimpleChannelInboundHandler<String>() {
                    override fun channelRead0(ctx: ChannelHandlerContext, msg: String) {
                        input.onNext(msg)
                    }
                })
            }
        })

    companion object {
        fun create(configuration: ConnectionConfiguration,
                   rawOutput: Observable<String>): Connection {
            /* Create the main three flows of data in the system */
            val input = PublishSubject.create<String>()
            val status = PublishSubject.create<Status>()

            return NettyConnection(input, rawOutput, status, configuration)
        }
    }

    override fun connect(): Observable<Connection> {
        return Observable.create<Connection> {
            val future = clientBuilder.connect(connectConfig.hostname, connectConfig.port)
            future.addListener(object : ChannelFutureListener {
                override fun operationComplete(completedFuture: ChannelFuture) {
                    /* Create a link between output stream and connection */
                    rawOutput.subscribe {
                        if (future.channel().isOpen()) {
                            future.channel().writeAndFlush(it + "\r\n")
                        } else {
                            /* TODO - this is an error */
                        }
                    }

                    /* Report the status */
                    status.onNext(Status.SOCKET_CONNECTED)
                }
            })

            it.onNext(this@NettyConnection)
        }.take(1).share()
    }

    override fun disconnect() {
        throw UnsupportedOperationException()
    }
}