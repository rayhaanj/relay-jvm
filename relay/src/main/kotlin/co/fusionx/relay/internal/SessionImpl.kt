package co.fusionx.relay.internal

import co.fusionx.irc.message.ClientMessageGenerator
import co.fusionx.irc.message.Message
import co.fusionx.relay.*
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.HashSet

public class SessionImpl(override val eventStream: Observable<Event>,
                         val outputStream: PublishSubject<Message>) : Session {

    override val status: BehaviorSubject<Status> = BehaviorSubject.create(Status.DISCONNECTED)
    override val capabilities: Set<Capability>
        get() = synchronized(internalCaps) { HashSet(internalCaps) }

    private val internalCaps: MutableSet<Capability> = HashSet()

    init {
        /* Update the status of the session */
        eventStream.ofType(javaClass<StatusEvent>())
            .subscribe { status.onNext(it.status) }

        /* For an ACK we need to add the ACKed caps into the list */
        eventStream.ofType(javaClass<CapEvent>())
            .filter { it.capType == CapType.ACK }
            .subscribe { synchronized(internalCaps) { internalCaps.addAll(it.capabilities) } }
    }

    override fun join(channelName: String) =
        outputStream.onNext(ClientMessageGenerator.join(channelName))
}