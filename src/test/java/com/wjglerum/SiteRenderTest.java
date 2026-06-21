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

    @Test
    void homepageShowsTourSections() {
        given().when().get("/").then()
            .statusCode(200)
            .body(containsString("Now Playing"))
            .body(containsString("Past Tours"))
            .body(containsString("Singles"));
    }

    @Test
    void homepageShowsActiveTourWithAnchorAndCount() {
        given().when().get("/").then()
            .statusCode(200)
            .body(containsString("id=\"tour-concurrency-crossroads\""))
            .body(containsString("Concurrency Crossroads"))
            .body(containsString("Now touring"));
    }

    @Test
    void homepageShowsWrappedTour() {
        given().when().get("/").then()
            .statusCode(200)
            .body(containsString("id=\"tour-sso-quarkus-oidc\""))
            .body(containsString("SSO made easy with Quarkus OIDC"));
    }

    @Test
    void homepageRendersComputedStats() {
        given().when().get("/").then()
            .statusCode(200)
            .body(containsString("<span class=\"num\">36</span>"))
            .body(containsString("Shows"));
    }

    @Test
    void talkPageShowsTourContextAndSiblings() {
        given().when().get("/talks/2025-11-06-concurrency-crossroads-jfall/").then()
            .statusCode(200)
            .body(containsString("Part of the"))
            .body(containsString("Concurrency Crossroads"))
            .body(containsString("href=\"/#tour-concurrency-crossroads\""))
            .body(containsString("Also played at"));
    }

    @Test
    void singleDateTourShowsContextWithoutSiblingList() {
        given().when().get("/talks/2026-06-03-practical-mcp-security-jprime/").then()
            .statusCode(200)
            .body(containsString("Part of the"))
            .body(org.hamcrest.Matchers.not(containsString("Also played at")));
    }
}
