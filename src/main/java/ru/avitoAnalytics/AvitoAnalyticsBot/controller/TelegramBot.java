package ru.avitoAnalytics.AvitoAnalyticsBot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.avitoAnalytics.AvitoAnalyticsBot.command.Command;
import ru.avitoAnalytics.AvitoAnalyticsBot.command.ParsedCommand;
import ru.avitoAnalytics.AvitoAnalyticsBot.command.Parser;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.User;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final String name;
    private final String token;

    @Autowired
    Parser parser;

    @Autowired
    UserRepository userRepository;

    public TelegramBot( @Value("${bot.name}") String botUsername, @Value("${bot.token}") String botToken) {
        this.name = botUsername;
        this.token = botToken;
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
        } catch (TelegramApiException e) {
        }

        List<BotCommand> listOfCommand = new ArrayList<>();
        listOfCommand.add(new BotCommand("/start", ""));
        listOfCommand.add(new BotCommand("/help", ""));
        listOfCommand.add(new BotCommand("/add", ""));
        listOfCommand.add(new BotCommand("/accounts", ""));
        try {
            this.execute(new SetMyCommands(listOfCommand, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
        }

    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.getMessage().hasText()) {
            String command = parser.getParsedCommand(update.getMessage().getText()).getCommand().toString();
            switch (command) {
                case "START" : {
                    String telegramId = update.getMessage().getChat().getUserName();
                    if (!userRepository.existsByTelegramId(telegramId)) {
                        User user = new User();
                        user.setTelegramId(telegramId);
                        userRepository.save(user);
                    }
                    String text = "Привки " + telegramId;
                    SendMessage sendMessage = createMessage(update.getMessage().getChatId(), text);
                    try {
                        this.execute(sendMessage);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }
                /*case "ADD" : {
                    User user = new User();
                    user.setTelegramId(update.getMessage().getChatId());
                }*/
            }
        }
    }

    private void registerUser(Message message) {
    }

    @Override
    public String getBotUsername() {
        return name;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    private SendMessage createMessage(long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        return sendMessage;
    }
}

