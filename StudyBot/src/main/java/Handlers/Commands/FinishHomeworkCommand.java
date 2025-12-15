package Handlers.Commands;
import Handlers.Commands.Interfaces.Command;
import Handlers.DbConnector;
public class FinishHomeworkCommand implements Command{
    @Override
    public String Action(long userId, String subject){
        boolean success = DbConnector.markHomeworkAsDone(userId, subject);
        if (success) {
            return "Задание по предмету «" + subject + "» отмечено как выполненное!";
        } else {
            return "Не найдено незавершённое задание по предмету «" + subject + "». Проверьте название.";
        }        
    }
}