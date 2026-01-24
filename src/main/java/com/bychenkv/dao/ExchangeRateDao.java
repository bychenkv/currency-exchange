package com.bychenkv.dao;

import com.bychenkv.exception.CurrencyNotFoundException;
import com.bychenkv.exception.ExchangeRateNotFoundException;
import com.bychenkv.model.Currency;
import com.bychenkv.model.CurrencyCodePair;
import com.bychenkv.model.ExchangeRate;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateDao {
    private final DataSource dataSource;
    private final CurrencyDao currencyDao;

    public ExchangeRateDao(DataSource dataSource, CurrencyDao currencyDao) {
        this.dataSource = dataSource;
        this.currencyDao = currencyDao;
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

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)
        ) {
            while (resultSet.next()) {
                exchangeRates.add(getExchangeRateFromResultSet(resultSet));
            }
        }

        return exchangeRates;
    }

    public Optional<ExchangeRate> findByCodePair(CurrencyCodePair codePair) throws SQLException {
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

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, codePair.base());
            statement.setString(2, codePair.target());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(getExchangeRateFromResultSet(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    public ExchangeRate save(CurrencyCodePair codePair, double rate) throws SQLException,
                                                                            CurrencyNotFoundException {
        Currency baseCurrency = currencyDao.findByCode(codePair.base())
                .orElseThrow(() -> new CurrencyNotFoundException(codePair.base()));

        Currency targetCurrency = currencyDao.findByCode(codePair.target())
                .orElseThrow(() -> new CurrencyNotFoundException(codePair.target()));

        String sql = """
                INSERT INTO exchange_rates (base_currency_id, target_currency_id, rate)
                VALUES (?, ?, ?)""";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setInt(1, baseCurrency.getId());
            statement.setInt(2, targetCurrency.getId());
            statement.setDouble(3, rate);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating exchange rate failed, no rows affected");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    return findById(id).orElseThrow(() ->
                            new SQLException("Error retrieving created exchange rate"));
                }
            }
        }

        throw new SQLException("Creating exchange rate failed");
    }

    public ExchangeRate update(CurrencyCodePair codePair, double rate) throws SQLException,
                                                                              CurrencyNotFoundException,
                                                                              ExchangeRateNotFoundException {
        Currency baseCurrency = currencyDao.findByCode(codePair.base())
                .orElseThrow(() -> new CurrencyNotFoundException(codePair.base()));

        Currency targetCurrency = currencyDao.findByCode(codePair.target())
                .orElseThrow(() -> new CurrencyNotFoundException(codePair.target()));

        String sql = """
                UPDATE exchange_rates
                SET rate = ?
                WHERE base_currency_id = ? AND target_currency_id = ?""";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setDouble(1, rate);
            statement.setInt(2, baseCurrency.getId());
            statement.setInt(3, targetCurrency.getId());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new ExchangeRateNotFoundException(codePair);
            }

            return findByCodePair(codePair).orElseThrow(() ->
                    new SQLException("Error retrieving updated exchange rate"));
        }
    }

    private Optional<ExchangeRate> findById(int id) throws SQLException {
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
                WHERE er.id = ?""";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, id);

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
