package com.example.currencyexchange.util;

import com.example.currencyexchange.dao.CurrencyDAO;
import com.example.currencyexchange.dao.ExchangeRateDAO;
import com.example.currencyexchange.service.currency.CurrencyService;
import com.example.currencyexchange.service.exchange.ExchangeRateService;
import com.example.currencyexchange.service.exchange.ExchangeService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppInitializer implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        DatabasePreparation databasePreparation = new DatabasePreparation();
        databasePreparation.creatingTablesForDatabase();

        CurrencyDAO currencyDAO = new CurrencyDAO();
        ExchangeRateDAO exchangeRateDAO = new ExchangeRateDAO();

        CurrencyService currencyService = new CurrencyService(currencyDAO);
        ExchangeService exchangeService = new ExchangeService(exchangeRateDAO);
        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateDAO, currencyService);

        ServletContext context = sce.getServletContext();
        context.setAttribute("currencyDAO", currencyDAO);
        context.setAttribute("exchangeRateDAO", exchangeRateDAO);
        context.setAttribute("currencyService", currencyService);
        context.setAttribute("exchangeService", exchangeService);
        context.setAttribute("exchangeRateService", exchangeRateService);
    }
}
