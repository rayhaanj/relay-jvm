package co.fusionx.relay.parser;

import java.util.Collections;
import java.util.List;

import co.fusionx.relay.util.StringUtils;

import static co.fusionx.relay.util.StringUtils.tokenizeOn;

public final class PlainParser {

    private final CommandParser mCommandParser;
    private final CodeParser mCodeParser;

    public PlainParser(CommandParser commandParser, CodeParser codeParser) {
        mCommandParser = commandParser;
        mCodeParser = codeParser;
    }

    public void parse(String input) {
        // Simply ignore an empty line as it is allowed by the RFC.
        if (input == null || input.isEmpty()) return;
        List<String> tokenized = tokenizeOn(input, ' ', false, true);

        // By reversing the list and parsing from the end forward, a lot of unnecessary copies are
        // avoided.
        Collections.reverse(tokenized);

        List<String> tags = getLast(tokenized).charAt(0) == '@' ?
                tokenizeOn(removeLast(tokenized), ';', false, false) : null;
        String prefix = getLast(tokenized).charAt(0) == ':' ? removeLast(tokenized) : null;
        String verb = removeLast(tokenized);

        int maybeCode = StringUtils.parseCode(verb);
        if (maybeCode == -1) {
            Collections.reverse(tokenized);
            mCommandParser.parseCommand(tags, prefix, verb, tokenized);
            return;
        }
        String target = removeLast(tokenized);
        Collections.reverse(tokenized);
        mCodeParser.parseCode(tags, prefix, maybeCode, target, tokenized);
    }

    private static String getLast(List<String> list) {
        return list.get(list.size() - 1);
    }

    private static String removeLast(List<String> list) {
        return list.remove(list.size() - 1);
    }
}