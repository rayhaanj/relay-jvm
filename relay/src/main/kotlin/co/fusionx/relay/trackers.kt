package co.fusionx.relay

public interface ChannelTracker {
    internal fun channel(channelName: String): Channel?
}

public interface QueryTracker {
    internal fun query(nick: String): Query?
}

public interface UserTracker {
    internal val self: User

    internal fun user(nick: String): User?
}