package com.greengate.backendtest.model;

public class LocalDate {
    String date;

    public LocalDate(String date) {
        this.date = date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "LocalDate{" +
                "date='" + date + '\'' +
                '}';
    }
}
