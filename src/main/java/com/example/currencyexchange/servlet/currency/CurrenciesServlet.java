package com.example.currencyexchange.servlet.currency;

import com.example.currencyexchange.dao.CurrencyDAO;
import com.example.currencyexchange.model.entity.Currency;
import com.example.currencyexchange.model.errors.CurrencyAlreadyExistsException;
import com.example.currencyexchange.model.response.ErrorResponse;
import com.example.currencyexchange.service.currency.CurrencyService;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static jakarta.servlet.http.HttpServletResponse.*;
import static java.util.Currency.getInstance;

@WebServlet(name = "CurrenciesServlet", urlPatterns = "/currencies")
public class CurrenciesServlet extends HttpServlet {
    private CurrencyDAO currencyDAO;
    private CurrencyService currencyService;
    private final Gson gson = new Gson();

    @Override
    public void init() {
        this.currencyDAO = (CurrencyDAO) getServletContext().getAttribute("currencyDAO");
        this.currencyService = (CurrencyService) getServletContext().getAttribute("currencyService");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");

        try {
            List<Currency> currencies = currencyService.findAllCurrencies();

            if (currencies.isEmpty()) {
                response.setStatus(SC_NOT_FOUND);
                gson.toJson(new ErrorResponse(
                        SC_NOT_FOUND,
                        "No currencies were found in the database."),
                        response.getWriter());
                return;
            }

            response.setStatus(SC_OK);
            gson.toJson(currencies, response.getWriter());

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

        String code = request.getParameter("code");
        String name = request.getParameter("name");
        String sign = request.getParameter("sign");

        if (code == null || code.isBlank()) {
            response.setStatus(SC_BAD_REQUEST);
            gson.toJson(new ErrorResponse(
                            SC_BAD_REQUEST,
                            "The code is missing in the parameter."),
                    response.getWriter());
            return;
        }
        String codeParameter = code.trim().toUpperCase();

        if (name == null || name.isBlank()) {
            response.setStatus(SC_BAD_REQUEST);
            gson.toJson(new ErrorResponse(
                    SC_BAD_REQUEST,
                    "The name is missing in the parameter."),
                    response.getWriter());
            return;
        }

        if (sign == null || sign.isBlank()) {
            response.setStatus(SC_BAD_REQUEST);
            gson.toJson(new ErrorResponse(
                    SC_BAD_REQUEST,
                    "The sign is missing in the parameter."),
                    response.getWriter());
            return;
        }

        try {
            getInstance(codeParameter);
            Currency added = currencyService.addCurrency(code, name, sign);
            response.setStatus(SC_CREATED);
            gson.toJson(added, response.getWriter());

        } catch (IllegalArgumentException e) {
            response.setStatus(SC_BAD_REQUEST);
            gson.toJson(new ErrorResponse(
                    SC_BAD_REQUEST,
                    "Invalid currency code (does not match ISO 4217, Example: USD, RUB)"),
                    response.getWriter());

        } catch (CurrencyAlreadyExistsException e) {
            response.setStatus(SC_CONFLICT);
            gson.toJson(new ErrorResponse(
                    SC_CONFLICT,
                    e.getMessage()),
                    response.getWriter());

        } catch (SQLException e) {
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            gson.toJson(new ErrorResponse(
                    SC_INTERNAL_SERVER_ERROR,
                    "An error has occurred with the database."),
                    response.getWriter());
        }
    }
}
