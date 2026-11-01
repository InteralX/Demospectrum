package ru.miet.demospectrum.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.Window;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import ru.miet.demospectrum.config.Preloader;
import ru.miet.demospectrum.model.PopulationRecord;
import ru.miet.demospectrum.parser.PopulationParser;
import ru.miet.demospectrum.service.ErrMsgLog;
import ru.miet.demospectrum.service.FileExportService;
import ru.miet.demospectrum.service.FileImportService;

public class MainWindow extends JFrame {

    private JLabel statusLabel;
    private JTextArea logArea;
    private PopulationChartPanel chartPanel;
    private JPanel cardsPanel;
    private CardLayout cardLayout;

    // Данные текущего вошедшего пользователя
    private String currentUsername;
    private String currentUserGroup;

    // Конструктор по умолчанию (если запускается отдельно для отладки)
    public MainWindow() {
        this("Гость", "user");
    }

    // Основной конструктор, принимающий данные пользователя
    public MainWindow(String username, String group) {
        this.currentUsername = username;
        this.currentUserGroup = group;

        setTitle("Демоспектр — Визуализация демографии России");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // Меню
        setJMenuBar(createMenuBar());

        // Центральная область (с поддержкой переключения CardLayout)
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        // Статус-панель
        statusLabel = new JLabel("Готов к работе");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusLabel, BorderLayout.SOUTH);

