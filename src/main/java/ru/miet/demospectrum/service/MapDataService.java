package ru.miet.demospectrum.service;

import ru.miet.demospectrum.config.DBConnection;
import ru.miet.demospectrum.model.City;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MapDataService {

    /**
     * Загружает города с координатами для отображения на карте.
     * Ограничиваем 100 крупнейшими городами (по населению за 2020 год).
     */
    public List<City> loadCitiesForMap(int limit) throws SQLException {
        List<City> cities = new ArrayList<>();

        String sql =
                "SELECT DISTINCT c.id, c.name, c.latitude, c.longitude, p.population " +
                        "FROM cities c " +
                        "JOIN population p ON c.id = p.city_id " +
                        "WHERE c.latitude IS NOT NULL " +
                        "  AND c.longitude IS NOT NULL " +
                        "  AND p.year = 2020 " +
                        "ORDER BY p.population DESC " +
                        "LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    City city = new City();
                    city.setId(rs.getInt("id"));
                    city.setName(rs.getString("name"));
                    city.setLatitude(rs.getDouble("latitude"));
                    city.setLongitude(rs.getDouble("longitude"));
                    cities.add(city);
                }
            }
        }
        return cities;
    }

    /**
     * Загружает города с учётом года и порога аномалии.
     * @param year Год
     * @param threshold Порог аномалии (например, 5.0)
     */
    public List<City> loadCitiesFiltered(int year, double threshold) throws SQLException {
        List<City> cities = new ArrayList<>();

        // SQL: берем города, у которых в указанном году изменение > порога (или <- порога)
        String sql =
                "SELECT DISTINCT c.id, c.name, c.latitude, c.longitude, p.change_percent " +
                        "FROM cities c " +
                        "JOIN population p ON c.id = p.city_id " +
                        "WHERE c.latitude IS NOT NULL " +
                        "  AND c.longitude IS NOT NULL " +
                        "  AND p.year = ? " +
                        "  AND ABS(p.change_percent) >= ? " +
                        "ORDER BY ABS(p.change_percent) DESC " +
                        "LIMIT 100";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, year);
            pstmt.setDouble(2, threshold);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    City city = new City();
                    city.setId(rs.getInt("id"));
                    city.setName(rs.getString("name"));
                    city.setLatitude(rs.getDouble("latitude"));
                    city.setLongitude(rs.getDouble("longitude"));
                    cities.add(city);
                }
            }
        }
        return cities;
    }

    /**
     * Проверяет, является ли город аномалией в заданном году.
     */
    public String getAnomalyType(int cityId, int year) throws SQLException {
        String sql = "SELECT is_anomaly, change_percent FROM population " +
                "WHERE city_id = ? AND year = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cityId);
            pstmt.setInt(2, year);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    boolean isAnomaly = rs.getBoolean("is_anomaly");
                    double change = rs.getDouble("change_percent");

                    if (!isAnomaly) return "normal";
                    if (change > 0) return "growth";   // рост
                    return "decline";                   // убыль
                }
            }
        }
        return "normal";
    }

    /**
     * Получает население города за указанный год.
     */
    public long getPopulation(int cityId, int year) throws SQLException {
        String sql = "SELECT population FROM population " +
                "WHERE city_id = ? AND year = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cityId);
            pstmt.setInt(2, year);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getLong("population");
            }
        }
        return 0;
    }
}