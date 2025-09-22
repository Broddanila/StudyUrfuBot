package Startup;

import Handlers.BotHandler;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


public class TelegramBot extends TelegramLongPollingBot {
    
     private final BotHandler botHandler = new BotHandler();
    @Override
    public String getBotUsername() {
        return Config.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return Config.getBotToken();
    }
    
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String messageFromUser = update.getMessage().getText();

            String reply = botHandler.Handle(messageFromUser);

            SendMessage message = new SendMessage(chatId, reply);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}