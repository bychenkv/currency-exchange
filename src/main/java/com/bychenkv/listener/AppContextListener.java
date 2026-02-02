package com.bychenkv.listener;

import com.bychenkv.dao.CurrencyDao;
import com.bychenkv.dao.ExchangeRateDao;
import com.bychenkv.service.CurrencyService;
import com.bychenkv.service.ExchangeRateService;
import com.bychenkv.service.ExchangeService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.sqlite.SQLiteDataSource;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;

@WebListener
public class AppContextListener implements ServletContextListener {
    private static final String SQLITE_PREFIX = "jdbc:sqlite:";
    private static final String CURRENCY_DB_PATH_ENV = "CURRENCY_DB_PATH";
    private static final String LOCAL_DB_PATH = "/Users/mac/currency.db";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        DataSource dataSource = configureDataSource();
        configureServices(sce, dataSource);

        sce.getServletContext().setAttribute("mapper", new ObjectMapper());
    }

    private static DataSource configureDataSource() {
        String dbPath = System.getenv(CURRENCY_DB_PATH_ENV);
        if (dbPath == null) {
            dbPath = LOCAL_DB_PATH;
        }

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(SQLITE_PREFIX + dbPath);

        return dataSource;
    }

    private static void configureServices(ServletContextEvent sce, DataSource dataSource) {
        CurrencyDao currencyDao = new CurrencyDao(dataSource);
        ExchangeRateDao exchangeRateDao = new ExchangeRateDao(dataSource);

        ExchangeService exchangeService = new ExchangeService(exchangeRateDao);
        CurrencyService currencyService = new CurrencyService(currencyDao);
        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateDao, currencyDao);

        ServletContext context = sce.getServletContext();
        context.setAttribute("currencyService", currencyService);
        context.setAttribute("exchangeRateService", exchangeRateService);
        context.setAttribute("exchangeService", exchangeService);
    }
}
