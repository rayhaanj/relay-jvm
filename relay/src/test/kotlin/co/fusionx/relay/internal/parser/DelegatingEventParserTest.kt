package co.fusionx.relay.internal.parser

import co.fusionx.irc.message.CodeMessage
import co.fusionx.irc.message.CodeMessageData
import co.fusionx.irc.message.CommandMessage
import co.fusionx.irc.message.CommandMessageData
import co.fusionx.relay.Event
import co.fusionx.relay.Session
import co.fusionx.relay.internal.parser.ext.CodeExtParser
import co.fusionx.relay.internal.parser.ext.CommandExtParser
import co.fusionx.relay.uninitialized
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import rx.Observable
import rx.observers.TestSubscriber
import org.junit.Before as befor
import org.junit.Before as before
import org.junit.Test as test

public class DelegatingEventParserTest {

    private val randomArgs = listOf("random argument")

    private val session = mock(javaClass<Session>())
    private val coreCommand = mock(javaClass<EventParser<CommandMessage>>())
    private val coreCode = mock(javaClass<EventParser<CodeMessage>>())
    private val extCommand = mock(javaClass<CommandExtParser>())
    private val extCode = mock(javaClass<CodeExtParser>())

    org.junit.Before fun init() {
        doReturn("test-capability").`when`(extCommand).capability
        doReturn("TESTCOMMAND").`when`(extCommand).command
    }

    org.junit.Test fun testCodeMessageDelegates() {
        val parser = DelegatingEventParser(session, coreCommand, coreCode, Observable.empty(), Observable.empty())

        val message = CodeMessageData(code = 100, target = "relay", arguments = randomArgs)
        parser.parse(message).subscribe(TestSubscriber())

        verify(coreCode).parse(message)
    }

    org.junit.Test fun testCommandMessageDelegates() {
        val parser = DelegatingEventParser(session, coreCommand, coreCode, Observable.empty(), Observable.empty())

        val message = CommandMessageData(command = "TESTCOMMAND", arguments = randomArgs)
        parser.parse(message).subscribe(TestSubscriber())

        verify(coreCommand).parse(message)
    }

    org.junit.Test fun testExtensionCommandMessageDelegates() {
        doReturn(setOf("test-capability")).`when`(session).capabilities
        doReturn(Observable.empty<Event>()).`when`(extCommand).parse(uninitialized())

        val parser = DelegatingEventParser(session, coreCommand, coreCode, Observable.just(extCommand), Observable.empty())

        val message = CommandMessageData(command = "TESTCOMMAND", arguments = randomArgs)
        parser.parse(message).subscribe(TestSubscriber())

        verify(extCommand).parse(message)
        verify(coreCommand, never()).parse(uninitialized())
    }

    org.junit.Test fun testExtensionCommandMessageWithoutCapDelegatesToCore() {
        val parser = DelegatingEventParser(session, coreCommand, coreCode, Observable.just(extCommand), Observable.empty())

        val message = CommandMessageData(command = "TESTCOMMAND", arguments = randomArgs)
        parser.parse(message).subscribe(TestSubscriber())

        verify(coreCommand).parse(message)
        verify(extCommand, never()).parse(uninitialized())
    }

    org.junit.Test fun testNotExtensionCommandMessageDelegatesToCore() {
        val parser = DelegatingEventParser(session, coreCommand, coreCode, Observable.just(extCommand), Observable.empty())

        val message = CommandMessageData(command = "ANOTHERCOMMAND", arguments = randomArgs)
        parser.parse(message).subscribe(TestSubscriber())

        verify(coreCommand).parse(message)
        verify(extCommand, never()).parse(uninitialized())
    }
}