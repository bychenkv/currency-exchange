package com.bychenkv;

import com.bychenkv.model.Currency;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/currencies")
public class CurrencyServlet extends HttpServlet {
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Currency> currencies = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:/Users/mac/currency.db")) {
            try (Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery("SELECT * FROM currencies")) {
                    while (resultSet.next()) {
                        int id = resultSet.getInt("id");
                        String code = resultSet.getString("code");
                        String fullName = resultSet.getString("full_name");
                        String sign = resultSet.getString("sign");
                        currencies.add(new Currency(id, code, fullName, sign));
                    }

                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(currencies);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter pw = resp.getWriter();
        pw.write(json);
        pw.close();
    }
}
