package co.fusionx.relay.internal.parser.ext

import co.fusionx.irc.message.CodeMessage
import co.fusionx.irc.message.CommandMessage
import co.fusionx.irc.message.Message
import co.fusionx.relay.ChannelTracker
import co.fusionx.relay.Event
import co.fusionx.relay.Session
import co.fusionx.relay.UserTracker
import co.fusionx.relay.internal.parser.EventParser
import rx.Observable
import rx.subjects.PublishSubject

interface CommandExtParser : EventParser<CommandMessage> {
    public fun canParse(message: CommandMessage): Boolean
}

interface CodeExtParser : EventParser<CodeMessage> {
    public fun canParse(message: CodeMessage): Boolean
}

public object ExtensionParsers {
    public fun commandParsers(session: Session,
                              eventSource: Observable<Event>,
                              outputSink: PublishSubject<Message>,
                              channelTracker: ChannelTracker,
                              userTracker: UserTracker): Observable<CommandExtParser> = Observable.defer {
        Observable.just(
            AccountNotifyParser(session, channelTracker, userTracker),
            AwayNotifyParser(session, eventSource, outputSink, channelTracker, userTracker),
            ExtendedJoinParser(session, eventSource, outputSink, channelTracker, userTracker)
        )
    }

    public fun codeParsers(): Observable<CodeExtParser> = Observable.defer {
        Observable.empty<CodeExtParser>()
    }
}