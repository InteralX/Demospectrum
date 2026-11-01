package ru.miet.demospectrum.view;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import ru.miet.demospectrum.config.Preloader;
import ru.miet.demospectrum.service.ErrMsgLog;

public class LoginWindow extends JFrame {

    private JTextField loginField;
    private JPasswordField passwordField;

    public LoginWindow() {
        setTitle("Демоспектр — Вход");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Заголовок
        JLabel titleLabel = new JLabel("Вход в систему", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        // Логин
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Логин:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        loginField = new JTextField(15);
        panel.add(loginField, gbc);

        // Пароль
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Пароль:"), gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        passwordField = new JPasswordField(15);
        panel.add(passwordField, gbc);

        // Кнопка "Войти"
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        JButton loginButton = new JButton("Войти");
        loginButton.addActionListener(e -> onLogin());
        panel.add(loginButton, gbc);

        // Кнопка "Выход"
        gbc.gridx = 0; gbc.gridy = 4;
        JButton exitButton = new JButton("Выход");
        exitButton.addActionListener(e -> System.exit(0));
        panel.add(exitButton, gbc);

        // Enter в поле пароля = нажатие "Войти"
        passwordField.addActionListener(e -> onLogin());

        add(panel);
    }

    /**
     * Проверка логина и пароля.
     */
    private void onLogin() {
        String login = loginField.getText().trim();
        String password = new String(passwordField.getPassword());

        ErrMsgLog logger = new ErrMsgLog();

        if (login.isEmpty() || password.isEmpty()) {
            logger.warn("Попытка входа с пустым логином или паролем");
            showError("Введите логин и пароль!");
            passwordField.setText("");
            return;
        }

        boolean authenticated = false;
        String matchedGroup = null;

        // Проверяем пользователей по порядку
        for (int i = 1; i <= 3; i++) {
            String expectedLogin = Preloader.get("user." + i + ".name");
            
            // Если ключ не найден, значит, список пользователей закончился
            if (expectedLogin == null) {
                break;
            }

            String expectedPassword = Preloader.get("user." + i + ".password");
            
            if (login.equals(expectedLogin) && password.equals(expectedPassword)) {
                authenticated = true;
                matchedGroup = Preloader.get("user." + i + ".group");
                break;
            }
        }

        if (authenticated) {
            logger.info("Успешный вход: " + login + " (группа: " + matchedGroup + ")");
            
            passwordField.setText("");

            // Передаем логин и группу в конструктор MainWindow
            MainWindow mainWindow = new MainWindow(login, matchedGroup);
            mainWindow.setVisible(true);
            dispose();
        } else {
            logger.warn("Неудачная попытка входа: логин='" + login + "'");
            showError("Неверный логин или пароль!");
            passwordField.setText("");
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message,
                "Ошибка входа", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        Preloader.loadConfig("config.properties");
        SwingUtilities.invokeLater(() -> {
            LoginWindow window = new LoginWindow();
            window.setVisible(true);
        });
    }
}
