package ru.miet.demospectrum.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;

public class ErrMsgLog {

    private static final Logger log = Logger.getLogger("Demospектр");
    private static final List<Exception> errList = new ArrayList<>();
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    static {
        try {
            // Получаем корневой логгер
            Logger rootLogger = Logger.getLogger("");

            // Удаляем все стандартные обработчики (чтобы не было дублей и кракозябр)
            for (Handler h : rootLogger.getHandlers()) {
                rootLogger.removeHandler(h);
            }

            // 1. Настраиваем вывод в ФАЙЛ (с кодировкой UTF-8)
            FileHandler fileHandler = new FileHandler("logs/app.log", true); // true = дописывать
            fileHandler.setEncoding("UTF-8");
            fileHandler.setFormatter(new SimpleFormatter());
            rootLogger.addHandler(fileHandler);

            // 2. Настраиваем вывод в КОНСОЛЬ (с кодировкой UTF-8)
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setEncoding("UTF-8");
            consoleHandler.setFormatter(new SimpleFormatter());
            rootLogger.addHandler(consoleHandler);

            // Устанавливаем общий уровень логирования
            rootLogger.setLevel(Level.INFO);

        } catch (IOException e) {
            System.err.println("❌ Не удалось настроить логгер: " + e.getMessage());
        }
    }

    /**
     * Информационное сообщение (INFO).
     */
    public void info(String message) {
        log.info(message);
    }

    /**
     * Предупреждение (WARNING).
     */
    public void warn(String message) {
        log.warning(message);
    }

    /**
     * Ошибка без исключения (SEVERE).
     */
    public void error(String message) {
        log.severe(message);
    }

    /**
     * Ошибка с исключением — добавляет в список и пишет в лог.
     */
    public int addErrWithLog(Exception e) {
        errList.add(e);
        String msg = e.getClass().getSimpleName() + ": " + e.getMessage();
        log.severe(msg);
        return errList.size();
    }

    /**
     * Ошибка без записи в лог (только в список).
     */
    public int addErr(Exception e) {
        errList.add(e);
        return errList.size();
    }

    public int getErrCount() {
        return errList.size();
    }

    public List<Exception> getErrList() {
        return errList;
    }

    public void showErrText(Exception e) {
        System.err.println("Ошибка: " + e.getMessage());
    }

    public Exception makeErr(Exception e) {
        addErr(e);
        return new Exception(e);
    }

    /**
     * Логирует действие пользователя (для Лабы №4).
     */
    public void action(String action, String details) {
        log.info(String.format("[%s] %s — %s",
                LocalDateTime.now().format(TIME_FORMAT), action, details));
    }
}