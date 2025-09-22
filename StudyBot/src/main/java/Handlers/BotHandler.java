package Handlers;

public class BotHandler {    
    private String startMessage = "Привет! 👋\nЯ — твой помощник для управления домашними заданиями и расписанием. Вот что я умею:\n📥 /add — добавить новое домашнее задание\n✅ /finish — отметить домашнее задание как выполненное\n🔐 /login — авторизоваться\n✏️ /edit — изменить расписание\nЧтобы начать, выбери нужную команду или напиши её из списка выше\nУдачи в учебе! 🎓";                
    public String Handle(String message) {
        if (IsCommand(message)) {            
            return "later";
        }
        else{
            return startMessage;
        }
    }

    private static boolean IsCommand(String message) {
        return message.startsWith("/");
    }
}