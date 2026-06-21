package com.wjglerum;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SiteRenderTest {

    @Test
    void homepageServesOk() {
        given().when().get("/").then()
            .statusCode(200)
            .body(containsString("Willem Jan Glerum"));
    }
}
