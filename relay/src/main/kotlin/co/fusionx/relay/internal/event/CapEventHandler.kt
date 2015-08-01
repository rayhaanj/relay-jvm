package co.fusionx.relay.internal.event

import co.fusionx.irc.message.ClientMessageGenerator
import co.fusionx.irc.message.Message
import co.fusionx.relay.*
import rx.Observable
import rx.subjects.PublishSubject

public class CapEventHandler(private val session: Session) : EventHandler {

    public override fun handle(eventSource: Observable<Event>,
                               outputSink: PublishSubject<Message>) {
        val capStream = eventSource.ofType(javaClass<CapEvent>())
            /* only pass through the event if we are in the middle of registration */
            .withLatestFrom(session.status) { x, y -> Pair(x, y) }
            .filter { it.second == Status.SOCKET_CONNECTED }
            .map { it.first }
            .share()

        capStream.filter { it.capType == CapType.LS }
            .filter { it.capabilities.isNotEmpty() }
            .map { it.capabilities.map { it.toString() } }
            .map { ClientMessageGenerator.cap(CapType.REQ.asString, it) }
            .defaultIfEmpty(ClientMessageGenerator.cap(CapType.END.asString))
            .subscribe { outputSink.onNext(it) }

        capStream.filter { it.capType == CapType.NAK }
            .map { ClientMessageGenerator.cap(CapType.END.asString) }
            .subscribe { outputSink.onNext(it) }

        capStream.filter { it.capType == CapType.ACK }
            .map { ClientMessageGenerator.cap(CapType.END.asString) }
            .subscribe { outputSink.onNext(it) }
    }
}