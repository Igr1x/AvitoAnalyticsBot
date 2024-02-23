package ru.avitoAnalytics.AvitoAnalyticsBot.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.avitoAnalytics.AvitoAnalyticsBot.actions.Actions;
import ru.avitoAnalytics.AvitoAnalyticsBot.actions.HelpAction;
import ru.avitoAnalytics.AvitoAnalyticsBot.actions.StartAction;
import ru.avitoAnalytics.AvitoAnalyticsBot.actions.TariffsAction;
import ru.avitoAnalytics.AvitoAnalyticsBot.command.Parser;
import ru.avitoAnalytics.AvitoAnalyticsBot.configuration.BotConfig;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.User;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.AccountService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.UserService;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.TelegramChatUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final UserService userService;
    private final AccountService accountService;

    private Map<String, Actions> actions = Map.of(
            "/start", new StartAction(),
            "/help", new HelpAction(),
            "/tariffs", new TariffsAction()
    );


    public TelegramBot(BotConfig botConfig, UserService userService, AccountService accountService) {
        this.botConfig = botConfig;
        this.userService = userService;
        this.accountService = accountService;

        List<BotCommand> listOfCommand = new ArrayList<>();
        listOfCommand.add(new BotCommand("/start", ""));
        listOfCommand.add(new BotCommand("/help", ""));
        listOfCommand.add(new BotCommand("/add", ""));
        listOfCommand.add(new BotCommand("/accounts", ""));
        listOfCommand.add(new BotCommand("/tariffs", ""));
        try {
            this.execute(new SetMyCommands(listOfCommand, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
        }

    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Long chatId = update.getMessage().getChatId();
            registrationUser(update.getMessage().getChat().getUserName(), chatId.toString());
            var key = update.getMessage().getText();
            if (actions.containsKey(key)) {
                if (key.equals("/help")) {
                    var msg = actions.get(key).handleMediaGroup(update, chatId);
                    try {
                        execute(msg);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        var msg2 = actions.get(key).handleMessage(update, chatId);
                        execute(msg2);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e);
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }

                } else {
                    try {
                        var msg = actions.get(key).handlePhoto(update, chatId);
                        execute(msg);
                    } catch (TelegramApiException | InvocationTargetException | NoSuchMethodException |
                             IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        else if (update.hasCallbackQuery()) {
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            var key = update.getCallbackQuery().getData();
            if (actions.containsKey(key)) {
                try {
                    var msg = actions.get(key).handlePhoto(update, chatId);
                    execute(msg);
                } catch (TelegramApiException | InvocationTargetException | NoSuchMethodException |
                         IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        /*if (update.hasMessage() && update.getMessage().hasText()) {
            String command = parser.getParsedCommand(update.getMessage().getText()).getCommand().toString();
            String username = update.getMessage().getChat().getUserName();
            Long chatId = update.getMessage().getChatId();
            User user = userService.getUser(chatId).orElseGet(() -> {
                User added = userService.saveUser(new User(), username, chatId);
                log.info("Added new user with name {} and id {}", username, chatId);
                return added;
            });
            if (!userService.existsUser(String.valueOf(chatId))) {
                userService.saveUser(user, username, chatId);
                log.info("Added new user with name {} and id {}", username, chatId);
            }
            switch (command) {
                case "START" -> {
                    try {
                        TelegramChatUtils.sendMessage(this, chatId, "Привки " + username);
                    } catch (TelegramApiException e) {
                        log.error("TelegramApiException occurred");
                    }
                }
                case "ADD" -> {
                    try {
                        TelegramChatUtils.sendMessage(this, chatId, "Введите через пробел:\n 1. client_id;\n" +
                                " 2. client_secret;\n" + " 3. Ссылку на свою Google.Таблицу;");
                    } catch (TelegramApiException e) {
                        log.error("TelegramApiException occurred");
                    }
                }
            }
            if (update.getMessage().hasText() && update.getMessage().getText().contains(" ")) {
                String responseUser = update.getMessage().getText();
                String[] str = responseUser.split(" ");
                AccountData accountData = new AccountData();
                accountData.setUser(accountService.getUser(chatId).get());
                accountData.setClientId(str[0]);
                accountData.setClientSecret(str[1]);
                accountData.setSheetsRef(str[2]);
                accountService.saveAccount(accountData);
            }
        }*/
    }

    private void registrationUser(String username, String chatId) {
        if (!userService.existsUser(chatId)) {
            User newUser = new User(username, chatId);
            userService.saveUser(newUser);
            log.info("Added new user with name {} and id {}", username, chatId);
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

