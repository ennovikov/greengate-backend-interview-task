package com.greengate.backendtest;

import com.google.gson.Gson;
import com.greengate.backendtest.model.ExchangeRateResult;
import com.greengate.backendtest.model.LocalDate;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.reactive.Separator;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Set;

import static com.greengate.backendtest.ValidationUtils.isValidCurrencyCode;

/**
 * Get the exchange rate at the specified date using api.frankfurter.app
 */
@Path("/exchange-rate")
public class ExchangeRateDateApi {

    private static final Logger LOG = LogManager.getLogger(ExchangeRateDateApi.class);
    private static final String FRANKFURTER_API_URL = "https://api.frankfurter.app";

    @Path("{date}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getExchangeRates(String date,
                                     @QueryParam("base") String base,
                                     @QueryParam("symbols") @Separator(",") Set<String> symbols) throws Exception {

        String errorMsg = validateParamsAndGetError(date, base, symbols);
        if (errorMsg != null) {
            return Response.status(404)
                    .type(MediaType.TEXT_PLAIN_TYPE)
                    .entity("{}" + errorMsg)
                    .build();
        }
        try {
            Map<String, Double> rates = fetchExchangeRateFromFrankfurter(date, base, symbols);

            ExchangeRateResult result = new ExchangeRateResult();
            result.setBase(base);
            result.setDate(new LocalDate(date));
            result.setRates(rates);
            return Response.status(200).entity(result).build();
        } catch (Exception e) {
            LOG.error("Unable to get the exchange rate: {}", e.getMessage());
            return Response.status(404)
                    .type(MediaType.TEXT_PLAIN_TYPE)
                    .entity("{}")
                    .build();
        }
    }

    private String validateParamsAndGetError(String date, String base, Set<String> symbols) {
        if (date == null) {
            return "Date must be specified";
        }

        // Check date
        try {
            java.time.LocalDate parsedDate = java.time.LocalDate.parse(date);
            if (!parsedDate.toString().equals(date)) {
                return "Invalid date";
            }
        } catch (DateTimeParseException e) {
            return "Invalid date format";
        }

        // Check base currency
        if (!isValidCurrencyCode(base)) {
            return "Invalid base currency code: " + base;
        }

        // Check symbols
        if (symbols == null || symbols.isEmpty()) {
            return "Symbols must be specified";
        }
        for (String symbol : symbols) {
            if (!isValidCurrencyCode(symbol)) {
                return "Invalid symbol currency code: " + symbol;
            }
        }
        return null;
    }

    private Map<String, Double> fetchExchangeRateFromFrankfurter(String date, String currencyFrom, Iterable<String> currenciesTo)
            throws IOException, InterruptedException, ExchangeRateFetchException {
        URI uri = URI.create(String.format("%s/%s?from=%s&to=%s", FRANKFURTER_API_URL, date, currencyFrom, String.join(",", currenciesTo)));

        // Build the GET request
        HttpRequest request = HttpRequest.newBuilder(uri)
                .GET()
                .header("Accept", "application/json")
                .build();

        // Send the request and get the response
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Check status code
        if (response.statusCode() == 200) {
            String jsonResponse = response.body();
            LOG.debug("JSON Response:\n{}", jsonResponse);

            FrankfurterResponse objResponse = new Gson().fromJson(jsonResponse, FrankfurterResponse.class);
            LOG.debug("rates: {}", objResponse.getRates().toString());
            return objResponse.getRates();
        } else {
            LOG.error(response);
            throw new ExchangeRateFetchException("Unable to fetch exchange rate from: " + FRANKFURTER_API_URL);
        }
    }

    private static class FrankfurterResponse {
        Double amount;
        String base;
        String date;
        Map<String, Double> rates;

        public Double getAmount() {
            return amount;
        }

        public void setAmount(Double amount) {
            this.amount = amount;
        }

        public String getBase() {
            return base;
        }

        public void setBase(String base) {
            this.base = base;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public Map<String, Double> getRates() {
            return rates;
        }

        public void setRates(Map<String, Double> rates) {
            this.rates = rates;
        }
    }
}
