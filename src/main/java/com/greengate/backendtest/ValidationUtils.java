package com.greengate.backendtest;

import org.apache.commons.lang3.StringUtils;

import java.util.Currency;

/**
 * Useful methods
 */
public class ValidationUtils {

    /**
     * Checks if currencyCode is in ISO4217
     * @param currencyCode
     * @return
     */
    public static boolean isValidCurrencyCode(String currencyCode) {
        if (StringUtils.isBlank(currencyCode)) {
            return false;
        }
        try {
            Currency.getInstance(currencyCode);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
