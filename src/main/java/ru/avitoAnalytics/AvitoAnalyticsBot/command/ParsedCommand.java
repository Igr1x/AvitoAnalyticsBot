package ru.avitoAnalytics.AvitoAnalyticsBot.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParsedCommand {
    Command command = Command.NONE;
}
