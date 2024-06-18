package com.greengate.backendtest.model;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Invoice
 */
public class Invoice   {
  private LocalDate date = null;

  private String currency = null;

  
  private List<InvoiceLine> lines = null;

  public Invoice date(LocalDate date) {
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

  public Invoice currency(String currency) {
    this.currency = currency;
    return this;
  }

  /**
   * Get currency
   * @return currency
   **/
    public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public Invoice lines(List<InvoiceLine> lines) {
    this.lines = lines;
    return this;
  }

  public Invoice addLinesItem(InvoiceLine linesItem) {
    if (this.lines == null) {
      this.lines = new ArrayList<InvoiceLine>();
    }
    this.lines.add(linesItem);
    return this;
  }

  /**
   * Get lines
   * @return lines
   **/
    public List<InvoiceLine> getLines() {
    return lines;
  }

  public void setLines(List<InvoiceLine> lines) {
    this.lines = lines;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Invoice invoice = (Invoice) o;
    return Objects.equals(this.date, invoice.date) &&
        Objects.equals(this.currency, invoice.currency) &&
        Objects.equals(this.lines, invoice.lines);
  }

  @Override
  public int hashCode() {
    return Objects.hash(date, currency, lines);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Invoice {\n");
    
    sb.append("    date: ").append(toIndentedString(date)).append("\n");
    sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
    sb.append("    lines: ").append(toIndentedString(lines)).append("\n");
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
