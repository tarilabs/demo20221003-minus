package org.acme;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;


@QuarkusTest
public class TaxCalculatorTest {
    private static final TypeReference<Map<String, Object>> MAP_STRING_OBJECT = new TypeReference<Map<String, Object>>() {};

    static {
      RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    public void testScenarioStock() throws Exception {
        var body = """
            {
              "Movements": [
                {
                  "type": "Stock",
                  "entry": 1000,
                  "exit": 1500
                },
                {
                  "type": "Bond",
                  "entry": 1000,
                  "exit": 900
                }
              ]
            }
            """;
        var fromREST = given()
          .body(body)
          .contentType(ContentType.JSON)
          .when()
            .post("/taxcalculator")
          .then()
            .statusCode(200)
            .extract().response();
        Map<String, Object> response = new ObjectMapper().readValue(fromREST.asByteArray(), MAP_STRING_OBJECT);
        assertThat(response)
          .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
          .containsEntry("different gain tax", 130.0)
          .containsEntry("capital gain tax", 0)
          .containsEntry("minus", -100)
          .containsEntry("total tax", 30.0)
          ;
    }

    @Test
    public void testScenarioETF() throws Exception {
        var body = """
            {
              "Movements": [
                {
                  "type": "ETF",
                  "entry": 1000,
                  "exit": 1200
                },
                {
                  "type": "Bond",
                  "entry": 1000,
                  "exit": 900
                }
              ]
            }
            """;
        var fromREST = given()
          .body(body)
          .contentType(ContentType.JSON)
          .when()
            .post("/taxcalculator")
          .then()
            .statusCode(200)
            .extract().response();
        Map<String, Object> response = new ObjectMapper().readValue(fromREST.asByteArray(), MAP_STRING_OBJECT);
        assertThat(response)
          .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
          .containsEntry("different gain tax", 0.0)
          .containsEntry("capital gain tax", 52.0)
          .containsEntry("minus", -100)
          .containsEntry("total tax", 52.0)
          ;
    }
}
