package Handlers.Commands;
import Handlers.Commands.Interfaces.Command;
import java.util.*;
import Handlers.Homework;
import Handlers.DbConnector;
public class ShowScheduleCommand implements Command{
    @Override
    public String Action(long userId,String string){
        List<String> scheduleLines = DbConnector.getSchedule(userId);
        List<Homework> homeworks = DbConnector.getHomeworks(userId);

        if (scheduleLines.isEmpty()) {
            return "Расписание не заполнено. Используйте /edit, чтобы добавить предметы.";
        }

        if (homeworks.isEmpty()) {
            return "Расписание:\n" + String.join("\n", scheduleLines) +"\n\nДомашних заданий пока нет. Используйте /add, чтобы добавить.";
        }

        StringBuilder sb = new StringBuilder("📅 Ваше расписание и домашние задания:\n\n");

        Map<String, List<String>> homeworksBySubject = new HashMap<>();
        for (Homework hw : homeworks) {
            homeworksBySubject.computeIfAbsent(hw.Get_subject(), k -> new ArrayList<>()).add(hw.Get_description());
        }
        for (String line : scheduleLines) {
        
            String[] parts = line.split(": ", 2);
            if (parts.length < 2) {
                sb.append(line).append("\n");
                continue;
            }

        String day = parts[0];  
        String subjectsStr = parts[1];  
        String[] subjects = subjectsStr.split(",\\s*");

        sb.append(day).append(":\n");  

        for (String subject : subjects) {
            if (homeworksBySubject.containsKey(subject)) {
                
                for (String task : homeworksBySubject.get(subject)) {
                    sb.append("  ").append(subject).append(" (")
                      .append(task).append(")\n");
                }
            } else {
                sb.append("  ").append(subject).append("\n");
            }
        }
        }
        return sb.toString();
    }    
}