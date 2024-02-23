package ru.avitoAnalytics.AvitoAnalyticsBot.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.avitoAnalytics.AvitoAnalyticsBot.command.Command;
import ru.avitoAnalytics.AvitoAnalyticsBot.command.ParsedCommand;
import ru.avitoAnalytics.AvitoAnalyticsBot.command.Parser;
import ru.avitoAnalytics.AvitoAnalyticsBot.configuration.BotConfig;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.User;
import ru.avitoAnalytics.AvitoAnalyticsBot.enums.SurveyStatus;
import ru.avitoAnalytics.AvitoAnalyticsBot.exceptions.RepeatAccountDataException;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.AccountService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.GoogleSheetsService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.UserService;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.TelegramChatUtils;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private final Parser parser;
    private final BotConfig botConfig;
    private final UserService userService;
    private final AccountService accountService;
    private final GoogleSheetsService googleSheetsService;

    private static SurveyStatus surveyStatus = SurveyStatus.CLIENT_ID;
    private AccountData accountData = new AccountData();

    public TelegramBot(Parser parser, BotConfig botConfig, UserService userService, AccountService accountService, GoogleSheetsService googleSheetsService) {
        this.parser = parser;
        this.botConfig = botConfig;
        this.userService = userService;
        this.accountService = accountService;
        this.googleSheetsService = googleSheetsService;

        List<BotCommand> listOfCommand = new ArrayList<>();
        listOfCommand.add(new BotCommand("/start", "Начало работы"));
        listOfCommand.add(new BotCommand("/help", "Справка"));
        listOfCommand.add(new BotCommand("/add", "Добавление аккаунта"));
        listOfCommand.add(new BotCommand("/accounts", "Работа с аккаунтом"));
        try {
            this.execute(new SetMyCommands(listOfCommand, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("TelegramApiException occurred");
        }

    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String command = parser.getParsedCommand(update.getMessage().getText()).getCommand().toString();
            String username = update.getMessage().getChat().getUserName();
            Long chatId = update.getMessage().getChatId();
            User user = userService.getUser(chatId).orElseGet(() -> {
                User added = userService.saveUser(new User(), username, chatId);
                log.info("Added new user with name {} and id {}", username, chatId);
                return added;
            });
            switch (command) {
                case "START" -> {
                    TelegramChatUtils.sendMessage(this, chatId, "Привки " + username);
                }
                case "ADD" -> {
                    TelegramChatUtils.sendMessage(this, chatId, "Введите свой client_id:");
                }
                case "NONE" -> {
                    try {
                        if (!surveyStatus.equals(SurveyStatus.END)) {
                            surveyStatus = TelegramChatUtils.processingMessage(this, update, accountData,
                                    surveyStatus, googleSheetsService);
                            if (surveyStatus.equals(SurveyStatus.END)) {
                                accountData.setUser(user);
                                accountService.saveAccount(accountData);
                                userService.addAccount(user, accountData);
                                final String responseEndSurvey = "Ваш аккаунт внесен в базу!";
                                TelegramChatUtils.sendMessage(this, chatId, responseEndSurvey);
                                accountData = new AccountData();
                                surveyStatus = SurveyStatus.CLIENT_ID;
                            }
                        }
                    } catch (RepeatAccountDataException e) {
                        TelegramChatUtils.sendMessage(this, chatId, "У вас уже есть данный аккаунт!");
                        log.error(e.getMessage());
                    } catch (TelegramApiException e) {
                        log.error("TelegramApiException occurred");
                    }
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

}

