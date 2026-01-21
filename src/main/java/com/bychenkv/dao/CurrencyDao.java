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

        try (Connection connection = DriverManager.getConnection(DB_CONNECTION_URL)) {
            try (Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery("SELECT * FROM currencies")) {
                    while (resultSet.next()) {
                        int id = resultSet.getInt("id");
                        String code = resultSet.getString("code");
                        String fullName = resultSet.getString("full_name");
                        String sign = resultSet.getString("sign");

                        Currency currency = new Currency(id, code, fullName, sign);
                        currencies.add(currency);
                    }
                }
            }
        }

        return currencies;
    }

    public Optional<Currency> findByCode(String code) throws SQLException {
        Optional<Currency> currency = Optional.empty();

        try (Connection connection = DriverManager.getConnection(DB_CONNECTION_URL)) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM currencies WHERE code = ?");
            statement.setString(1, code);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String fullName = resultSet.getString("full_name");
                String sign = resultSet.getString("sign");

                currency = Optional.of(new Currency(id, code, fullName, sign));
            }

            resultSet.close();
            statement.close();
        }

        return currency;
    }
}
