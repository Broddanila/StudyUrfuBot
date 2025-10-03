package Handlers;

import java.util.HashMap;
import java.util.Map;

public class BotHandler {
    private Map<Long,BotState> userStates = new HashMap<>();
    public enum BotState {
    SLEEP,
    WAITING_FOR_LESSON,
    WAITING_FOR_HOMEWORK,
    WAITING_FOR_LOGIN,
    WAITING_FOR_EDIT
    // добавьте свои состояния
    }        
    private String reply;
    public void SetReply(String newReply){
        reply = newReply;
    }             
    public String Handle(String message,long userId) {
        reply= "Привет! 👋\nЯ — твой помощник для управления домашними заданиями и расписанием. Вот что я умею:\n📥 /show — посмотреть рассписание\n/add — добавить новое домашнее задание\n✅ /finish — отметить домашнее задание как выполненное\n🔐 /login — авторизоваться\n✏️ /edit — изменить расписание\nЧтобы начать, выбери нужную команду или напиши её из списка выше\nУдачи в учебе! 🎓";
        BotState state = userStates.getOrDefault(userId, BotState.SLEEP);                
        switch (message){
            case "/show":
                break;
            case "/add":
                userStates.put(userId, BotState.WAITING_FOR_HOMEWORK);
                return"Введите название предмета и домашнее задание на него через пробел";
            case "/finish":
                userStates.put(userId, BotState.WAITING_FOR_LESSON);
                return "Введите название предмета по которому вы сделали домашнее задание";                    
            case "/edit":
                userStates.put(userId, BotState.WAITING_FOR_EDIT);
                return "Введите расписание в формате: понедельник названиепредмета1 названиепредмета2 вторник названиепредмета1 и т.д ";
            case "/login":
                userStates.put(userId, BotState.WAITING_FOR_LOGIN);
                return "Введите логин и пароль через пробел";
            default:
                if(state==BotState.SLEEP){
                    return reply;                                            
                }
                else if(state == BotState.WAITING_FOR_HOMEWORK){
                    return AddHomeworkHandler.AddHomework(message);
                }
                else if(state == BotState.WAITING_FOR_LESSON){
                    
                }
                else if(state == BotState.WAITING_FOR_EDIT){
                    
                }
                else if(state == BotState.WAITING_FOR_LOGIN){
                    
                }                                    
            }    
                                
        return reply;
    }       
}