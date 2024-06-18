package com.greengate.backendtest;

import com.google.gson.Gson;
import com.greengate.backendtest.model.ExchangeRateResult;
import com.greengate.backendtest.model.LocalDate;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

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
    public ExchangeRateResult getExchangeRates(String date,
                                               @QueryParam("base") String base,
                                               @QueryParam("symbols") String[] symbols) throws Exception {
        Map<String, Double> rates = fetchExchangeRateFromFrankfurter(date, base, symbols);
        if (rates == null) {
            return null; // TODO: handle 404
        }
        ExchangeRateResult result = new ExchangeRateResult();
        result.setBase(base);
        result.setDate(new LocalDate(date));
        result.setRates(rates);
        return result;
    }

    private Map<String, Double> fetchExchangeRateFromFrankfurter(String date, String currencyFrom, String[] currenciesTo) throws IOException, InterruptedException {
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
            return null; // TODO handle the failure
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
