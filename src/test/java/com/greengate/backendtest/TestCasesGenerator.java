package com.greengate.backendtest;

import java.util.List;

public interface TestCasesGenerator {

    default List<TestCase> getTestCases() {
        return List.of(
                new TestCase("Happy Path", 200, """
                        {
                          "invoice": {
                            "currency": "NZD",
                            "date": "2020-07-07",
                            "lines": [
                              {
                                "description": "Intel Core i9",
                                "currency": "USD",
                                "amount": 700
                              },
                              {
                                "description": "ASUS ROG Strix",
                                "currency": "AUD",
                                "amount": 500
                              }
                            ]
                          }
                        }
                        """, "1600.86"),

                new TestCase("Invalid Date", 400, """
                        {
                          "invoice": {
                            "currency": "NZD",
                            "date": "xxx",
                            "lines": [
                              {
                                "description": "Intel Core i9",
                                "currency": "USD",
                                "amount": 700
                              },
                              {
                                "description": "ASUS ROG Strix",
                                "currency": "AUD",
                                "amount": 500
                              }
                            ]
                          }
                        }
                        """, "Error: Invalid invoice date format"),

                new TestCase("Missing Date", 400, """
                        {
                          "invoice": {
                            "currency": "NZD",
                            "lines": [
                              {
                                "description": "Intel Core i9",
                                "currency": "USD",
                                "amount": 700
                              },
                              {
                                "description": "ASUS ROG Strix",
                                "currency": "AUD",
                                "amount": 500
                              }
                            ]
                          }
                        }
                        """, "Error: Invoice date must be specified"),

                new TestCase("Invalid Invoice Currency", 400, """
                        {
                          "invoice": {
                            "currency": "???",
                            "date": "2020-07-07",
                            "lines": [
                              {
                                "description": "Intel Core i9",
                                "currency": "USD",
                                "amount": 700
                              },
                              {
                                "description": "ASUS ROG Strix",
                                "currency": "AUD",
                                "amount": 500
                              }
                            ]
                          }
                        }
                        """, "Error: Invalid invoice currency"),

                new TestCase("Invalid Line Currency ", 400, """
                        {
                          "invoice": {
                            "currency": "NZD",
                            "date": "2020-07-07",
                            "lines": [
                              {
                                "description": "Intel Core i9",
                                "currency": "???",
                                "amount": 700
                              },
                              {
                                "description": "ASUS ROG Strix",
                                "currency": "AUD",
                                "amount": 500
                              }
                            ]
                          }
                        }
                        """, "Error: Invalid invoice line currency: ???")
        );
    }
}
