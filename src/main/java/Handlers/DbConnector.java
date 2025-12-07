package Handlers;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class DbConnector {
    private static final String URL = "jdbc:postgresql://studybot-db-1:5432/botdb";
    private static final String USER = "botuser";
    private static final String PASSWORD = "botpass";

    /**
     * Получение соединения с БД
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Добавление домашнего задания в БД
     */
    public static void addHomework(long userId, String subject, String description) {
        String sql = "INSERT INTO bot.homeworks (user_id, subject_name, description, entry_order, completed) " +
                   "SELECT ?, ?, ?, COALESCE(MAX(entry_order), 0) + 1, FALSE " +
                   "FROM bot.homeworks " +
                   "WHERE user_id = ? AND subject_name = ? " +
                   "ON CONFLICT DO NOTHING";

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

    /**
     * Сохранение предмета в расписание
     */
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

public static void addSubjectToSchedule(long userId, int dayOfWeek, String subject) {
    ensureUserExists(userId);  // Гарантируем существование пользователя

    String sql = "INSERT INTO bot.schedule (user_id, day_of_week, subject_name) " +
               "VALUES (?, ?, ?) " +
               "ON CONFLICT (user_id, day_of_week, subject_name) DO NOTHING";

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setLong(1, userId);
        pstmt.setInt(2, dayOfWeek);
        pstmt.setString(3, subject);

        pstmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}


    /**
     * Получение расписания пользователя
     */
    public static List<String> getSchedule(long userId) {
        List<String> schedule = new ArrayList<>();
        String sql = "SELECT day_of_week, subject_name FROM bot.schedule WHERE user_id = ? ORDER BY day_of_week";

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

    /**
     * Отметить домашнее задание как выполненное
     */
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

    /**
     * Получить список невыполненных домашних заданий
     */
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
            hw.subject = rs.getString("subject_name");
            hw.description = rs.getString("description");
            hw.entryOrder = rs.getInt("entry_order");
            homeworks.add(hw);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return homeworks;
}

}
