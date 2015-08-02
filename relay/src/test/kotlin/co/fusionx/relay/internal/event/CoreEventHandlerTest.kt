package co.fusionx.relay.internal.event

import co.fusionx.irc.message.ClientMessageGenerator
import co.fusionx.irc.message.Message
import co.fusionx.relay.*
import org.mockito.Mockito.*
import rx.observers.TestSubscriber
import rx.subjects.PublishSubject
import org.junit.Test as test

public class CoreEventHandlerTest {

    private val eventSource: PublishSubject<Event> = PublishSubject.create()
    private val outputSink: PublishSubject<Message> = PublishSubject.create()

    private val userConfiguration = mock<UserConfiguration>()
    private val coreEventHandler = CoreEventHandler(userConfiguration)

    private val messageSubscriber = TestSubscriber<Message>()

    init {
        coreEventHandler.handle(eventSource, outputSink)
    }

    public test fun testPingResponse() {
        outputSink.subscribe(messageSubscriber)
        eventSource.onNext(PingEvent("test.server.relay"))

        messageSubscriber.assertValuesNoErrors(ClientMessageGenerator.pong("test.server.relay"))
    }

    public test fun testStatusSocketConnectResponse() {
        `when`(userConfiguration.nick).thenReturn("relay")
        `when`(userConfiguration.realName).thenReturn("relay-realname")
        `when`(userConfiguration.username).thenReturn("relay-username")

        outputSink.subscribe(messageSubscriber)
        eventSource.onNext(StatusEvent(Status.SOCKET_CONNECTED))

        messageSubscriber.assertValuesNoErrors(
            ClientMessageGenerator.cap("LS"),
            ClientMessageGenerator.user("relay-username", "relay-realname"),
            ClientMessageGenerator.nick("relay")
        )
    }
}