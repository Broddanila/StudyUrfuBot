package Handlers.Commands;
import Handlers.Commands.Interfaces.Command;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import Handlers.DbConnector;
public class ProcessScheduleInputCommand implements Command {
    @Override
    public String Action(long userId,String input){  
        if (!input.startsWith("(") || !input.endsWith(")")) {
            return "Ошибка: строка должна начинаться с \"(\" и заканчиваться \")\"";
        }

        Pattern pattern = Pattern.compile("\\(([^)]*)\\)");
        Matcher matcher = pattern.matcher(input);

        int dayIndex = 1;
        boolean hasChanges = false;

        while (matcher.find()) {
            if (dayIndex > 7) {
                return "Ошибка: слишком много дней (максимум 7).";
            }

            String dayContent = matcher.group(1).trim();
            String[] subjects = dayContent.split("\\s+");

            
            DbConnector.clearScheduleDay(userId, dayIndex);

            if (subjects.length > 0 && !subjects[0].isEmpty()) {
                for (int j = 0; j < subjects.length; j++) {
                    String subject = subjects[j].trim();
                    if (!subject.isEmpty()) {
                        try {
                            
                            DbConnector.addSubjectToSchedule(userId, dayIndex, subject, j + 1);
                            hasChanges = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return "Ошибка при сохранении предмета: " + subject + e;
                        }
                    }
                }
            }
            dayIndex++;
        }

        
        if (dayIndex == 1) {
            return "Ошибка: не найдено ни одного дня в формате (предметы).";
        }

        
        if (!hasChanges) {
            return "Расписание не содержит предметов. Проверьте формат ввода.";
        }

        return "Расписание обновлено! Теперь вы можете использовать /show, чтобы увидеть его.";
    }
}