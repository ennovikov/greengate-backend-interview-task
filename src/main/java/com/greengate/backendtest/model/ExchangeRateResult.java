package com.greengate.backendtest.model;

import java.util.Objects;
import java.util.HashMap;
import java.util.Map;

/**
 * ExchangeRateResult
 */
public class ExchangeRateResult   {
  private LocalDate date = null;

  private String base = null;

  
  private Map<String, Double> rates = null;

  public ExchangeRateResult date(LocalDate date) {
    this.date = date;
    return this;
  }

  /**
   * Get date
   * @return date
   **/
    public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public ExchangeRateResult base(String base) {
    this.base = base;
    return this;
  }

  /**
   * Get base
   * @return base
   **/
    public String getBase() {
    return base;
  }

  public void setBase(String base) {
    this.base = base;
  }

  public ExchangeRateResult rates(Map<String, Double> rates) {
    this.rates = rates;
    return this;
  }

  public ExchangeRateResult putRatesItem(String key, Double ratesItem) {
    if (this.rates == null) {
      this.rates = new HashMap<String, Double>();
    }
    this.rates.put(key, ratesItem);
    return this;
  }

  /**
   * Get rates
   * @return rates
   **/
    public Map<String, Double> getRates() {
    return rates;
  }

  public void setRates(Map<String, Double> rates) {
    this.rates = rates;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExchangeRateResult exchangeRateResult = (ExchangeRateResult) o;
    return Objects.equals(this.date, exchangeRateResult.date) &&
        Objects.equals(this.base, exchangeRateResult.base) &&
        Objects.equals(this.rates, exchangeRateResult.rates);
  }

  @Override
  public int hashCode() {
    return Objects.hash(date, base, rates);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExchangeRateResult {\n");
    
    sb.append("    date: ").append(toIndentedString(date)).append("\n");
    sb.append("    base: ").append(toIndentedString(base)).append("\n");
    sb.append("    rates: ").append(toIndentedString(rates)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
