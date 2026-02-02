package com.bychenkv.dao;

import com.bychenkv.exception.*;
import com.bychenkv.model.Currency;
import com.bychenkv.dto.CurrencyCodePair;
import com.bychenkv.model.ExchangeRate;
import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateDao {
    private final DataSource dataSource;

    public ExchangeRateDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<ExchangeRate> findAll() {
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
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find all exchange rates", e);
        }

        return exchangeRates;
    }

    public Optional<ExchangeRate> findByCodePair(CurrencyCodePair codePair) {
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
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find exchange rate for pair: " + codePair, e);
        }

        return Optional.empty();
    }

    public int save(Currency base, Currency target, BigDecimal rate) {
        String sql = """
                INSERT INTO exchange_rates (base_currency_id, target_currency_id, rate)
                VALUES (?, ?, ?)""";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setInt(1, base.getId());
            statement.setInt(2, target.getId());
            statement.setBigDecimal(3, rate);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("No rows affected");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    if (id == 0) {
                        throw new SQLException("Error retrieving created exchange rate");
                    }
                    return id;
                }
            }
        } catch (SQLiteException e) {
            if (e.getResultCode() == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE) {
                throw new ExchangeRateAlreadyExistsException("Exchange rate for pair " + base.getCode() +
                                                             target.getCode() + " already exists", e);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save exchange rate", e);
        }

        throw new DatabaseException("Failed to save exchange rate");
    }

    public void update(Currency base, Currency target, BigDecimal rate) {
        CurrencyCodePair codePair =  new CurrencyCodePair(base.getCode() + target.getCode());
        String sql = """
                UPDATE exchange_rates
                SET rate = ?
                WHERE base_currency_id = ? AND target_currency_id = ?""";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBigDecimal(1, rate);
            statement.setInt(2, base.getId());
            statement.setInt(3, target.getId());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new ExchangeRateNotFoundException(codePair);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update exchange rate", e);
        }
    }

    private ExchangeRate getExchangeRateFromResultSet(ResultSet resultSet) throws SQLException {
        return new ExchangeRate(resultSet.getInt("id"),
                getCurrencyFromResultSet(resultSet, "base"),
                getCurrencyFromResultSet(resultSet, "target"),
                resultSet.getBigDecimal("rate"));
    }

    private Currency getCurrencyFromResultSet(ResultSet resultSet, String prefix) throws SQLException {
        return new Currency(resultSet.getInt(getFullColumnName(prefix, "id")),
                resultSet.getString(getFullColumnName(prefix, "code")),
                resultSet.getString(getFullColumnName(prefix, "name")),
                resultSet.getString(getFullColumnName(prefix, "sign")));
    }

    private String getFullColumnName(String prefix, String name) {
        return prefix + "_" + name;
    }
}
