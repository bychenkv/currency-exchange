package com.bychenkv.dao;

import com.bychenkv.dto.request.CurrencyRequestDto;
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
    private static final String FIND_ALL_QUERY = "SELECT * FROM currencies";
    private static final String FIND_BY_CODE_QUERY = "SELECT * FROM currencies WHERE code = ?";
    private static final String SAVE_QUERY = """
            INSERT INTO currencies (code, full_name, sign)
            VALUES (?, ?, ?)""";

    private final DataSource dataSource;

    public CurrencyDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Currency> findAll() {
        List<Currency> currencies = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(FIND_ALL_QUERY)
        ) {
            while (resultSet.next()) {
                Currency currency = getCurrencyFromResultSet(resultSet);
                currencies.add(currency);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find all currencies", e);
        }

        return currencies;
    }

    public Optional<Currency> findByCode(String code) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_CODE_QUERY)
        ) {
            statement.setString(1, code);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(getCurrencyFromResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find currency " + code, e);
        }

        return Optional.empty();
    }

    public int save(CurrencyRequestDto currency) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SAVE_QUERY, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setString(1, currency.code());
            statement.setString(2, currency.name());
            statement.setString(3, currency.sign());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("No rows affected");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
                throw new SQLException("No generated keys returned");
            }
        } catch (SQLiteException e) {
            if (e.getResultCode() == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE) {
                throw new CurrencyAlreadyExistsException(currency.code());
            }
            throw new DatabaseException("Failed to save currency", e);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save currency", e);
        }
    }

    private static Currency getCurrencyFromResultSet(ResultSet resultSet) throws SQLException {
        return new Currency(
                resultSet.getInt("id"),
                resultSet.getString("code"),
                resultSet.getString("full_name"),
                resultSet.getString("sign")
        );
    }
}
