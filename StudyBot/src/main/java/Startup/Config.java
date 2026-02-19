package Startup;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static Properties properties = new Properties();

    static {
        // Загружаем properties из файла
        try (InputStream is = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (is == null) {
                throw new RuntimeException("Файл config.properties не найден!");
            }
            properties.load(is);
            
            // Заменяем плейсхолдеры на значения из переменных окружения
            replacePlaceholders();
            
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить config.properties", e);
        }
    }
    
    private static void replacePlaceholders() {
        // Заменяем плейсхолдеры в каждом свойстве
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            if (value != null && value.contains("${")) {
                // Ищем все плейсхолдеры вида ${NAME}
                String processed = value;
                int start = processed.indexOf("${");
                while (start != -1) {
                    int end = processed.indexOf("}", start);
                    if (end != -1) {
                        String placeholder = processed.substring(start + 2, end);
                        String envValue = System.getenv(placeholder);
                        if (envValue != null) {
                            processed = processed.substring(0, start) + envValue + processed.substring(end + 1);
                        } else {
                            // Если переменной окружения нет, оставляем как есть
                            System.err.println("Warning: Environment variable " + placeholder + " not found");
                        }
                    }
                    start = processed.indexOf("${", start + 1);
                }
                properties.setProperty(key, processed);
            }
        }
    }

    public static String getBotToken() {
        return properties.getProperty("bot.token");
    }

    public static String getBotUsername() {
        return properties.getProperty("bot.username");
    }
    
    public static String getDbUrl() {
        return properties.getProperty("db.url");
    }
    
    public static String getDbUser() {
        return properties.getProperty("db.user");
    }
    
    public static String getDbPassword() {
        return properties.getProperty("db.password");
    }
}