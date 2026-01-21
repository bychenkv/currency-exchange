package com.bychenkv.dao;

import com.bychenkv.model.Currency;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CurrencyDao {
    private final static String DB_CONNECTION_URL = "jdbc:sqlite:/Users/mac/currency.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }
    }

    public List<Currency> findAll() throws SQLException {
        List<Currency> currencies = new ArrayList<>();
        String sql = "SELECT * FROM currencies";

        try (Connection connection = DriverManager.getConnection(DB_CONNECTION_URL);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)
        ) {
            while (resultSet.next()) {
                currencies.add(
                        new Currency(
                                resultSet.getInt("id"),
                                resultSet.getString("code"),
                                resultSet.getString("full_name"),
                                resultSet.getString("sign")
                        )
                );
            }
        }

        return currencies;
    }

    public Optional<Currency> findByCode(String code) throws SQLException {
        String sql = "SELECT * FROM currencies WHERE code = ?";

        try (Connection connection = DriverManager.getConnection(DB_CONNECTION_URL);
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, code);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(
                            new Currency(
                                    resultSet.getInt("id"),
                                    resultSet.getString("code"),
                                    resultSet.getString("full_name"),
                                    resultSet.getString("sign")
                            )
                    );
                }
            }
        }

        return Optional.empty();
    }
}
