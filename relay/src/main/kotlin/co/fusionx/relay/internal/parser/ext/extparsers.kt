package co.fusionx.relay.internal.parser.ext

import co.fusionx.irc.message.CodeMessage
import co.fusionx.irc.message.CommandMessage
import co.fusionx.irc.message.Message
import co.fusionx.relay.*
import co.fusionx.relay.internal.parser.EventParser
import rx.Observable
import rx.subjects.PublishSubject
import java.util.concurrent.ExecutorService

interface CommandExtParser : EventParser<CommandMessage> {
    public val capabilities: Set<String>

    public fun canParse(message: CommandMessage): Boolean
}

interface CodeExtParser : EventParser<CodeMessage> {
    public fun canParse(message: CodeMessage): Boolean
}

public object ExtensionParsers {
    public fun commandParsers(atomCreationHooks: AtomCreationHooks,
                              session: Session,
                              eventSource: Observable<Event>,
                              outputSink: PublishSubject<Message>,
                              mainExecutor: ExecutorService,
                              channelTracker: ChannelTracker,
                              userTracker: UserTracker): Observable<CommandExtParser> =
        Observable.just(
            AccountNotifyParser(session, channelTracker, userTracker),
            AwayNotifyParser(session, eventSource, outputSink, channelTracker, userTracker),
            ExtendedJoinParser(atomCreationHooks, session, eventSource, outputSink, mainExecutor,
                channelTracker, userTracker)
        )

    public fun codeParsers(): Observable<CodeExtParser> = Observable.defer {
        Observable.empty<CodeExtParser>()
    }
}