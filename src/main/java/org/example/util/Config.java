package org.example.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Config {
    private static Properties properties = new Properties();

    static {
        try (InputStream inputStream = Config.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (inputStream == null) {
                System.err.println("Không tìm thấy tệp config.properties");
            } else {
                properties.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
