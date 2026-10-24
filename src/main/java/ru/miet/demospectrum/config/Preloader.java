package ru.miet.demospectrum.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class Preloader {

    public static Properties props = new Properties();

    public static void loadConfig(String fileName) {
        try {
            InputStream fs = Preloader.class.getResourceAsStream("/" + fileName);

            if (fs == null) {
                System.err.println(" Конфигурационный файл не найден: " + fileName);
                return;
            }

            props.load(new java.io.InputStreamReader(fs, java.nio.charset.StandardCharsets.UTF_8));
            fs.close();
            System.out.println(" Конфигурация загружена");
            System.out.println("   Пользователь: " + props.getProperty("user.name"));
            System.out.println("   Группа: " + props.getProperty("user.group"));
            System.out.println("   Отладка: " + props.getProperty("debug.mode"));

        } catch (Exception e) {
            System.err.println(" Ошибка загрузки конфигурации: " + e.getMessage());
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(props.getProperty(key, "false"));
    }
}