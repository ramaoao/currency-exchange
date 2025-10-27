package com.example.currencyexchange.servlet.exchangerate;

import com.example.currencyexchange.model.entity.ExchangeRate;
import com.example.currencyexchange.model.errors.CurrencyNotFoundException;
import com.example.currencyexchange.model.errors.ExchangeRateAlreadyExistsException;
import com.example.currencyexchange.model.response.ErrorResponse;
import com.example.currencyexchange.service.exchange.ExchangeRateService;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import static jakarta.servlet.http.HttpServletResponse.*;

@WebServlet(name = "ExchangeRatesServlet", urlPatterns = "/exchangeRates/*")
public class ExchangeRatesServlet extends HttpServlet {
    private final ExchangeRateService exchangeRateService = new ExchangeRateService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");

        try {
            List<ExchangeRate> exchangeRates = exchangeRateService.findAllExchangeRates();

            if (exchangeRates.isEmpty()) {
                response.setStatus(SC_NOT_FOUND);
                gson.toJson(new ErrorResponse(
                        SC_NOT_FOUND,
                        "No exchange rates were found in the database."),
                        response.getWriter());
                return;
            }

            response.setStatus(SC_OK);
            gson.toJson(exchangeRates, response.getWriter());

        } catch (SQLException e) {
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            gson.toJson(new ErrorResponse(
                    SC_INTERNAL_SERVER_ERROR,
                    "An error has occurred with the database."),
                    response.getWriter());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");

        String baseCodeParameter = request.getParameter("baseCurrencyCode");
        String targetCodeParameter = request.getParameter("targetCurrencyCode");
        String rateParameter = request.getParameter("rate");

        if (baseCodeParameter == null || baseCodeParameter.isBlank()) {
            response.setStatus(SC_BAD_REQUEST);
            gson.toJson(new ErrorResponse(
                    SC_BAD_REQUEST,
                    "The base currency code is missing in the parameter."),
                    response.getWriter());
            return;
        }

        if (targetCodeParameter == null || targetCodeParameter.isBlank()) {
            response.setStatus(SC_BAD_REQUEST);
            gson.toJson(new ErrorResponse(
                    SC_BAD_REQUEST,
                    "The target currency code is missing in the parameter."),
                    response.getWriter());
            return;
        }

        if (rateParameter == null || rateParameter.isBlank()) {
            response.setStatus(SC_BAD_REQUEST);
            gson.toJson(new ErrorResponse(
                    SC_BAD_REQUEST,
                    "The rate is missing in the parameter."),
                    response.getWriter());
            return;
        }

        BigDecimal rate;
        try {
            rate = new BigDecimal(rateParameter.trim());
        } catch (IllegalArgumentException e) {
            response.setStatus(SC_BAD_REQUEST);
            gson.toJson(new ErrorResponse(
                    SC_BAD_REQUEST,
                    "Incorrect rate format."),
                    response.getWriter());
            return;
        }

        try {
            ExchangeRate added = exchangeRateService.addExchangeRate(baseCodeParameter, targetCodeParameter, rate);
            response.setStatus(SC_CREATED);
            gson.toJson(added, response.getWriter());

        } catch (CurrencyNotFoundException e) {
            response.setStatus(SC_NOT_FOUND);
            gson.toJson(new ErrorResponse(
                    SC_NOT_FOUND,
                    e.getMessage()),
                    response.getWriter());

        } catch (ExchangeRateAlreadyExistsException e) {
            response.setStatus(SC_CONFLICT);
            gson.toJson(new ErrorResponse(
                    SC_CONFLICT,
                    e.getMessage()),
                    response.getWriter());

        } catch (SQLException e) {
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            gson.toJson(new ErrorResponse(
                    SC_INTERNAL_SERVER_ERROR,
                    "Database error occurred."),
                    response.getWriter());
        }
    }
}
