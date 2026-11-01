package ru.miet.demospectrum;

import com.formdev.flatlaf.FlatLightLaf;
import ru.miet.demospectrum.config.Preloader;
import ru.miet.demospectrum.view.LoginWindow;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Не удалось инициализировать FlatLaf");
        }

        Preloader.loadConfig("config.properties");

        SwingUtilities.invokeLater(() -> {
            LoginWindow loginWindow = new LoginWindow();
            loginWindow.setVisible(true);
        });
    }
}
