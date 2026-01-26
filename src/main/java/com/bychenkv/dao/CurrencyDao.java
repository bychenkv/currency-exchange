package com.bychenkv.dao;

import com.bychenkv.exception.CurrencyAlreadyExistsException;
import com.bychenkv.exception.DatabaseException;
import com.bychenkv.model.Currency;
import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CurrencyDao {
    private final DataSource dataSource;

    public CurrencyDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Currency> findAll() {
        List<Currency> currencies = new ArrayList<>();
        String sql = "SELECT * FROM currencies";

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)
        ) {
            while (resultSet.next()) {
                currencies.add(getCurrencyFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find all currencies", e);
        }

        return currencies;
    }

    public Optional<Currency> findByCode(String code) {
        String sql = "SELECT * FROM currencies WHERE code = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, code);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(getCurrencyFromResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find currency by code: " + code, e);
        }

        return Optional.empty();
    }

    public Currency save(Currency currency) {
        String sql = "INSERT INTO currencies (code, full_name, sign) VALUES (?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setString(1, currency.getCode());
            statement.setString(2, currency.getFullName());
            statement.setString(3, currency.getSign());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("No rows affected");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    currency.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("No ID obtained");
                }
            }
        } catch (SQLException e) {
            if (e instanceof SQLiteException &&
                ((SQLiteException) e).getResultCode() == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE) {
                throw new CurrencyAlreadyExistsException("Currency with code " + currency.getCode() +
                                                         " already exists", e);
            }
            throw new DatabaseException("Failed to save currency", e);
        }

        return currency;
    }

    private Currency getCurrencyFromResultSet(ResultSet resultSet) throws SQLException {
        return new Currency(
                resultSet.getInt("id"),
                resultSet.getString("code"),
                resultSet.getString("full_name"),
                resultSet.getString("sign")
        );
    }
}
