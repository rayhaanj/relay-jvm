package co.fusionx.relay

data class Capability(val capabilityType: String, val value: String?) {
    override fun toString(): String = capabilityType + if (value == null) "" else "=${value}"

    companion object
}

enum class CapType private constructor(val asString: String) {
    LS("LS"),
    REQ("REQ"),
    ACK("ACK"),
    NAK("NAK"),
    END("END");

    companion object
}