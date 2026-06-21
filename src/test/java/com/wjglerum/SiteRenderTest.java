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
    void homepageShowsOldTourUnderPastTours() {
        // reactive-quarkus last ran in 2022, well outside the twelve-month window,
        // so it is wrapped and not recently active: it belongs under Past Tours.
        String body = given().when().get("/").then().statusCode(200).extract().asString();
        org.junit.jupiter.api.Assertions.assertTrue(body.contains("id=\"tour-reactive-quarkus\""));
        org.junit.jupiter.api.Assertions.assertTrue(
            body.indexOf("id=\"tour-reactive-quarkus\"") > body.indexOf("id=\"past-tours\""),
            "reactive-quarkus should render after the Past Tours heading");
    }

    @Test
    void recentlyActiveTourListsUnderNowPlaying() {
        // practical-mcp-security wrapped (single past date) but is within twelve months,
        // so it must appear in the Now Playing region, before the Past Tours heading.
        String body = given().when().get("/").then().statusCode(200).extract().asString();
        int mcp = body.indexOf("id=\"tour-practical-mcp-security\"");
        int past = body.indexOf("id=\"past-tours\"");
        org.junit.jupiter.api.Assertions.assertTrue(mcp >= 0, "practical-mcp-security should render");
        org.junit.jupiter.api.Assertions.assertTrue(mcp < past,
            "recently active tour should appear before the Past Tours heading");
    }

    @Test
    void homepageShowsTalkTypeLabels() {
        // The scala-iot tour includes booth demos; its type badge must render on the overview.
        given().when().get("/").then()
            .statusCode(200)
            .body(containsString("class=\"tag booth\""))
            .body(containsString("Booth Demo"));
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
