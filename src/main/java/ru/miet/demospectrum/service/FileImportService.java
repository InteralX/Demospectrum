package ru.miet.demospectrum.service;

import ru.miet.demospectrum.dao.PopulationDAO;
import ru.miet.demospectrum.model.PopulationRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileImportService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Импортирует данные из CSV-файла в БД.
     */
    public int importFromCsv(String fileName) throws Exception {
        PopulationDAO dao = new PopulationDAO();
        int imported = 0;
        int skipped = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Пропускаем заголовок
                if (lineNumber == 1) continue;

                // Пропускаем пустые строки
                if (line.trim().isEmpty()) continue;

                try {
                    String[] parts = line.split(",");
                    if (parts.length < 6) {
                        skipped++;
                        continue;
                    }

                    // Парсим поля
                    int id = Integer.parseInt(parts[0].trim());
                    int cityId = Integer.parseInt(parts[1].trim());
                    int year = Integer.parseInt(parts[2].trim());
                    long population = Long.parseLong(parts[3].trim());
                    double changePercent = Double.parseDouble(parts[4].trim());
                    boolean isAnomaly = Boolean.parseBoolean(parts[5].trim());

                    // Проверяем, нет ли уже такой записи
                    // (по cityId + year)
                    if (dao.exists(cityId, year)) {
                        skipped++;
                        continue;
                    }

                    // Создаём запись
                    PopulationRecord record = new PopulationRecord(cityId, year, population);
                    record.setChangePercent(changePercent);
                    record.setAnomaly(isAnomaly);

                    dao.insert(record);
                    imported++;

                } catch (Exception e) {
                    System.err.println(" Строка " + lineNumber + ": " + e.getMessage());
                    skipped++;
                }
            }
        }

        System.out.println(" Импорт завершён: " + fileName);
        System.out.println("   Импортировано: " + imported);
        System.out.println("   Пропущено (уже есть или ошибки): " + skipped);
        System.out.println("   Время: " + LocalDateTime.now().format(FORMATTER));

        return imported;
    }
}