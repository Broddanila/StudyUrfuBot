package Handlers;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        reply = "Привет! 👋\nЯ — твой помощник для управления домашними заданиями и расписанием. Вот что я умею:\n📥 /show — посмотреть расписание\n/add — добавить новое домашнее задание\n✅ /finish — отметить домашнее задание как выполненное\n🔐 /login — авторизоваться\n✏️ /edit — изменить расписание\nЧтобы начать, выбери нужную команду или напиши её из списка выше\nУдачи в учебе! 🎓";
        
        BotState state = userStates.getOrDefault(userId, BotState.SLEEP);

        DbConnector.ensureUserExists(userId);
        switch (message) {
            case "/show":
                return showSchedule(userId);
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
                    String subject = parts[0];
                    String description = parts[1];
                    DbConnector.addHomework(userId, subject, description);
                    return "Домашнее задание добавлено!";
                } else if (state == BotState.WAITING_FOR_LESSON) {
                    return finishHomework(userId, message);
                } else if (state == BotState.WAITING_FOR_EDIT) {
                    return processScheduleInput(message, userId);
                }
        }
        return reply;
    }

    // Метод для показа расписания
    private String showSchedule(long userId) {
    // Получаем расписание (предметы по дням)
    List<String> scheduleLines = DbConnector.getSchedule(userId);
    
    // Получаем все незавершённые ДЗ
    List<Homework> homeworks = DbConnector.getHomeworks(userId);

    if (scheduleLines.isEmpty()) {
        return "Расписание не заполнено. Используйте /edit, чтобы добавить предметы.";
    }

    if (homeworks.isEmpty()) {
        return "Расписание:\n" + String.join("\n", scheduleLines) +
                "\n\nДомашних заданий пока нет. Используйте /add, чтобы добавить.";
    }

    StringBuilder sb = new StringBuilder("📅 Ваше расписание и домашние задания:\n\n");

    // Группируем ДЗ по предметам
    Map<String, List<String>> homeworksBySubject = new HashMap<>();
    for (Homework hw : homeworks) {
        homeworksBySubject.computeIfAbsent(hw.subject, k -> new ArrayList<>())
                             .add(hw.description);
    }

    // Выводим по дням
    for (String line : scheduleLines) {
        // Разделяем день и предметы
        String[] parts = line.split(": ", 2);
        if (parts.length < 2) {
            sb.append(line).append("\n");
            continue;
        }

        String day = parts[0];  // Например, "Понедельник"
        String subjectsStr = parts[1];  // Например, "Математика, Физика"
        String[] subjects = subjectsStr.split(",\\s*");

        sb.append(day).append(":\n");  // Выводим день

        for (String subject : subjects) {
            if (homeworksBySubject.containsKey(subject)) {
                // Для каждого ДЗ по предмету выводим строку
                for (String task : homeworksBySubject.get(subject)) {
                    sb.append("  ").append(subject).append(" (")
                      .append(task).append(")\n");
                }
            } else {
                // Если ДЗ нет — просто выводим предмет
                sb.append("  ").append(subject).append("\n");
            }
        }
    }

    return sb.toString();
}



    // Метод для отметки ДЗ как выполненного
    private String finishHomework(long userId, String subject) {
        boolean success = DbConnector.markHomeworkAsDone(userId, subject);
        if (success) {
            return "Задание по предмету «" + subject + "» отмечено как выполненное!";
        } else {
            return "Не найдено незавершённое задание по предмету «" + subject + "». Проверьте название.";
        }
    }

    // Метод для обработки ввода расписания (как в предыдущем ответе)
    private String processScheduleInput(String input, long userId) {
        Pattern pattern = Pattern.compile("\\(([^)]*)\\)");
        Matcher matcher = pattern.matcher(input);


        int dayIndex = 1;
        boolean hasChanges = false;

        while (matcher.find()) {
            String dayContent = matcher.group(1).trim();
            String[] subjects = dayContent.split("\\s+");


            if (subjects.length > 0 && !subjects[0].isEmpty()) {
                for (String subject : subjects) {
                    try {
                        DbConnector.addSubjectToSchedule(userId, dayIndex, subject);
                        hasChanges = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "Ошибка при сохранении предмета: " + subject;
                    }
                }
            }
            dayIndex++;
        }

        if (dayIndex > 7) {
            return "Ошибка: слишком много дней (максимум 7).";
        }

        if (!hasChanges) {
            return "Расписание не содержит предметов. Проверьте формат ввода.";
        }

        return "Расписание обновлено! Теперь вы можете использовать /show, чтобы увидеть его.";
    }
}
