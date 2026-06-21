package com.wjglerum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class SetlistTest {

    static final LocalDate TODAY = LocalDate.of(2026, 6, 21);

    static final Map<String, Tours.TourMeta> META = Map.of(
        "concurrency-crossroads", new Tours.TourMeta("Concurrency Crossroads", "sub"),
        "sso-quarkus-oidc", new Tours.TourMeta("SSO made easy with Quarkus OIDC", "sub"),
        "practical-mcp-security", new Tours.TourMeta("Practical MCP Security", "sub"));

    static Talk talk(String tour, LocalDate date, String location) {
        return new Talk(tour, "Title", "/talks/x/", date,
            date.toString(), date.toString(), date.getYear(),
            location, location == null || location.equals("Online") ? null
                : location.substring(location.lastIndexOf(',') + 1).trim(),
            "Conf", TalkType.TALK, false, false);
    }

    @Test
    void futureDateMakesTourActive() {
        var group = List.of(
            talk("concurrency-crossroads", LocalDate.of(2025, 11, 6), "Ede, The Netherlands"),
            talk("concurrency-crossroads", LocalDate.of(2026, 11, 4), "Malmö, Sweden"));
        assertEquals("now-touring", Setlist.statusOf(group, TODAY));
    }

    @Test
    void allPastDatesMakeTourWrapped() {
        var group = List.of(talk("sso-quarkus-oidc", LocalDate.of(2025, 11, 13), "Marrakesh, Morocco"));
        assertEquals("wrapped", Setlist.statusOf(group, TODAY));
    }

    @Test
    void recentlyActiveWhenMostRecentWithinTwelveMonths() {
        var group = List.of(talk("practical-mcp-security", LocalDate.of(2026, 6, 3), "Sofia, Bulgaria"));
        assertTrue(Setlist.recentlyActive(group, TODAY));
    }

    @Test
    void notRecentlyActiveWhenOlderThanTwelveMonths() {
        var group = List.of(talk("sso-quarkus-oidc", LocalDate.of(2024, 10, 23), "Marrakesh, Morocco"));
        assertFalse(Setlist.recentlyActive(group, TODAY));
    }

    @Test
    void groupSortsTalksNewestFirstAndCounts() {
        var talks = List.of(
            talk("concurrency-crossroads", LocalDate.of(2025, 11, 6), "Ede, The Netherlands"),
            talk("concurrency-crossroads", LocalDate.of(2026, 11, 4), "Malmö, Sweden"),
            talk("concurrency-crossroads", LocalDate.of(2025, 7, 9), "Paris, France"));
        var groups = Setlist.group(talks, META, TODAY);
        assertEquals(1, groups.size());
        TourGroup g = groups.get(0);
        assertEquals(3, g.dateCount());
        assertEquals(LocalDate.of(2026, 11, 4), g.talks().get(0).date());
        assertEquals(LocalDate.of(2025, 7, 9), g.talks().get(2).date());
    }

    @Test
    void unknownSlugFailsFast() {
        var talks = List.of(talk("does-not-exist", LocalDate.of(2025, 1, 1), "Paris, France"));
        var ex = assertThrows(IllegalStateException.class, () -> Setlist.group(talks, META, TODAY));
        assertTrue(ex.getMessage().contains("does-not-exist"));
    }

    @Test
    void statsCountDistinctToursCitiesCountries() {
        var talks = List.of(
            talk("concurrency-crossroads", LocalDate.of(2025, 7, 9), "Paris, France"),
            talk("concurrency-crossroads", LocalDate.of(2026, 11, 4), "Malmö, Sweden"),
            talk("sso-quarkus-oidc", LocalDate.of(2022, 9, 8), "Oslo, Norway"),
            talk(null, LocalDate.of(2020, 1, 17), "Online"));
        Stats s = Setlist.stats(talks);
        assertEquals(4, s.shows());
        assertEquals(2, s.signatureTalks());
        assertEquals(3, s.cities());
        assertEquals(3, s.countries());
        assertEquals(2020, s.sinceYear());
    }
}
