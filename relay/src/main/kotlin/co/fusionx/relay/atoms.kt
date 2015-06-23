package co.fusionx.relay

import rx.Observable

public enum class UserLevel private constructor(val char: String) {
    OWNER("~"),
    SUPEROP("&"),
    OP("@"),
    HALFOP("%"),
    VOICE("+");

    companion object
}

public enum class Status {
    DISCONNECTED,
    SOCKET_CONNECTED,
    CONNECTED
}

/**
 * Represents an IRC user
 */
public interface User {
    public val nick: Observable<String>
    public val channels: Set<Channel>
}

private interface EventProducer<T> {
    public val eventStream: Observable<T>
}

/**
 * Represents an IRC session
 */
public interface Session : EventProducer<Event> {
    public val status: Observable<Status>
    public val capabilities: Set<Capability>

    public fun join(channelName: String)
}

/**
 * Represents an IRC server
 */
public interface Server : EventProducer<ServerEvent>

/**
 * Represents an IRC channel
 */
public interface Channel : EventProducer<ChannelEvent> {
    public val name: String
    public val users: Set<User>

    public fun privmsg(message: String)
}

/**
 * Represents an IRC query
 */
public interface Query