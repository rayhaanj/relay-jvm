package co.fusionx.relay.internal.parser

import co.fusionx.irc.message.CodeMessage
import co.fusionx.irc.message.CodeMessageData
import co.fusionx.irc.message.CommandMessage
import co.fusionx.irc.message.CommandMessageData
import co.fusionx.relay.internal.parser.ext.CodeExtParser
import co.fusionx.relay.internal.parser.ext.CommandExtParser
import co.fusionx.relay.mock
import co.fusionx.relay.uninitialized
import org.mockito.Mockito.on
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import rx.Observable
import rx.observers.TestSubscriber
import org.junit.Before as before
import org.junit.Test as test

public class DelegatingEventParserTest {

    private val randomArgs = listOf("random argument")

    private val coreCommand = mock<EventParser<CommandMessage>>()
    private val coreCode = mock<EventParser<CodeMessage>>()
    private val extCommand = mock<CommandExtParser>()
    private val extCode = mock<CodeExtParser>()

    test fun testCodeMessageDelegates() {
        val parser = DelegatingEventParser(coreCommand, coreCode, Observable.empty(), Observable.empty())

        val message = CodeMessageData(code = 100, target = "relay", arguments = randomArgs)
        parser.parse(message).subscribe(TestSubscriber())

        verify(coreCode).parse(message)
    }

    test fun testCommandMessageDelegates() {
        val parser = DelegatingEventParser(coreCommand, coreCode, Observable.empty(), Observable.empty())

        val message = CommandMessageData(command = "TESTCOMMAND", arguments = randomArgs)
        parser.parse(message).subscribe(TestSubscriber())

        verify(coreCommand).parse(message)
    }

    test fun testExtensionCommandMessageDelegates() {
        val message = CommandMessageData(command = "TESTCOMMAND", arguments = randomArgs)
        on(extCommand.canParse(message)).thenReturn(true)

        val parser = DelegatingEventParser(coreCommand, coreCode, Observable.just(extCommand), Observable.empty())
        parser.parse(message).subscribe(TestSubscriber())

        verify(extCommand).parse(message)
        verify(coreCommand, never()).parse(uninitialized())
    }

    test fun testNotExtensionCommandDelegatesToCore() {
        val message = CommandMessageData(command = "ANOTHERCOMMAND", arguments = randomArgs)
        on(extCommand.canParse(message)).thenReturn(false)

        val parser = DelegatingEventParser(coreCommand, coreCode, Observable.just(extCommand), Observable.empty())
        parser.parse(message).subscribe(TestSubscriber())

        verify(coreCommand).parse(message)
        verify(extCommand, never()).parse(uninitialized())
    }
}