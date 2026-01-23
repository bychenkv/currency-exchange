package com.bychenkv.dao;

import com.bychenkv.model.Currency;
import com.bychenkv.model.ExchangeRate;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
                exchangeRates.add(getExchangeRateFromResultSet(resultSet));
            }
        }

        return exchangeRates;
    }

    public Optional<ExchangeRate> findByCodePair(String baseCode, String targetCode) throws SQLException {
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
                JOIN currencies target ON er.target_currency_id = target.id
                WHERE base_code = ? AND target_code = ?""";

        try (Connection connection = DriverManager.getConnection(DB_CONNECTION_URL);
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, baseCode);
            statement.setString(2, targetCode);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(getExchangeRateFromResultSet(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    private ExchangeRate getExchangeRateFromResultSet(ResultSet resultSet) throws SQLException {
        return new ExchangeRate(
                resultSet.getInt("id"),
                getCurrencyFromResultSet(resultSet, "base"),
                getCurrencyFromResultSet(resultSet, "target"),
                resultSet.getDouble("rate")
        );
    }

    private Currency getCurrencyFromResultSet(ResultSet resultSet, String prefix) throws SQLException {
        return new Currency(
                resultSet.getInt(getFullColumnName(prefix, "id")),
                resultSet.getString(getFullColumnName(prefix, "code")),
                resultSet.getString(getFullColumnName(prefix, "name")),
                resultSet.getString(getFullColumnName(prefix, "sign"))
        );
    }

    private String getFullColumnName(String prefix, String name) {
        return prefix + "_" + name;
    }
}
