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

    @Test
    void aboutPageHasNoFoucScriptAndToggle() {
        given().when().get("/about/").then()
            .statusCode(200)
            .body(containsString("wjg-theme"))
            .body(containsString("data-theme"))
            .body(containsString("id=\"theme-toggle\""));
    }

    @Test
    void talkPageHasThemeToggle() {
        given().when().get("/talks/2025-11-06-concurrency-crossroads-jfall/").then()
            .statusCode(200)
            .body(containsString("id=\"theme-toggle\""));
    }
}
