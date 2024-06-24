package com.greengate.backendtest;

import java.util.List;

public interface TestCasesGenerator {

    default List<TestCase> getHappyPathTestCases() {
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
                        """, "1600.86")
        );
    }

    default List<TestCase> getFailureTestCases() {
        return List.of(
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
                        """, ""),

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
                        """, ""),

                new TestCase("Invalid Invoice Currency", 400, """
                        {
                          "invoice": {
                            "currency": "111",
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
                        """, "")

        );
    }
}
