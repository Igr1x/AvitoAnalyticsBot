package ru.avitoAnalytics.AvitoAnalyticsBot.controller;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.avitoAnalytics.AvitoAnalyticsBot.actions.*;
import ru.avitoAnalytics.AvitoAnalyticsBot.configuration.BotConfig;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.User;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.AccountService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.UserService;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.PatternMap;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final UserService userService;
    private final AccountService accountService;

    @Autowired
    private BalanceAction balanceAction;
    @Autowired
    private ConnectTariff connectTariff;

    private PatternMap<String, Actions> actionsCommand = new PatternMap<>();
    private PatternMap<String, Actions> actionsKeyboard = new PatternMap<>();

    @PostConstruct
    public void init() {
        actionsKeyboard.putPattern(key -> key.startsWith("tariff"), new TariffAction());
        actionsKeyboard.putPattern(key -> key.startsWith("connect"), connectTariff);
        actionsCommand.put("/start", new StartAction());
        actionsCommand.put("/help", new HelpAction());
        actionsCommand.put("/tariffs", new TariffsAction());
        actionsCommand.put("/balance", balanceAction);
        actionsKeyboard.put("/start", new StartAction());
        actionsKeyboard.put("/help", new HelpAction());
        actionsKeyboard.put("/tariffs", new TariffsAction());
        actionsKeyboard.put("/balance", balanceAction);
        actionsKeyboard.put("backToTariffs", new TariffsAction());
    }

    public TelegramBot(BotConfig botConfig, UserService userService, AccountService accountService) {
        this.botConfig = botConfig;
        this.userService = userService;
        this.accountService = accountService;

        List<BotCommand> listOfCommand = new ArrayList<>();
        listOfCommand.add(new BotCommand("/start", "халоу"));
        listOfCommand.add(new BotCommand("/help", ""));
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
            var key = update.getMessage().getText();
            if (key.equals("/start")) {
                registrationUser(update.getMessage().getChat().getUserName(), chatId);
            }
            if (actionsCommand.containsKey(key)) {
                try {
                    var msg = actionsCommand.get(key).handleMessage(update, chatId);
                    if (key.equals("/help")) {
                        executeAsync((SendDocument) msg);
                        return;
                    }
                    executeAsync((SendPhoto) msg);
                } catch (TelegramApiException | InvocationTargetException | NoSuchMethodException |
                         IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (update.hasCallbackQuery()) {
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            var key = update.getCallbackQuery().getData();
            if (actionsKeyboard.containsKey(key)) {
                try {
                    var msg = actionsKeyboard.get(key).handleMessage(update, chatId);
                    if (key.equals("/help")) {
                        executeAsync((SendDocument) msg);
                        return;
                    }
                    if (key.equals("connect1") || key.equals("connect2") || key.equals("connect3")) {
                        executeAsync((SendMessage) msg);
                        return;
                    }
                    executeAsync((SendPhoto) msg);
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

    private void registrationUser(String username, Long chatId) {
        User user = userService.getUser(chatId).orElseGet(() ->
        {
            User added = new User(username, chatId.toString());
            log.info("Added new user with name {} and id {}", username, chatId);
            return userService.saveUser(added);
        });
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

