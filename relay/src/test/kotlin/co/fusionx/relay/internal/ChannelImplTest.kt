package co.fusionx.relay.internal

import co.fusionx.irc.message.ClientMessageGenerator
import co.fusionx.irc.message.Message
import co.fusionx.relay.Event
import co.fusionx.relay.test.assertThat
import rx.observers.TestSubscriber
import rx.subjects.PublishSubject
import org.junit.Test as test

public class ChannelImplTest {

    private val eventSource: PublishSubject<Event> = PublishSubject.create()
    private val outputSink: PublishSubject<Message> = PublishSubject.create()
    private val channel = ChannelImpl("#relay", eventSource, outputSink)

    private val messageSubscriber = TestSubscriber<Message>()

    public test fun testToStringIsName() {
        assertThat(channel.toString()).isEqualTo("#relay")
    }

    public test fun testPrivmsgCommand() {
        val messageString = "Test message"
        val message = ClientMessageGenerator.privmsg("#relay", messageString)

        outputSink.subscribe(messageSubscriber)
        channel.privmsg(messageString)

        messageSubscriber.assertValue(message)
        messageSubscriber.assertNoErrors()
    }
}