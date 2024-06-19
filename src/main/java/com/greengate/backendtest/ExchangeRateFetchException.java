package com.greengate.backendtest;

/**
 * Failed to fetch relevant exchange rate data
 */
public class ExchangeRateFetchException extends Exception {
    public ExchangeRateFetchException(String errorMsg) {
        super(errorMsg);
    }
}
