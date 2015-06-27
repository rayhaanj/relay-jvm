package co.fusionx.relay.internal

import co.fusionx.irc.message.ClientMessageGenerator
import co.fusionx.irc.message.Message
import co.fusionx.relay.*
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.HashSet

public class SessionImpl(override val eventSource: Observable<Event>,
                         val outputStream: PublishSubject<Message>) : Session {

    override val status: BehaviorSubject<Status> = BehaviorSubject.create(Status.DISCONNECTED)
    override internal val capabilities: MutableSet<Capability> = HashSet()

    init {
        /* Update the status of the session */
        eventSource.ofType(javaClass<StatusEvent>())
            .subscribe { status.onNext(it.status) }

        /* For an ACK we need to add the ACKed caps into the list */
        eventSource.ofType(javaClass<CapEvent>())
            .filter { it.capType == CapType.ACK }
            .subscribe { capabilities.addAll(it.capabilities) }
    }

    override fun join(channelName: String) =
        outputStream.onNext(ClientMessageGenerator.join(channelName))
}