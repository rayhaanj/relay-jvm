package co.fusionx.relay.internal.event

import co.fusionx.irc.message.ClientMessageGenerator
import co.fusionx.irc.message.Message
import co.fusionx.relay.*
import co.fusionx.relay.internal.parser.ext.CommandExtParser
import rx.Observable
import rx.subjects.PublishSubject

public class CoreEventHandler(private val userConfig: UserConfiguration,
                              private val session: Session,
                              private val extCommandParsers: Observable<CommandExtParser>) {

    public fun handle(eventSource: Observable<Event>,
                      outputSink: PublishSubject<Message>) {
        /* Auto respond to pings with pongs */
        eventSource.ofType(javaClass<PingEvent>())
            .map { ClientMessageGenerator.pong(it.server) }
            .subscribe { outputSink.onNext(it) }

        /* Messages sent on initial connection */
        eventSource.ofType(javaClass<StatusEvent>())
            .filter { it.status == Status.SOCKET_CONNECTED }
            .concatMap {
                /* Send CAP LS, USER and NICK */
                Observable.just(
                    ClientMessageGenerator.cap(CapType.LS.asString),
                    ClientMessageGenerator.user(userConfig.username, userConfig.realName),
                    ClientMessageGenerator.nick(userConfig.nick)
                )
            }
            .subscribe { outputSink.onNext(it) }

        handleCap(eventSource, outputSink)
    }

    private fun handleCap(eventSource: Observable<Event>,
                          outputSink: PublishSubject<Message>) {
        /* Create the cap stream */
        val capStream = eventSource.ofType(javaClass<CapEvent>())
            /* only pass through the event if we are in the middle of registration */
            .withLatestFrom(session.status) { x, y -> Pair(x, y) }
            .filter { it.second == Status.SOCKET_CONNECTED }
            .map { it.first }
            /* SHare the stream */
            .share()

        /* If we have a LS then */
        capStream.filter { it.capType == CapType.LS }
            /* map the caps to strings */
            .map { it.capabilities.map { it.toString() } }
            /* filter out any empty CAP lists */
            .filter { it.isNotEmpty() }
            /* package up the cap list into a message */
            .map { ClientMessageGenerator.cap(CapType.REQ.asString, it) }
            /* send a CAP END message if there is no CAPs we can request */
            .defaultIfEmpty(ClientMessageGenerator.cap(CapType.END.asString))
            .subscribe { outputSink.onNext(it) }

        /* If we have a NAK then */
        capStream.filter { it.capType == CapType.NAK }
            /* send a CAP END message */
            .map { ClientMessageGenerator.cap(CapType.END.asString) }
            .subscribe { outputSink.onNext(it) }

        /* If we have a ACK then */
        capStream.filter { it.capType == CapType.ACK }
            /* send a CAP END message */
            .map { ClientMessageGenerator.cap(CapType.END.asString) }
            .subscribe { outputSink.onNext(it) }
    }
}