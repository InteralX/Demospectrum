package ru.miet.demospectrum.service;

import ru.miet.demospectrum.dao.PopulationDAO;
import ru.miet.demospectrum.model.PopulationRecord;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FileExportService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Экспортирует данные о населении в CSV-файл.
     */
    public void exportToCsv(String fileName) throws Exception {
        PopulationDAO dao = new PopulationDAO();
        List<PopulationRecord> records = dao.getAll();

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName, false))) {
            // Заголовок
            writer.println("id,city_id,year,population,change_percent,is_anomaly");

            // Данные
            for (PopulationRecord r : records) {
                // Форматируем число с точкой (американская локаль)
                String changeStr = String.format(java.util.Locale.US, "%.2f", r.getChangePercent());

                writer.println(String.format("%d,%d,%d,%d,%s,%b",
                        r.getId(),
                        r.getCityId(),
                        r.getYear(),
                        r.getPopulation(),
                        changeStr,
                        r.isAnomaly()));
            }
        }

        System.out.println(" Экспорт завершён: " + fileName);
        System.out.println("   Записей: " + records.size());
        System.out.println("   Время: " + LocalDateTime.now().format(FORMATTER));
    }
}