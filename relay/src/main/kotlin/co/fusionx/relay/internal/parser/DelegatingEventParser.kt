package co.fusionx.relay.internal.parser

import co.fusionx.irc.message.CodeMessage
import co.fusionx.irc.message.CommandMessage
import co.fusionx.irc.message.Message
import co.fusionx.relay.*
import co.fusionx.relay.internal.parser.ext.CodeExtParser
import co.fusionx.relay.internal.parser.ext.CommandExtParser
import co.fusionx.relay.internal.parser.ext.ExtensionParsers
import rx.Observable
import rx.subjects.PublishSubject

class DelegatingEventParser(private val coreCommandParser: EventParser<CommandMessage>,
                            private val coreCodeParser: EventParser<CodeMessage>,
                            private val extCommandParsers: Observable<CommandExtParser>,
                            private val extCodeParsers: Observable<CodeExtParser>) {

    fun parse(message: Message): Observable<Event> = when (message) {
        is CommandMessage -> parseCommand(message)
        is CodeMessage -> parseCode(message)
        else -> throw IllegalArgumentException("Should NEVER occur")
    }

    private fun parseCommand(message: CommandMessage): Observable<Event> = extCommandParsers
        /* Get an extension parser which might be able to parse the message */
        .filter { it.canParse(message) }
        .map<EventParser<CommandMessage>> { it }
        .firstOrDefault(coreCommandParser)
        .concatMap { it.parse(message) }

    private fun parseCode(message: CodeMessage): Observable<Event> = extCodeParsers
        /* Get an extension parser which might be able to parse the message */
        .filter { it.canParse(message) }
        .map<EventParser<CodeMessage>> { it }
        .firstOrDefault(coreCodeParser)
        .concatMap { it.parse(message) }

    companion object {
        fun create(creationHooks: AtomCreationHooks,
                   session: Session,
                   events: Observable<Event>,
                   output: PublishSubject<Message>,
                   channels: ChannelTracker,
                   users: UserTracker): DelegatingEventParser {
            val command = CoreCommandParser.create(creationHooks, events, output, channels, users)
            val code = CoreCodeParser.create(creationHooks, events, channels, users)
            val extCommands = ExtensionParsers.commandParsers(creationHooks, session, events,
                output, channels, users)
            val extCodes = ExtensionParsers.codeParsers()

            return DelegatingEventParser(command, code, extCommands, extCodes)
        }
    }
}