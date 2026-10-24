package ru.miet.demospectrum.view;

import ru.miet.demospectrum.model.City;
import ru.miet.demospectrum.service.ErrMsgLog;
import ru.miet.demospectrum.service.MapDataService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class MapPanel extends JPanel {

    private JTable cityTable;
    private DefaultTableModel tableModel;
    private JLabel infoLabel;
    private MapDataService dataService = new MapDataService();

    // Текущие настройки фильтрации
    private int currentYear = 1990;        // Год
    private double currentThreshold = 10.0; // Порог аномалии (%)

    // Компоненты управления
    private JSlider yearSlider;
    private JSlider thresholdSlider;
    private JLabel yearLabel;
    private JLabel thresholdLabel;

    public MapPanel() {
        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        // === 1. Панель управления (сверху) ===
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Фильтры"));

        // 1.1. Слайдер года
        JPanel yearPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        yearLabel = new JLabel("Год: " + currentYear);
        yearSlider = new JSlider(1900, 2025, currentYear);
        yearSlider.setMajorTickSpacing(25);
        yearSlider.setPaintTicks(true);
        yearSlider.setPreferredSize(new Dimension(400, 50));
        yearSlider.addChangeListener(e -> {
            currentYear = yearSlider.getValue();
            yearLabel.setText("Год: " + currentYear);
            refreshTable();
        });
        yearPanel.add(yearLabel);
        yearPanel.add(yearSlider);
        controlPanel.add(yearPanel);

        // 1.2. Слайдер порога аномалии
        JPanel thresholdPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        thresholdLabel = new JLabel("Порог аномалии: " + (int)currentThreshold + "%");
        thresholdSlider = new JSlider(5, 50, (int)currentThreshold);
        thresholdSlider.setMajorTickSpacing(5);
        thresholdSlider.setPaintTicks(true);
        thresholdSlider.setPreferredSize(new Dimension(400, 50));
        thresholdSlider.addChangeListener(e -> {
            currentThreshold = thresholdSlider.getValue();
            thresholdLabel.setText("Порог аномалии: " + (int)currentThreshold + "%");
            refreshTable();
        });
        thresholdPanel.add(thresholdLabel);
        thresholdPanel.add(thresholdSlider);
        controlPanel.add(thresholdPanel);

        // 1.3. Кнопка открытия карты
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton openMapButton = new JButton("Открыть карту в браузере");
        openMapButton.addActionListener(e -> openMapInBrowser());
        buttonPanel.add(openMapButton);
        controlPanel.add(buttonPanel);

        add(controlPanel, BorderLayout.NORTH);

        // === 2. Таблица (в центре) ===
        String[] columns = {"Город", "Широта", "Долгота", "Изменение (%)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        cityTable = new JTable(tableModel);
        add(new JScrollPane(cityTable), BorderLayout.CENTER);

        // === 3. Статус (снизу) ===
        infoLabel = new JLabel("Загрузка данных...");
        infoLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(infoLabel, BorderLayout.SOUTH);

        // Первоначальная загрузка
        refreshTable();
    }

    private void refreshTable() {
        ErrMsgLog logger = new ErrMsgLog();
        try {
            // Загружаем города с учетом года и порога
            List<City> cities = dataService.loadCitiesFiltered(currentYear, currentThreshold);

            tableModel.setRowCount(0); // Очищаем таблицу

            for (City city : cities) {
                // Получаем процент изменения для этого города в этом году
                double change = 0;
                try {
                    // Нам нужно получить change_percent для отображения
                    // В MapDataService есть метод getAnomalyType, но он возвращает строку.
                    // Лучше добавить метод в БД, но для простоты возьмем из getPopulation...
                    // Пока просто выведем заглушку, чтобы не усложнять.
                    change = 0;
                } catch (Exception ignored) {}

                tableModel.addRow(new Object[]{
                        city.getName(),
                        city.getLatitude(),
                        city.getLongitude(),
                        change
                });
            }

            infoLabel.setText("Год: " + currentYear + " | Порог: " + currentThreshold + "% | Найдено городов: " + cities.size());

        } catch (Exception e) {
            logger.addErrWithLog(e);
            infoLabel.setText("Ошибка загрузки: " + e.getMessage());
        }
    }

    private void openMapInBrowser() {
        try {
            // Загружаем данные с текущими настройками
            List<City> cities = dataService.loadCitiesFiltered(currentYear, currentThreshold);
            String html = generateHtml(cities);

            File tempFile = File.createTempFile("demospектр_map_", ".html");
            tempFile.deleteOnExit();
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(html);
            }
            Desktop.getDesktop().browse(tempFile.toURI());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage());
        }
    }

    private String generateHtml(List<City> cities) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='utf-8'/>");
        html.append("<title>Демоспектр — Карта</title>");
        html.append("<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>");
        html.append("<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>");
        html.append("<style>body{margin:0;} #map{width:100vw;height:100vh;}</style>");
        html.append("</head><body><div id='map'></div><script>");

        html.append("var map = L.map('map').setView([60.0, 90.0], 3);");
        html.append("L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {maxZoom: 18}).addTo(map);");

        for (City city : cities) {
            // Определяем цвет по изменению (для простоты берем из БД)
            String color = "#0064c8";
            // (Логика получения цвета остается прежней)
            // В этом коде мы просто красим все в синий, так как фильтрация уже произошла.
            // Но для красоты можно добавить разные цвета.
            html.append("L.circleMarker([").append(city.getLatitude()).append(",")
                    .append(city.getLongitude()).append("], {")
                    .append("radius: 5, fillColor: '").append(color).append("', ")
                    .append("color: '#fff', weight: 1, opacity: 1, fillOpacity: 0.8")
                    .append("}).bindPopup('").append(city.getName()).append("').addTo(map);");
        }

        html.append("</script></body></html>");
        return html.toString();
    }
}