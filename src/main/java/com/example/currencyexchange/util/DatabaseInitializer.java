package com.example.currencyexchange.util;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class DatabaseInitializer implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        DatabasePreparation databasePreparation = new DatabasePreparation();
        databasePreparation.creatingTablesForDatabase();
    }
}
