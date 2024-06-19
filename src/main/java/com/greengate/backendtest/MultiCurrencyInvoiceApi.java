package com.greengate.backendtest;

import com.google.gson.Gson;
import com.greengate.backendtest.model.ExchangeRateResult;
import com.greengate.backendtest.model.Invoice;
import com.greengate.backendtest.model.InvoiceContainer;
import com.greengate.backendtest.model.InvoiceLine;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.greengate.backendtest.ValidationUtils.isValidCurrencyCode;

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
    public RestResponse<String> getInvoiceTotal(InvoiceContainer invoiceContainer) {
        String errorMsg = validateInputAndGetError(invoiceContainer);
        if (errorMsg != null) {
            return RestResponse.status(400, "Error: " + errorMsg);
        }

        try {
            double total = getInvoiceTotal(invoiceContainer.getInvoice());
            return RestResponse.ok(roundTotal(total));

        } catch (ExchangeRateFetchException e) {
            LOG.error("Unable to get the exchange rate: {}", e.getMessage());
            return RestResponse.status(404, "Error: " + e.getMessage());
        } catch (Exception e) {
            LOG.error("Unable to calculate total: {}", e.getMessage());
            return RestResponse.status(500, "Error: " + e.getMessage());
        }

    }

    /**
     * Validate request parameters and return an error message
     * @param invoiceContainer
     * @return
     */
    private String validateInputAndGetError(InvoiceContainer invoiceContainer) {
        if (invoiceContainer == null || invoiceContainer.getInvoice() == null) {
            return "Invoice must be specified";
        }

        Invoice invoice = invoiceContainer.getInvoice();
        if (invoice.getDate() == null || invoice.getDate().getDate() == null) {
            return "Invoice date must be specified";
        }

        // Check invoice date
        try {
            String invoiceDate = invoice.getDate().getDate();
            LocalDate parsedDate = LocalDate.parse(invoiceDate);
            if (!parsedDate.toString().equals(invoiceDate)) {
                return "Invalid invoice date";
            }
        } catch (DateTimeParseException e) {
            return "Invalid invoice date format";
        }

        // Check invoice currency
        if (StringUtils.isEmpty(invoice.getCurrency())) {
            return "Invoice currency must be specified";
        }
        if (!isValidCurrencyCode(invoice.getCurrency())) {
            return "Invalid invoice currency";
        }

        // Check line items
        if (invoice.getLines().isEmpty()) {
            return "Invoice must have at least one line item";
        }
        for (InvoiceLine line : invoice.getLines()) {
            if (line.getAmount() <= 0) {
                return "Invalid invoice line amount: " + line;
            }
            if (!isValidCurrencyCode(line.getCurrency())) {
                return "Invalid invoice line currency: " + line;
            }
        }
        return null;
    }

    private Double getInvoiceTotal(Invoice invoice) throws IOException, InterruptedException, ExchangeRateFetchException {
        Map<String, Double> rates = getRatesMap(invoice);
        return invoice.getLines().stream()
                .map(invoiceLine -> {
                    Double rate = rates.get(invoiceLine.getCurrency());
                    return rate * invoiceLine.getAmount();
                })
                .reduce(0d, Double::sum);
    }

    private Map<String, Double> getRatesMap(Invoice invoice) throws ExchangeRateFetchException, IOException, InterruptedException {
        Set<String> lineCurrencies = invoice.getLines().stream()
                .map(InvoiceLine::getCurrency)
                .collect(Collectors.toSet());

        // no need to make the request if it is the same currency
        if (lineCurrencies.size() == 1 || lineCurrencies.stream().anyMatch(code -> code.equals(invoice.getCurrency()))) {
            return Collections.singletonMap(invoice.getCurrency(), 1d);
        }

        Map<String, Double> result = new HashMap<>(lineCurrencies.size());
        for (String lineCurrencyCode : lineCurrencies) {
            Double rate = fetchExchangeRate(invoice.getDate().getDate(), lineCurrencyCode, invoice.getCurrency());
            result.put(lineCurrencyCode, rate);
        }
        return result;
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

    /**
     * Fetch exchange rate from the local service
     * @param date
     * @param currencyFrom
     * @param currencyTo
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private Double fetchExchangeRate(String date, String currencyFrom, String currencyTo)
            throws IOException, InterruptedException, ExchangeRateFetchException {
        URI uri = URI.create(String.format("%s/%s?base=%s&symbols=%s", GET_EXCHANGE_RATE_API_URL, date, currencyFrom, currencyTo));

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

            // there will be one rate there only
            return objResponse.getRates().get(currencyTo);
        } else {
            LOG.error(response);
            throw new ExchangeRateFetchException("Unable to fetch exchange rate from: " + GET_EXCHANGE_RATE_API_URL);
        }
    }
}
