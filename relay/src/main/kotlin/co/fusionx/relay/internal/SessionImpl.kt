package co.fusionx.relay.internal

import co.fusionx.irc.message.ClientMessageGenerator
import co.fusionx.irc.message.Message
import co.fusionx.relay.*
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.HashSet

public class SessionImpl(override val eventSource: Observable<Event>,
                         val outputSink: PublishSubject<Message>) : Session {

    override val status: BehaviorSubject<Status> = BehaviorSubject.create(Status.DISCONNECTED)
    override internal val capabilities: MutableSet<Capability> = HashSet()

    init {
        eventSource.ofType(javaClass<StatusEvent>())
            .subscribe { status.onNext(it.status) }

        /* Store caps in a local buffer until we hit a last line event and then flush to global */
        val localBuffer: MutableSet<Capability> = HashSet()
        eventSource.ofType(javaClass<CapEvent>())
            .filter { it.capType == CapType.ACK }
            .subscribe {
                if (it.lastLine) {
                    capabilities.addAll(localBuffer)
                    capabilities.addAll(it.capabilities)

                    localBuffer.clear()
                } else {
                    localBuffer.addAll(it.capabilities)
                }
            }
    }

    override fun join(channelName: String) =
        outputSink.onNext(ClientMessageGenerator.join(channelName))
}