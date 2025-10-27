package com.example.currencyexchange.servlet.exchangerate;

import com.example.currencyexchange.model.errors.ExchangeRateNotFoundException;
import com.example.currencyexchange.model.response.ErrorResponse;
import com.example.currencyexchange.model.response.ExchangeResponse;
import com.example.currencyexchange.service.exchange.ExchangeService;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

import static jakarta.servlet.http.HttpServletResponse.*;

@WebServlet(name = "ExchangeServiceServlet", urlPatterns = "/exchange")
public class ExchangeServlet extends HttpServlet {
    private final ExchangeService exchangeService = new ExchangeService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");

        String baseCodeParameter = request.getParameter("from");
        String targetCodeParameter = request.getParameter("to");
        String amountParameter = request.getParameter("amount");

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

        if (amountParameter == null || amountParameter.isBlank()) {
            response.setStatus(SC_BAD_REQUEST);
            gson.toJson(new ErrorResponse(
                    SC_BAD_REQUEST,
                    "The amount is missing in the parameter."),
                    response.getWriter());
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountParameter.trim());
        } catch (IllegalArgumentException e) {
            response.setStatus(SC_BAD_REQUEST);
            gson.toJson(new ErrorResponse(
                    SC_BAD_REQUEST,
                    "Incorrect amount format."),
                    response.getWriter());
            return;
        }

        try {
            ExchangeResponse exchangeResponse = exchangeService.convertCurrency(baseCodeParameter, targetCodeParameter, amount);

            response.setStatus(SC_OK);
            gson.toJson(exchangeResponse, response.getWriter());

        } catch (ExchangeRateNotFoundException e) {
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