        // === Логирование старта ===
        log("=== Программа запущена ===");
        log("Пользователь: " + currentUsername + " (" + currentUserGroup + ")");
        log("Время: " + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    /**
     * Создаёт меню: Файл, Отладка (root), Вид, О программе.
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // === Меню "Файл" ===
        JMenu fileMenu = new JMenu("Файл");

        JMenuItem exportItem = new JMenuItem("Экспорт данных...");
        exportItem.addActionListener(e -> onExportData());

        JMenuItem importItem = new JMenuItem("Импорт данных...");
        importItem.addActionListener(e -> onImportData());

        JMenuItem exitItem = new JMenuItem("Выход");
        exitItem.addActionListener(e -> {
            log("=== Программа завершена ===");
            System.exit(0);
        });

        fileMenu.add(exportItem);
        fileMenu.add(importItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        // === Меню "Отладка" (только для root) ===
        if ("root".equalsIgnoreCase(currentUserGroup)) {
            JMenu debugMenu = new JMenu("Отладка");

            JMenuItem showConfigItem = new JMenuItem("Показать конфигурацию");
            showConfigItem.addActionListener(e -> showConfigDialog());

            JMenuItem clearLogItem = new JMenuItem("Очистить лог");
            clearLogItem.addActionListener(e -> clearLog());

            debugMenu.add(showConfigItem);
            debugMenu.add(clearLogItem);

            menuBar.add(debugMenu);
        }

        // === Меню "Вид" (Переключение экранов и тем) ===
        JMenu viewMenu = new JMenu("Вид");
        
        JMenuItem showMapItem = new JMenuItem("Карта регионов");
        showMapItem.addActionListener(e -> {
            cardLayout.show(cardsPanel, "MAP");
            log("Открыта карта регионов");
        });

        JMenuItem showChartItem = new JMenuItem("График динамики населения");
        showChartItem.addActionListener(e -> {
            try {
                PopulationParser parser = new PopulationParser();
                List<PopulationRecord> records = parser.parse("https://ru.wikipedia.org/wiki/Зеленоград", 1);
                chartPanel.setData("Зеленоград", records);
                log("График обновлен: загружено записей — " + records.size());
            } catch (Exception ex) {
                log("Ошибка загрузки данных для графика: " + ex.getMessage());
            }
            cardLayout.show(cardsPanel, "CHART");
        });

        viewMenu.add(showMapItem);
        viewMenu.add(showChartItem);
        viewMenu.addSeparator();
        
        JMenuItem lightThemeItem = new JMenuItem("Светлая тема");
        lightThemeItem.addActionListener(e -> changeTheme(false));
        
        JMenuItem darkThemeItem = new JMenuItem("Темная тема");
        darkThemeItem.addActionListener(e -> changeTheme(true));
        
        viewMenu.add(lightThemeItem);
        viewMenu.add(darkThemeItem);
        
        menuBar.add(viewMenu);

        // === Меню "О программе" ===
        JMenu helpMenu = new JMenu("О программе");
        JMenuItem aboutItem = new JMenuItem("О разработчиках");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(helpMenu);
        return menuBar;
    }

    private JPanel createCenterPanel() {
        cardsPanel = new JPanel();
        cardLayout = new CardLayout();
        cardsPanel.setLayout(cardLayout);

        MapPanel mapPanel = new MapPanel();
        chartPanel = new PopulationChartPanel();

        cardsPanel.add(mapPanel, "MAP");
        cardsPanel.add(chartPanel, "CHART");

        logArea = new JTextArea(8, 80);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Лог операций"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                cardsPanel, logScroll);
        splitPane.setResizeWeight(0.75);
        splitPane.setDividerLocation(500);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private void onExportData() {
        try {
            ErrMsgLog logger = new ErrMsgLog();
            logger.action("Экспорт", "Старт экспорта в export.csv");

            FileExportService service = new FileExportService();
            service.exportToCsv("export.csv");

            logger.action("Экспорт", "Успешно завершён: export.csv");
            log("✅ Экспорт завершён: export.csv");

            JOptionPane.showMessageDialog(this,
                    "Данные экспортированы в export.csv",
                    "Экспорт",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            new ErrMsgLog().addErrWithLog(e);
            log("❌ Ошибка экспорта: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Ошибка: " + e.getMessage(),
                    "Ошибка экспорта",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onImportData() {
        try {
            ErrMsgLog logger = new ErrMsgLog();
            logger.action("Импорт", "Открытие диалога выбора файла");

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Выберите CSV для импорта");
            int result = chooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                String path = chooser.getSelectedFile().getAbsolutePath();
                logger.action("Импорт", "Выбран файл: " + path);

                FileImportService service = new FileImportService();
                int imported = service.importFromCsv(path);

                logger.action("Импорт", "Импортировано записей: " + imported);
                log("✅ Импорт завершён: " + path + " (" + imported + " записей)");
                
                JOptionPane.showMessageDialog(this,
                        "Импортировано записей: " + imported,
                        "Импорт",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            log("❌ Ошибка импорта: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Ошибка: " + e.getMessage(),
                    "Ошибка импорта",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showConfigDialog() {
        String configText = "Текущая конфигурация:\n\n"
                + "Пользователь: " + currentUsername + "\n"
                + "Группа: " + currentUserGroup + "\n"
                + "Отладка: " + Preloader.get("debug.mode") + "\n"
                + "Автотесты: " + Preloader.get("autotest.mode");

        JOptionPane.showMessageDialog(this, configText,
                "Конфигурация", JOptionPane.INFORMATION_MESSAGE);
        log("Показана конфигурация");
    }

    private void changeTheme(boolean isDark) {
        try {
            if (isDark) {
                UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
                log("Включена темная тема");
            } else {
                UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
                log("Включена светлая тема");
            }
            
            for (Window window : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
            }
        } catch (Exception e) {
            log("Ошибка при смене темы: " + e.getMessage());
        }
    }

    private void clearLog() {
        logArea.setText("");
        log("Лог очищен");
    }

    private void showAboutDialog() {
        String message = "«Статистика изменения плотности населения России "
                + "и векторов миграции населения — Демоспектр»\n\n"
                + "Разработчики:\n"
                + "• Виталина Лысковская — Backend/Data\n"
                + "• Павел Елюков — Парсинг городов\n\n"
                + "Группа: ПИН-22 и ПИН-27\n"
                + "Год: 2026";
        JOptionPane.showMessageDialog(this, message, "О разработчиках",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
        statusLabel.setText(message);
    }

    public void setStatus(String text) {
        statusLabel.setText(text);
    }

    public static void main(String[] args) {
        Preloader.loadConfig("config.properties");

        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
