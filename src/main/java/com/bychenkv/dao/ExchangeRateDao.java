package com.bychenkv.dao;

import com.bychenkv.model.Currency;
import com.bychenkv.model.ExchangeRate;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExchangeRateDao {
    private final static String DB_CONNECTION_URL = "jdbc:sqlite:/Users/mac/currency.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }
    }

    public List<ExchangeRate> findAll() throws SQLException {
        List<ExchangeRate> exchangeRates = new ArrayList<>();
        String sql = """
                SELECT er.id AS id,
                       base.id AS base_id,
                       base.code AS base_code,
                       base.full_name AS base_name,
                       base.sign AS base_sign,
                       target.id AS target_id,
                       target.code AS target_code,
                       target.full_name AS target_name,
                       target.sign AS target_sign,
                       er.rate AS rate
                FROM exchange_rates er
                JOIN currencies base ON er.base_currency_id = base.id
                JOIN currencies target ON er.target_currency_id = target.id""";

        try (Connection connection = DriverManager.getConnection(DB_CONNECTION_URL);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)
        ) {
            while (resultSet.next()) {
                exchangeRates.add(
                        new ExchangeRate(
                                resultSet.getInt("id"),
                                new Currency(
                                        resultSet.getInt("base_id"),
                                        resultSet.getString("base_code"),
                                        resultSet.getString("base_name"),
                                        resultSet.getString("base_sign")
                                ),
                                new Currency(
                                        resultSet.getInt("target_id"),
                                        resultSet.getString("target_code"),
                                        resultSet.getString("target_name"),
                                        resultSet.getString("target_sign")
                                ),
                                resultSet.getDouble("rate")
                        )
                );
            }
        }

        return exchangeRates;
    }
}
