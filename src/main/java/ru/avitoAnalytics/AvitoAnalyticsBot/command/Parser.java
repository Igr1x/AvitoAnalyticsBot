package ru.avitoAnalytics.AvitoAnalyticsBot.command;

import org.springframework.stereotype.Component;

@Component
public class Parser {
    private final String PREFIX_FOR_COMMAND = "/";

    public ParsedCommand getParsedCommand(String text) {
        String trimText = "";
        if (text != null) trimText = text.trim();
        ParsedCommand result = new ParsedCommand(Command.NONE);
        if ("".equals(trimText)) return result;
        if (isCommand(trimText)) {
            Command command = getCommandFromText(trimText);
            result.setCommand(command);
        }
        return result;
    }

    private Command getCommandFromText(String text) {
        String upperCaseText = text.toUpperCase().trim();
        Command command = Command.NONE;
        try {
            command = Command.valueOf(upperCaseText.substring(1));
        } catch (IllegalArgumentException e) {

        }
        return command;
    }


    private boolean isCommand(String text) {
        return text.startsWith(PREFIX_FOR_COMMAND);
    }
}