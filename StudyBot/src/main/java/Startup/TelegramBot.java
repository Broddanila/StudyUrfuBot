package Startup;

import Handlers.BotHandler;
import Handlers.KeyboardFactory;

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
            long userId = update.getMessage().getFrom().getId();
            String messageText = update.getMessage().getText();
            String reply = botHandler.Handle(messageText, userId);

            sendMessage(userId, reply, messageText);
        }
    }
    
    private void sendMessage(Long chatId, String text, String userMessage) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setParseMode("HTML");
        
        // Показываем основную клавиатуру всегда, кроме случаев ожидания ввода
        // Простая проверка: если пользователь не вводит команду /cancel и не находится в состоянии ожидания,
        // но так как у нас нет доступа к BotState здесь, будем показывать клавиатуру для всех ответов,
        // кроме тех, которые являются приглашениями к вводу
        if (!text.contains("Введите") && !text.contains("Ошибка") && !text.startsWith("Не найдено")) {
            message.setReplyMarkup(KeyboardFactory.createMainKeyboard());
        } else if (text.contains("Введите")) {
            // Для приглашений к вводу показываем клавиатуру с кнопкой отмены
            message.setReplyMarkup(KeyboardFactory.createCancelKeyboard());
        }
        
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}