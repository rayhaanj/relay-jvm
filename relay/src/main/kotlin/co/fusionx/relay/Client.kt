package co.fusionx.relay

/**
 * An entry-point which aggregates different IRC subsystems into one unified interface.
 */
public interface Client {
    public val server: Server
    public val session: Session
    public val channelTracker: ChannelTracker
}