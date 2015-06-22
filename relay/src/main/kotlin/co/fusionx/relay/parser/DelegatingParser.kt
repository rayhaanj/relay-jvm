package co.fusionx.relay.parser

internal class DelegatingParser constructor(
        private val coreCommandParser: CommandParser,
        private val coreCodeParser: CodeParser) : CommandParser, CodeParser {

    override fun parseCommand(tags: List<String>?, prefix: String?,  command: String,
            arguments: List<String>) {
        coreCommandParser.parseCommand(tags, prefix, command, arguments)
    }

    override fun parseCode(tags: List<String>?, prefix: String?, code: Int, target: String,
            arguments: List<String>) {
        coreCodeParser.parseCode(tags, prefix, code, target, arguments)
    }
}