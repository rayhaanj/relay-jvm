package co.fusionx.relay.parser

public interface CommandParser {
    public fun parseCommand(tags: List<String>?, prefix: String?,  command: String,
            arguments: List<String>)
}

public interface CodeParser {
    public fun parseCode(tags: List<String>?, prefix: String?, code: Int, target: String,
            arguments: List<String>)
}