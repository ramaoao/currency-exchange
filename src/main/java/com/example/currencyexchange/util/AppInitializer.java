package com.example.currencyexchange.util;

import com.example.currencyexchange.repository.CurrencyRepository;
import com.example.currencyexchange.repository.ExchangeRateRepository;
import com.example.currencyexchange.repository.ImplCurrencyRepository;
import com.example.currencyexchange.repository.ImplExchangeRateRepository;
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

        CurrencyRepository currencyRepository = new ImplCurrencyRepository();
        ExchangeRateRepository exchangeRateRepository = new ImplExchangeRateRepository();

        CurrencyService currencyService = new CurrencyService(currencyRepository);
        ExchangeService exchangeService = new ExchangeService(exchangeRateRepository);
        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateRepository, currencyService);

        ServletContext context = sce.getServletContext();
        context.setAttribute("currencyDAO", currencyRepository);
        context.setAttribute("exchangeRateDAO", exchangeRateRepository);
        context.setAttribute("currencyService", currencyService);
        context.setAttribute("exchangeService", exchangeService);
        context.setAttribute("exchangeRateService", exchangeRateService);
    }
}
