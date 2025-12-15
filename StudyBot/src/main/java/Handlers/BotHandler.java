package Handlers;
import java.util.*;
import Handlers.Commands.FinishHomeworkCommand;
import Handlers.Commands.ProcessScheduleInputCommand;
import Handlers.Commands.ShowScheduleCommand;

public class BotHandler {
    private Map<Long, BotState> userStates = new HashMap<>();
    
    public enum BotState {
        SLEEP,
        WAITING_FOR_LESSON,
        WAITING_FOR_HOMEWORK,
        WAITING_FOR_LOGIN,
        WAITING_FOR_EDIT
    }
    
    private String reply;

    public void SetReply(String newReply) {
        reply = newReply;
    }

    public String Handle(String message, long userId) {
        reply = "Привет! 👋\nЯ — твой помощник для управления домашними заданиями и расписанием. Вот что я умею:\n📥 /show — посмотреть расписание\n✏️ /add — добавить новое домашнее задание\n✅ /finish — отметить домашнее задание как выполненное\n✏️ /edit — изменить расписание\nЧтобы начать, выбери нужную команду или напиши её из списка выше\nУдачи в учебе! 🎓";
        
        BotState state = userStates.getOrDefault(userId, BotState.SLEEP);

        DbConnector.ensureUserExists(userId);
        switch (message) {
            case "/show":
                ShowScheduleCommand ssc = new ShowScheduleCommand();
                return ssc.Action(userId,"");
            case "/add":
                userStates.put(userId, BotState.WAITING_FOR_HOMEWORK);
                return "Введите название предмета и домашнее задание на него через пробел";
            case "/finish":
                userStates.put(userId, BotState.WAITING_FOR_LESSON);
                return "Введите название предмета, по которому вы сделали домашнее задание";
            case "/edit":
                userStates.put(userId, BotState.WAITING_FOR_EDIT);
                return "Введите расписание в формате: (названиепредмета1 названиепредмета2)(названиепредмета3 названиепредмета4 названиепредмета5)()(названиепредмета6)()()()\n" +
                       "Скобки — разные дни, начиная с понедельника. Предметы внутри скобок разделяйте пробелами.";
            default:
                if (state == BotState.SLEEP) {
                    return reply;
                } else if (state == BotState.WAITING_FOR_HOMEWORK) {
                    String[] parts = message.split(" ", 2);
                    if (parts.length < 2) {
                        return "Ошибка: укажите предмет и задание через пробел.";
                    }
                    userStates.put(userId, BotState.SLEEP);
                    String subject = parts[0];
                    String description = parts[1];
                    DbConnector.addHomework(userId, subject, description);
                    return "Домашнее задание добавлено!";
                } else if (state == BotState.WAITING_FOR_LESSON) {
                    userStates.put(userId, BotState.SLEEP);
                    FinishHomeworkCommand fhc = new FinishHomeworkCommand();
                    return fhc.Action(userId,message);                    
                } else if (state == BotState.WAITING_FOR_EDIT) {
                    userStates.put(userId, BotState.SLEEP);
                    ProcessScheduleInputCommand psic = new ProcessScheduleInputCommand();
                    return psic.Action(userId,message);
                }
        }
        return reply;
    }              
}
