package com.bychenkv;

import com.bychenkv.dao.CurrencyDao;
import com.bychenkv.dao.ExchangeRateDao;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.sqlite.SQLiteDataSource;
import tools.jackson.databind.ObjectMapper;

@WebListener
public class AppContextListener implements ServletContextListener {
    private final static String DB_CONNECTION_URL = "jdbc:sqlite:/Users/mac/currency.db";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(DB_CONNECTION_URL);

        CurrencyDao currencyDao = new CurrencyDao(dataSource);
        ExchangeRateDao exchangeRateDao = new ExchangeRateDao(dataSource, currencyDao);
        ObjectMapper mapper = new ObjectMapper();

        ServletContext context = sce.getServletContext();
        context.setAttribute("currencyDao", currencyDao);
        context.setAttribute("exchangeRateDao", exchangeRateDao);
        context.setAttribute("mapper", mapper);
    }
}
