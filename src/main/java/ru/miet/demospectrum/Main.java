package ru.miet.demospectrum;

import ru.miet.demospectrum.config.Preloader;
import ru.miet.demospectrum.view.LoginWindow;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Загружаем конфигурацию
        Preloader.loadConfig("config.properties");

        // Открываем окно авторизации
        SwingUtilities.invokeLater(() -> {
            LoginWindow loginWindow = new LoginWindow();
            loginWindow.setVisible(true);
        });
    }
}