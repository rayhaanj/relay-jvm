package co.fusionx.relay.internal.parser.ext

import co.fusionx.irc.message.CodeMessage
import co.fusionx.irc.message.CommandMessage
import co.fusionx.irc.message.Message
import co.fusionx.relay.ChannelTracker
import co.fusionx.relay.Event
import co.fusionx.relay.UserTracker
import co.fusionx.relay.internal.parser.EventParser
import rx.Observable
import rx.subjects.PublishSubject

interface CommandExtParser : EventParser<CommandMessage> {
    val capability: String
    val command: String
}

interface CodeExtParser : EventParser<CodeMessage> {
    val capability: String
    val code: Int
}

public object ExtensionParsers {
    public fun commandParsers(eventSource: Observable<Event>,
                              outputStream: PublishSubject<Message>,
                              channelTracker: ChannelTracker,
                              userTracker: UserTracker): Observable<CommandExtParser> = Observable.defer {
        Observable.just(
            AccountNotifyParser(eventSource, outputStream, channelTracker, userTracker),
            AwayNotifyParser(eventSource, outputStream, channelTracker, userTracker),
            ExtendedJoinParser(eventSource, outputStream, channelTracker, userTracker)
        )
    }

    public fun codeParsers(): Observable<CodeExtParser> = Observable.defer {
        Observable.empty<CodeExtParser>()
    }
}