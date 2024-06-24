package com.greengate.backendtest;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Comparator;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class MultiCurrencyInvoiceTest {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String ENDPOINT = "/invoice/total";

    public static Stream<Arguments> generateHappyPathTestCases() {
        var generator = new TestCasesGenerator(){};
        var testCases = generator.getHappyPathTestCases();
        return testCases
                .stream()
                .sorted(Comparator.comparing(TestCase::name))
                .map((testCase)-> Arguments.of(testCase.name(), testCase.statusCode(),
                        testCase.postBody(), testCase.responseBody()));
    }

    private static Stream<Arguments> generateFailureTestCases() {
        var generator = new TestCasesGenerator(){};
        var testCases = generator.getFailureTestCases();
        return testCases
                .stream()
                .sorted(Comparator.comparing(TestCase::name))
                .map((testCase)-> Arguments.of(testCase.name(), testCase.statusCode(),
                        testCase.postBody(), testCase.responseBody()));
    }

    @ParameterizedTest
    @MethodSource("generateHappyPathTestCases")
    public void testSuccessfulAPI(String name, int statusCode, String postBody, String expectedOutput) {
        given()
            .body(postBody)
            .contentType("application/json")
            .baseUri(BASE_URL)
        .when()
            .post(ENDPOINT)
        .then()
            .statusCode(statusCode)
            .contentType("text/plain")
            .body(equalTo(expectedOutput));
    }

    @ParameterizedTest
    @MethodSource("generateFailureTestCases")
    public void testFailedAPI(String name, int statusCode, String postBody) {
        given()
            .body(postBody)
            .contentType("application/json")
            .baseUri(BASE_URL)
        .when()
            .post(ENDPOINT)
        .then()
            .statusCode(statusCode);
    }
}
