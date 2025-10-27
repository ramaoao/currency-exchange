package com.example.currencyexchange.servlet.exchangerate;

import com.example.currencyexchange.dao.ExchangeRateDAO;
import com.example.currencyexchange.model.entity.ExchangeRate;
import com.example.currencyexchange.model.errors.ExchangeRateNotFoundException;
import com.example.currencyexchange.model.response.ErrorResponse;
import com.example.currencyexchange.service.exchange.ExchangeRateService;
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

@WebServlet(name = "ExchangeRateServlet", urlPatterns = "/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    private final ExchangeRateDAO exchangeRateDAO = new ExchangeRateDAO();
    private final ExchangeRateService exchangeRateService = new ExchangeRateService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");

        String pathInfo = request.getPathInfo().substring(1);

        if (pathInfo.length() != 6) {
            response.setStatus(SC_BAD_REQUEST);
            gson.toJson(new ErrorResponse(
                    SC_BAD_REQUEST,
                    "The address is missing exchange rate codes or they are entered incorrectly."),
                    response.getWriter());
            return;
        }

        String baseCode = pathInfo.substring(0, 3);
        String targetCode = pathInfo.substring(3, 6);

        try {
            ExchangeRate exchangeRate = exchangeRateService.findExchangeRateByCodes(baseCode, targetCode);
            response.setStatus(SC_OK);
            gson.toJson(exchangeRate, response.getWriter());

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

    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");

        String pathInfo = request.getPathInfo().substring(1);

        if (pathInfo.length() != 6) {
            response.setStatus(SC_BAD_REQUEST);
            gson.toJson(new ErrorResponse(
                    SC_BAD_REQUEST,
                    "The address is missing exchange rate codes or they are entered incorrectly."),
                    response.getWriter());
            return;
        }

        String rateParameter = request.getReader().readLine();
        if (rateParameter == null || rateParameter.isBlank()) {
            response.setStatus(SC_BAD_REQUEST);
            gson.toJson(new ErrorResponse(
                    SC_BAD_REQUEST,
                    "The rate is missing in the parameter."),
                    response.getWriter());
            return;
        }

        String rateValue = rateParameter.replace("rate=", "");
        if (rateValue.isBlank()) {
            response.setStatus(SC_BAD_REQUEST);
            gson.toJson(new ErrorResponse(
                    SC_BAD_REQUEST,
                    "The rate value is missing in the parameter."),
                    response.getWriter());
            return;
        }

        BigDecimal rate;
        try {
            rate = new BigDecimal(rateValue);
        } catch (NumberFormatException e) {
            response.setStatus(SC_BAD_REQUEST);
            gson.toJson(new ErrorResponse(
                    SC_BAD_REQUEST,
                    "Incorrect rate format."),
                    response.getWriter());
            return;
        }

        String baseCode = pathInfo.substring(0, 3);
        String targetCode = pathInfo.substring(3, 6);

        try {
            ExchangeRate update = exchangeRateService.updateExchangeRate(baseCode, targetCode, rate);
            response.setStatus(SC_OK);
            gson.toJson(update, response.getWriter());

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
