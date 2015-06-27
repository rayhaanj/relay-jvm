package co.fusionx.relay

/** TODO */
interface Event

/** TODO */
interface SessionEvent : Event

/** TODO */
interface ServerEvent : Event

/** TODO */
interface ChannelEvent : Event {
    val channel: Channel
}

/** TODO */
interface UserEvent : Event {
    val user: User
}

/** TODO */
data class ServerGenericCodeEvent(val code: Int,
                                  val text: String) : ServerEvent

/** TODO */
data class ServerPrivmsgEvent(val sender: User?,
                              val senderNick: String,
                              val text: String) : ServerEvent

/** TODO */
data class ServerNoticeEvent(val sender: User?,
                             val senderNick: String,
                             val text: String) : ServerEvent

/**
 * TODO
 */
data class CapEvent(val capType: CapType,
                    val capabilities: List<Capability> = listOf()) : SessionEvent

/**
 * TODO
 */
data class PingEvent(val server: String) : SessionEvent

/**
 * TODO
 */
data class StatusEvent(val status: Status) : SessionEvent

/**
 * TODO
 */
data class JoinEvent(override val channel: Channel,
                     override val user: User) : SessionEvent, ChannelEvent, UserEvent

/**
 * Represents the [user] changing nick from [oldNick] to [newNick]
 *
 * NOTE: it is not deterministic whether the [user] has as their nick [oldNick] or [newNick]. Only after this event has
 * been fully dispatched will the [user] deterministically have newNick as their nick.
 */
data class NickEvent(override val user: User,
                     val oldNick: String,
                     val newNick: String) : SessionEvent, UserEvent

/**
 * TODO
 */
data class ChannelNickEvent(override val channel: Channel,
                            override val user: User,
                            val oldNick: String,
                            val newNick: String) : ChannelEvent, UserEvent

/**
 * TODO
 */
data class PartEvent(override val channel: Channel,
                     override val user: User,
                     val reason: String? = null) : SessionEvent, ChannelEvent, UserEvent

/**
 * TODO
 */
data class QuitEvent(override val user: User,
                     val reason: String? = null) : SessionEvent, UserEvent

/**
 * TODO
 */
data class ChannelQuitEvent(override val channel: Channel,
                            override val user: User,
                            val reason: String?) : ChannelEvent, UserEvent


/* Used specially inside Names (and eventually WHOIS) */
data class LevelledUser(val levels: List<UserLevel>, val user: User)

/**
 * TODO
 */
data class ChannelNamesReplyEvent(override val channel: Channel,
                                  val levelledUsers: List<LevelledUser>) : ChannelEvent

/**
 * TODO
 */
data class ChannelPrivmsgEvent(override val channel: Channel,
                               val sender: User?,
                               val senderNick: String,
                               val text: String) : ChannelEvent

/**
 * TODO
 */
data class ChannelNoticeEvent(override val channel: Channel,
                              val sender: User?,
                              val senderNick: String,
                              val text: String) : ChannelEvent