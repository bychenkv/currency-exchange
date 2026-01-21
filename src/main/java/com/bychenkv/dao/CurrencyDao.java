package com.bychenkv.dao;

import com.bychenkv.model.Currency;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CurrencyDao {
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }
    }

    public List<Currency> findAll() throws SQLException {
        List<Currency> currencies = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:/Users/mac/currency.db")) {
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
}
