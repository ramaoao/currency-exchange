package com.example.currencyexchange.servlet.currency;

import com.example.currencyexchange.model.entity.Currency;
import com.example.currencyexchange.model.errors.CurrencyNotFoundException;
import com.example.currencyexchange.model.response.ErrorResponse;
import com.example.currencyexchange.service.currency.CurrencyService;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

import static jakarta.servlet.http.HttpServletResponse.*;

@WebServlet(name = "CurrencyServlet", urlPatterns = "/currency/*")
public class CurrencyServlet extends HttpServlet {
    private final CurrencyService currencyService = new CurrencyService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");

        String code = request.getPathInfo().substring(1);

        if (code.length() != 3) {
            response.setStatus(SC_BAD_REQUEST);
            gson.toJson(new ErrorResponse(
                    SC_BAD_REQUEST,
                    "The currency code is missing from the address or they are entered incorrectly."),
                    response.getWriter());
            return;
        }

        try {
            Currency currency = currencyService.findCurrencyByCode(code);
            response.setStatus(SC_OK);
            gson.toJson(currency, response.getWriter());

        } catch (CurrencyNotFoundException e) {
            response.setStatus(SC_NOT_FOUND);
            gson.toJson(new ErrorResponse(
                    SC_NOT_FOUND,
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


