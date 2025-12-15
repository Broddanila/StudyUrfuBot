package Handlers;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class DbConnector {
    private static final String URL = Startup.Config.getDbUrl();
    private static final String USER = Startup.Config.getDbUser();
    private static final String PASSWORD = Startup.Config.getDbPassword();

    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    
    public static void addHomework(long userId, String subject, String description) {
    ensureUserExists(userId);
    
    String sql = "INSERT INTO bot.homeworks (user_id, subject_name, description, entry_order, completed) " +
               "VALUES (?, ?, ?, COALESCE((SELECT MAX(entry_order) FROM bot.homeworks WHERE user_id = ? AND subject_name = ?), 0) + 1, FALSE) " +
               "ON CONFLICT (user_id, subject_name, entry_order) DO NOTHING";

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setLong(1, userId);
        pstmt.setString(2, subject);
        pstmt.setString(3, description);
        pstmt.setLong(4, userId);  
        pstmt.setString(5, subject);

        pstmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
    }



    
    public static void ensureUserExists(long userId) {
    String sql = "INSERT INTO bot.users (id) VALUES (?) ON CONFLICT (id) DO NOTHING";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setLong(1, userId);
        pstmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

public static void addSubjectToSchedule(long userId, int dayOfWeek, String subject, int orderIndex) throws SQLException {
    ensureUserExists(userId);
    String sql = "INSERT INTO bot.schedule (user_id, day_of_week, subject_name, order_index) VALUES (?, ?, ?, ?)";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setLong(1, userId);
        pstmt.setInt(2, dayOfWeek);
        pstmt.setString(3, subject);
        pstmt.setInt(4, orderIndex);
        pstmt.executeUpdate();
    }
}

public static void clearScheduleDay(long userId, int dayOfWeek) {
    String sql = "DELETE FROM bot.schedule WHERE user_id = ? AND day_of_week = ?";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setLong(1, userId);
        pstmt.setInt(2, dayOfWeek);
        pstmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}



    
    public static List<String> getSchedule(long userId) {
    List<String> schedule = new ArrayList<>();
    String sql = "SELECT day_of_week, subject_name, order_index FROM bot.schedule WHERE user_id = ? ORDER BY day_of_week, order_index";

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setLong(1, userId);
        ResultSet rs = pstmt.executeQuery();

        Map<Integer, List<String>> days = new HashMap<>();
        while (rs.next()) {
            int day = rs.getInt("day_of_week");
            String subject = rs.getString("subject_name");
            days.computeIfAbsent(day, k -> new ArrayList<>()).add(subject);
        }

        String[] dayNames = {"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"};
        for (int i = 1; i <= 7; i++) {
            List<String> subjects = days.getOrDefault(i, Collections.emptyList());
            if (!subjects.isEmpty()) {
                schedule.add(dayNames[i-1] + ": " + String.join(", ", subjects));
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return schedule;
}

    
    public static boolean markHomeworkAsDone(long userId, String subject) {
        String sql = "UPDATE bot.homeworks SET completed = TRUE " +
                   "WHERE user_id = ? AND subject_name = ? AND completed = FALSE";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {


            pstmt.setLong(1, userId);
            pstmt.setString(2, subject);

            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    
    public static List<String> getPendingHomeworks(long userId) {
        List<String> homeworks = new ArrayList<>();
        String sql = "SELECT subject_name, description FROM bot.homeworks " +
                   "WHERE user_id = ? AND completed = FALSE " +
                   "ORDER BY entry_order";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String subject = rs.getString("subject_name");
                String desc = rs.getString("description");
                homeworks.add(subject + ": " + desc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return homeworks;
    }
    public static List<Homework> getHomeworks(long userId) {
    List<Homework> homeworks = new ArrayList<>();
    String sql = "SELECT subject_name, description, entry_order " +
               "FROM bot.homeworks " +
               "WHERE user_id = ? AND completed = FALSE " +
               "ORDER BY subject_name, entry_order";

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setLong(1, userId);
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            Homework hw = new Homework();
            hw.Set_subject( rs.getString("subject_name"));
            hw.Set_description(rs.getString("description"));
            hw.Set_entryOrder (rs.getInt("entry_order"));
            homeworks.add(hw);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return homeworks;
}

}
