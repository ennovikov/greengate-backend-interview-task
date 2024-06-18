package com.greengate.backendtest;

import com.google.gson.Gson;
import com.greengate.backendtest.model.ExchangeRateResult;
import com.greengate.backendtest.model.Invoice;
import com.greengate.backendtest.model.InvoiceLine;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.reactive.RestResponse;

import java.io.IOException;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Get the total  rate at the specified date using api.frankfurter.app
 */
@Path("/invoice/total")
public class MultiCurrencyInvoiceApi {

    private static final Logger LOG = LogManager.getLogger(MultiCurrencyInvoiceApi.class);
    private static final String GET_EXCHANGE_RATE_API_URL = "http://localhost:8080/exchange-rate"; // TODO: move to prop file

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public RestResponse<String> getInvoiceTotal(List<Invoice> invoices) {
        // TODO handle:
        //  - Failed to parse request body, including invalid/missing required input (HTTP status code `400`): An appropriate error message prefixed with `Error: `
        //  - Failed to calculate total (HTTP status code `500`): An appropriate error message prefixed with `Error:`

        double total = invoices.stream()
                .parallel()
                .map(this::getInvoiceTotal)
                .reduce(0d, Double::sum);

        return RestResponse.ok(roundTotal(total));
    }

    private Double getInvoiceTotal(Invoice invoice) {
        List<String> lineCurrencies = invoice.getLines().stream()
                .map(InvoiceLine::getCurrency)
                .collect(Collectors.toList());
        try {
            Map<String, Double> rates = fetchExchangeRate(invoice.getDate().getDate(), invoice.getCurrency(), lineCurrencies);
            return invoice.getLines().stream()
                    .map(invoiceLine -> {
                        Double rate = rates.get(invoiceLine.getCurrency());
                        return rate * invoiceLine.getAmount();
                    })
                    .reduce(0d, Double::sum);
        } catch (Exception e) {
            LOG.error("Unable to get the exchange rate: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Round to 2 decimal places
     * @param total
     * @return
     */
    private String roundTotal(double total) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        return df.format(total);
    }

    private Map<String, Double> fetchExchangeRate(String date, String currencyFrom, List<String> currenciesTo) throws IOException, InterruptedException {
        URI uri = URI.create(String.format("%s/%s?base=%s&symbols=%s", GET_EXCHANGE_RATE_API_URL, date, currencyFrom, String.join(",", currenciesTo)));

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

            ExchangeRateResult objResponse = new Gson().fromJson(jsonResponse, ExchangeRateResult.class);
            LOG.debug("rates: {}", objResponse.getRates().toString());
            return objResponse.getRates();
        } else {
            LOG.error(response);
            return null; // TODO handle the failure
        }
    }
}
