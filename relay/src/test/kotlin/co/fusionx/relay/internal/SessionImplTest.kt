package co.fusionx.relay.internal

import co.fusionx.irc.message.Message
import co.fusionx.relay.*
import co.fusionx.relay.test.assertThat
import rx.observers.TestSubscriber
import rx.subjects.PublishSubject
import org.junit.Test as test

public class SessionImplTest {

    private val eventSource: PublishSubject<Event> = PublishSubject.create()
    private val outputSink: PublishSubject<Message> = PublishSubject.create()
    private val session = SessionImpl(eventSource, outputSink)

    private val statusSubscriber = TestSubscriber<Status>()
    private val eventSubscriber = TestSubscriber<Event>()
    private val messageSubscriber = TestSubscriber<Message>()

    public test fun testDefaultStatus() {
        session.status.subscribe(statusSubscriber)

        statusSubscriber.assertValue(Status.DISCONNECTED)
        statusSubscriber.assertNoErrors()
    }

    public test fun testStatusEventsPropagate() {
        eventSource.onNext(StatusEvent(Status.SOCKET_CONNECTED))
        session.status.subscribe(statusSubscriber)

        eventSource.onNext(StatusEvent(Status.CONNECTED))
        eventSource.onNext(StatusEvent(Status.DISCONNECTED))

        statusSubscriber.assertValues(Status.SOCKET_CONNECTED, Status.CONNECTED, Status.DISCONNECTED)
        statusSubscriber.assertNoErrors()
    }

    public test fun testSingleCapAckEvent() {
        val capability = Capability("sasl")
        eventSource.onNext(CapEvent(CapType.ACK, true, listOf(capability)))

        assertThat(session.capabilities).contains(capability)
    }

    public test fun testMultiLineCaps() {
        val sasl = Capability("sasl")
        eventSource.onNext(CapEvent(CapType.ACK, false, listOf(sasl)))
        assertThat(session.capabilities).isEmpty()

        val capNotify = Capability("cap-notify")
        eventSource.onNext(CapEvent(CapType.ACK, true, listOf(capNotify)))

        assertThat(session.capabilities).containsOnly(sasl, capNotify)
    }
}