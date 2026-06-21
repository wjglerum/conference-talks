# On Tour Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reframe wjglerum.nl from a flat grid of talks into a touring metaphor with computed tour status, a themed map, and light/dark theming, per `docs/superpowers/specs/2026-06-21-on-tour-redesign-design.md`.

**Architecture:** Content stays data: each talk gains a `tour:` slug in front matter. A `Tours` Roq `@DataMapping` exposes `data/tours.yml`. A `@Named("setlist")` CDI bean (`Setlist`) groups the talks collection into tours, computes status from an injectable build date, and is called from Qute via the `cdi:` namespace (the same mechanism `cities` already uses). The status and grouping logic is pure and static so it is unit-tested without Quarkus. Theming is CSS custom properties swapped by a `data-theme` attribute, set before first paint by an inline script and toggled by a three-state header button that dispatches a `themechange` event the map listens to.

**Tech Stack:** Quarkus 3.36.2, Quarkus Roq, Qute templates, Java 25, JUnit 5 + REST Assured, Leaflet + CARTO tiles, GitHub Actions + Pages.

## Global Constraints

- Java release is 25 (`maven.compiler.release=25`); CI uses `java-version: '25'`.
- No runtime backend. The site is a static Roq build deployed to GitHub Pages.
- Content stays data: a talk is one markdown file with YAML front matter under `src/main/resources/content/talks/`.
- Never use an em dash in any document, comment, copy, or commit message. Use hyphens or rewrite.
- Qute build validation checks `cdi:` and `inject:` expressions, so every property and method referenced in a template must exist on the bean or its returned record types, or the build fails.
- The map `<script>` in `index.html` lives inside a Qute raw block `{| ... |}` and uses escaped braces `\{ \}` in the inline JSON. Keep all map JavaScript inside that raw block; do not introduce new unescaped `{` or `}` outside it.
- The project's preferred way to run tests is the Quarkus Dev MCP `devui-testing_runTests` tool via a subagent (see `AGENTS.md`). The `./mvnw test` commands below are the equivalent reproducible form; use whichever the executing environment supports, but do not run `mvn clean` while dev mode is running.
- All build-time date logic reads the date from the `Setlist` bean's `today()` method, never `LocalDate.now()` directly outside that method, so tests stay deterministic.

---

## File Structure

**Create:**
- `data/tours.yml` lives at `src/main/resources/data/tours.yml`. Tour slug to title and subtitle.
- `src/main/java/com/wjglerum/Tours.java`. Roq `@DataMapping("tours")` record, nested `TourMeta`.
- `src/main/java/com/wjglerum/Talk.java`. Render-ready view of one show (date strings, location, flags).
- `src/main/java/com/wjglerum/TourGroup.java`. One tour: metadata, its talks, count, status, badge flag.
- `src/main/java/com/wjglerum/Stats.java`. The five computed hero numbers.
- `src/main/java/com/wjglerum/Setlist.java`. `@Named("setlist")` bean: adapts Roq pages to `Talk`, groups, computes status, plus pure static logic.
- `src/main/resources/templates/theme-head.html`. Inline no-FOUC script for `<head>`.
- `src/main/resources/templates/theme-toggle.html`. Three-state toggle button and its script.
- `src/main/resources/templates/tour-block.html`. One rendered tour block, included per tour.
- `src/test/java/com/wjglerum/SetlistTest.java`. Pure unit tests for status, grouping, stats, validation.
- `src/test/java/com/wjglerum/SiteRenderTest.java`. `@QuarkusTest` + REST Assured render assertions.

**Modify:**
- All 33 toured talk files under `src/main/resources/content/talks/` gain a `tour:` line.
- `src/main/resources/application.properties`. Add `%test.setlist.today`.
- `src/main/resources/templates/layouts/main.html`. Theme head include, toggle in header.
- `src/main/resources/templates/layouts/talk.html`. Theme head include, toggle in header, tour context block.
- `src/main/resources/content/index.html`. Rewritten to the setlist layout with themed map.
- `src/main/resources/public/site.css`. New token palette, theme overrides, new components.
- `.github/workflows/deploy.yml`. Scheduled daily rebuild.

---

## Task 1: Tour data and front matter

**Files:**
- Create: `src/main/resources/data/tours.yml`
- Create: `src/main/java/com/wjglerum/Tours.java`
- Modify: 33 files under `src/main/resources/content/talks/`
- Test: `src/test/java/com/wjglerum/SiteRenderTest.java` (one bootstrap test, expanded in later tasks)

**Interfaces:**
- Produces: `Tours` record with `Map<String, Tours.TourMeta> list()`; `Tours.TourMeta` with `String title()`, `String subtitle()`. Accessed in templates as `cdi:tours.list`.

- [ ] **Step 1: Create the tour metadata file**

Create `src/main/resources/data/tours.yml`:

```yaml
list:
  concurrency-crossroads:
    title: "Concurrency Crossroads"
    subtitle: "Reactive Programming vs. Virtual Threads in Quarkus"
  secure-ai-agents:
    title: "Secure AI Agents"
    subtitle: "Building secure AI agents with Quarkus LangChain4j"
  practical-mcp-security:
    title: "Practical MCP Security"
    subtitle: "Securing the Model Context Protocol in practice"
  sso-quarkus-oidc:
    title: "SSO made easy with Quarkus OIDC"
    subtitle: "Single sign-on for Quarkus apps with OpenID Connect"
  reactive-quarkus:
    title: "Reactive Quarkus"
    subtitle: "Building reactive applications on Quarkus"
  scala-iot:
    title: "The Scala era"
    subtitle: "Akka, clustering and IoT demos from the Scala years"
```

- [ ] **Step 2: Create the Tours data mapping**

Create `src/main/java/com/wjglerum/Tours.java`:

```java
package com.wjglerum;

import java.util.Map;

import io.quarkiverse.roq.data.runtime.annotations.DataMapping;

@DataMapping("tours")
public record Tours(Map<String, TourMeta> list) {

    public record TourMeta(String title, String subtitle) {
    }
}
```

- [ ] **Step 3: Add the `tour:` slug to every toured talk file**

Run this from the repo root. It inserts a `tour:` line as the first front matter key (right after the opening `---`) in each listed file:

```bash
cd src/main/resources/content/talks

add_tour() { awk -v slug="$2" 'NR==1{print; print "tour: " slug; next} {print}' "$1" > "$1.tmp" && mv "$1.tmp" "$1"; }

add_tour 2025-07-09-virtual-threads-reactive-rivieradev.md concurrency-crossroads
add_tour 2025-09-16-concurrency-crossroads-java-forum-nord.md concurrency-crossroads
add_tour 2025-09-16-virtual-threads-quarkus-insights.md concurrency-crossroads
add_tour 2025-10-13-virtual-threads-reactive-javacro.md concurrency-crossroads
add_tour 2025-11-06-concurrency-crossroads-jfall.md concurrency-crossroads
add_tour 2025-11-14-concurrency-crossroads-devoxx-ma.md concurrency-crossroads
add_tour 2026-04-21-concurrency-crossroads-ocx.md concurrency-crossroads
add_tour 2026-04-22-concurrency-crossroads-devoxx-fr.md concurrency-crossroads
add_tour 2026-06-03-concurrency-crossroads-jprime.md concurrency-crossroads
add_tour 2026-11-04-concurrency-crossroads-oredev.md concurrency-crossroads

add_tour 2025-04-11-local-ai-agents-lunatech.md secure-ai-agents
add_tour 2025-09-02-secure-ai-agents-javazone.md secure-ai-agents
add_tour 2025-10-06-secure-ai-agents-devoxx-be.md secure-ai-agents
add_tour 2026-03-11-secure-ai-agents-javaland.md secure-ai-agents
add_tour 2026-06-19-secure-ai-agents-devoxxpl.md secure-ai-agents
add_tour 2026-09-01-secure-ai-agents-javazone.md secure-ai-agents

add_tour 2026-06-03-practical-mcp-security-jprime.md practical-mcp-security

add_tour 2022-03-31-sso-quarkus-oidc-quarkus-meetup.md sso-quarkus-oidc
add_tour 2022-09-08-sso-quarkus-oidc-javazone.md sso-quarkus-oidc
add_tour 2024-07-09-sso-quarkus-oidc-rivieradev.md sso-quarkus-oidc
add_tour 2024-10-04-sso-quarkus-oidc-lunaconf.md sso-quarkus-oidc
add_tour 2024-10-23-sso-quarkus-oidc-ocx.md sso-quarkus-oidc
add_tour 2025-05-14-sso-quarkus-oidc-jprime.md sso-quarkus-oidc
add_tour 2025-10-14-sso-quarkus-oidc-javacro.md sso-quarkus-oidc
add_tour 2025-11-13-sso-quarkus-oidc-devoxx-ma.md sso-quarkus-oidc

add_tour 2021-09-17-reactive-quarkus-lunaconf.md reactive-quarkus
add_tour 2021-11-19-reactive-quarkus-devoxxua.md reactive-quarkus
add_tour 2022-06-22-reactive-quarkus-devoxxpl.md reactive-quarkus
add_tour 2022-10-06-reactive-quarkus-devoxx-ma.md reactive-quarkus

add_tour 2016-06-15-iot-scala-booth-scaladays.md scala-iot
add_tour 2017-05-30-iot-scala-booth-scaladays.md scala-iot
add_tour 2018-04-20-visualising-iot-scalaua.md scala-iot
add_tour 2018-05-15-akka-cluster-booth-scaladays.md scala-iot

cd -
```

- [ ] **Step 4: Verify the front matter edits**

Run: `grep -L "^tour:" src/main/resources/content/talks/*.md`
Expected: exactly three files printed (the singles): `2018-02-09-essential-linux-lunatech.md`, `2020-01-17-hacking-room-raspberry-lunaconf.md`, `2023-10-19-quarkus-java21-meetup.md`. Every other file has a `tour:` line.

Run: `grep -c "^tour:" src/main/resources/content/talks/*.md | grep -c ":1"`
Expected: `33`

- [ ] **Step 5: Write the bootstrap render test**

Create `src/test/java/com/wjglerum/SiteRenderTest.java`:

```java
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
```

- [ ] **Step 6: Run the test to verify the data and bean load**

Run: `./mvnw test -Dtest=SiteRenderTest`
Expected: PASS. This proves `Tours.java` compiles, `tours.yml` deserializes, and the still-old homepage renders with the new front matter present.

- [ ] **Step 7: Commit**

```bash
git add src/main/resources/data/tours.yml src/main/java/com/wjglerum/Tours.java src/main/resources/content/talks/ src/test/java/com/wjglerum/SiteRenderTest.java
git commit -m "Add tour data mapping and tour slugs to talk front matter"
```

---

## Task 2: Setlist grouping, status, and stats logic

**Files:**
- Create: `src/main/java/com/wjglerum/Talk.java`
- Create: `src/main/java/com/wjglerum/TourGroup.java`
- Create: `src/main/java/com/wjglerum/Stats.java`
- Create: `src/main/java/com/wjglerum/Setlist.java`
- Create: `src/test/java/com/wjglerum/SetlistTest.java`
- Modify: `src/main/resources/application.properties`

**Interfaces:**
- Consumes: `Tours` and `Tours.TourMeta` from Task 1; Roq `io.quarkiverse.roq.frontmatter.runtime.model.DocumentPage` (provides `date()` returning `ZonedDateTime`, `title()`, `url()` returning `RoqUrl` with `path()`, and `data(String)` returning `Object`); `RoqCollection extends ArrayList<DocumentPage>`.
- Produces:
  - `Talk(String tour, String title, String url, LocalDate date, String dateShort, String dateLong, int year, String location, String country, String conference, String type, boolean hasVideo, boolean upcoming)`
  - `TourGroup(String slug, String title, String subtitle, List<Talk> talks, int dateCount, String status, boolean recentlyActive)` where `status` is `"now-touring"` or `"wrapped"`
  - `Stats(int shows, int signatureTalks, int cities, int countries, int sinceYear)`
  - Bean `Setlist` methods called from templates: `nowPlaying(List<DocumentPage>)`, `pastTours(List<DocumentPage>)`, `singles(List<DocumentPage>)`, `stats(List<DocumentPage>)`, `tour(List<DocumentPage>, String slug)`, `siblings(List<DocumentPage>, String slug, String currentUrl)`
  - Pure static methods used by tests: `Setlist.group(List<Talk>, Map<String,Tours.TourMeta>, LocalDate)`, `Setlist.statusOf(List<Talk>, LocalDate)`, `Setlist.recentlyActive(List<Talk>, LocalDate)`, `Setlist.stats(List<Talk>)`

- [ ] **Step 1: Write the failing unit tests**

Create `src/test/java/com/wjglerum/SetlistTest.java`:

```java
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
            "Conf", "talk", false, false);
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
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `./mvnw test -Dtest=SetlistTest`
Expected: FAIL to compile (`Talk`, `TourGroup`, `Stats`, `Setlist` do not exist yet).

- [ ] **Step 3: Create the record types**

Create `src/main/java/com/wjglerum/Talk.java`:

```java
package com.wjglerum;

import java.time.LocalDate;

public record Talk(
        String tour,
        String title,
        String url,
        LocalDate date,
        String dateShort,
        String dateLong,
        int year,
        String location,
        String country,
        String conference,
        String type,
        boolean hasVideo,
        boolean upcoming) {
}
```

Create `src/main/java/com/wjglerum/TourGroup.java`:

```java
package com.wjglerum;

import java.util.List;

public record TourGroup(
        String slug,
        String title,
        String subtitle,
        List<Talk> talks,
        int dateCount,
        String status,
        boolean recentlyActive) {
}
```

Create `src/main/java/com/wjglerum/Stats.java`:

```java
package com.wjglerum;

public record Stats(int shows, int signatureTalks, int cities, int countries, int sinceYear) {
}
```

- [ ] **Step 4: Create the Setlist bean**

Create `src/main/java/com/wjglerum/Setlist.java`:

```java
package com.wjglerum;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkiverse.roq.frontmatter.runtime.model.DocumentPage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named("setlist")
@ApplicationScoped
public class Setlist {

    private static final DateTimeFormatter SHORT = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter LONG = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);

    private final Tours tours;
    private final String todayOverride;

    public Setlist(Tours tours,
            @ConfigProperty(name = "setlist.today", defaultValue = "") String todayOverride) {
        this.tours = tours;
        this.todayOverride = todayOverride;
    }

    LocalDate today() {
        return todayOverride.isBlank() ? LocalDate.now() : LocalDate.parse(todayOverride);
    }

    // ---- template entry points ----

    public List<TourGroup> nowPlaying(List<DocumentPage> pages) {
        LocalDate today = today();
        return group(toTalks(pages), tours.list(), today).stream()
                .filter(g -> g.status().equals("now-touring"))
                .sorted(Comparator.comparing(g -> nextOrLastDate(g, today)))
                .toList();
    }

    public List<TourGroup> pastTours(List<DocumentPage> pages) {
        return group(toTalks(pages), tours.list(), today()).stream()
                .filter(g -> g.status().equals("wrapped"))
                .sorted(Comparator.comparing(Setlist::mostRecent).reversed())
                .toList();
    }

    public List<Talk> singles(List<DocumentPage> pages) {
        return toTalks(pages).stream()
                .filter(t -> t.tour() == null)
                .sorted(Comparator.comparing(Talk::date).reversed())
                .toList();
    }

    public Stats stats(List<DocumentPage> pages) {
        return stats(toTalks(pages));
    }

    public TourGroup tour(List<DocumentPage> pages, String slug) {
        if (slug == null) {
            return null;
        }
        return group(toTalks(pages), tours.list(), today()).stream()
                .filter(g -> g.slug().equals(slug))
                .findFirst().orElse(null);
    }

    public List<Talk> siblings(List<DocumentPage> pages, String slug, String currentUrl) {
        if (slug == null) {
            return List.of();
        }
        return toTalks(pages).stream()
                .filter(t -> slug.equals(t.tour()) && !t.url().equals(currentUrl))
                .sorted(Comparator.comparing(Talk::date).reversed())
                .toList();
    }

    // ---- adaptation from Roq pages to Talk ----

    private List<Talk> toTalks(List<DocumentPage> pages) {
        LocalDate today = today();
        List<Talk> talks = new ArrayList<>();
        for (DocumentPage p : pages) {
            LocalDate date = p.date().toLocalDate();
            String location = str(p.data("location"));
            talks.add(new Talk(
                    str(p.data("tour")),
                    p.title(),
                    p.url().path(),
                    date,
                    date.format(SHORT),
                    date.format(LONG),
                    date.getYear(),
                    location,
                    country(location),
                    str(p.data("conference")),
                    str(p.data("type")),
                    p.data("video") != null,
                    !date.isBefore(today)));
        }
        return talks;
    }

    private static String str(Object o) {
        return o == null ? null : o.toString();
    }

    private static String country(String location) {
        if (location == null || location.equals("Online")) {
            return null;
        }
        int comma = location.lastIndexOf(',');
        return comma < 0 ? null : location.substring(comma + 1).trim();
    }

    // ---- pure logic (unit-tested directly) ----

    static List<TourGroup> group(List<Talk> talks, Map<String, Tours.TourMeta> meta, LocalDate today) {
        Map<String, List<Talk>> byTour = new LinkedHashMap<>();
        for (Talk t : talks) {
            if (t.tour() == null) {
                continue;
            }
            if (!meta.containsKey(t.tour())) {
                throw new IllegalStateException("Talk \"" + t.title()
                        + "\" references unknown tour slug \"" + t.tour() + "\" not present in tours.yml");
            }
            byTour.computeIfAbsent(t.tour(), k -> new ArrayList<>()).add(t);
        }
        List<TourGroup> groups = new ArrayList<>();
        for (Map.Entry<String, List<Talk>> e : byTour.entrySet()) {
            List<Talk> sorted = e.getValue().stream()
                    .sorted(Comparator.comparing(Talk::date).reversed())
                    .toList();
            Tours.TourMeta m = meta.get(e.getKey());
            groups.add(new TourGroup(e.getKey(), m.title(), m.subtitle(),
                    sorted, sorted.size(), statusOf(sorted, today), recentlyActive(sorted, today)));
        }
        return groups;
    }

    static String statusOf(List<Talk> group, LocalDate today) {
        boolean active = group.stream().anyMatch(t -> !t.date().isBefore(today));
        return active ? "now-touring" : "wrapped";
    }

    static boolean recentlyActive(List<Talk> group, LocalDate today) {
        LocalDate cutoff = today.minusMonths(12);
        return group.stream().map(Talk::date).max(Comparator.naturalOrder())
                .map(d -> !d.isBefore(cutoff)).orElse(false);
    }

    static Stats stats(List<Talk> talks) {
        int shows = talks.size();
        long signature = talks.stream().map(Talk::tour).filter(Objects::nonNull).distinct().count();
        long cities = talks.stream().map(Talk::location)
                .filter(l -> l != null && !l.equals("Online")).distinct().count();
        long countries = talks.stream().map(Talk::country).filter(Objects::nonNull).distinct().count();
        int since = talks.stream().mapToInt(Talk::year).min().orElse(0);
        return new Stats(shows, (int) signature, (int) cities, (int) countries, since);
    }

    private static LocalDate nextOrLastDate(TourGroup g, LocalDate today) {
        return g.talks().stream().map(Talk::date)
                .filter(d -> !d.isBefore(today)).min(Comparator.naturalOrder())
                .orElseGet(() -> mostRecent(g));
    }

    private static LocalDate mostRecent(TourGroup g) {
        return g.talks().stream().map(Talk::date).max(Comparator.naturalOrder()).orElse(LocalDate.MIN);
    }
}
```

- [ ] **Step 5: Run the unit tests to verify they pass**

Run: `./mvnw test -Dtest=SetlistTest`
Expected: PASS (7 tests).

- [ ] **Step 6: Add the deterministic test date**

In `src/main/resources/application.properties`, add this line at the end so render tests in later tasks compute status against a fixed date:

```properties
%test.setlist.today=2026-06-21
```

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/wjglerum/Talk.java src/main/java/com/wjglerum/TourGroup.java src/main/java/com/wjglerum/Stats.java src/main/java/com/wjglerum/Setlist.java src/test/java/com/wjglerum/SetlistTest.java src/main/resources/application.properties
git commit -m "Add Setlist bean with tour grouping, status and stats logic"
```

---

## Task 3: Theme chrome (tokens, no-FOUC script, three-state toggle)

**Files:**
- Create: `src/main/resources/templates/theme-head.html`
- Create: `src/main/resources/templates/theme-toggle.html`
- Modify: `src/main/resources/templates/layouts/main.html`
- Modify: `src/main/resources/templates/layouts/talk.html`
- Modify: `src/main/resources/public/site.css`
- Test: `src/test/java/com/wjglerum/SiteRenderTest.java`

**Interfaces:**
- Consumes: nothing from earlier tasks (pure chrome). Independent of the homepage rewrite.
- Produces: a `data-theme` attribute on `<html>` set before first paint; a `themechange` CustomEvent dispatched on `document` whenever the resolved theme changes (Task 4's map listens for it); CSS tokens `--ground`, `--panel`, `--text`, `--dim`, `--faint`, `--accent`, `--gold`, `--rule` for both themes.

- [ ] **Step 1: Write the failing theme assertions**

Add these tests to `src/test/java/com/wjglerum/SiteRenderTest.java` (inside the class, keep existing imports; they already cover `containsString`):

```java
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
```

- [ ] **Step 2: Run to verify they fail**

Run: `./mvnw test -Dtest=SiteRenderTest`
Expected: FAIL (`wjg-theme` and `theme-toggle` not present yet).

- [ ] **Step 3: Create the no-FOUC head partial**

Create `src/main/resources/templates/theme-head.html`:

```html
<script>
(function () {
    try {
        var stored = localStorage.getItem('wjg-theme');
        var theme = (stored === 'light' || stored === 'dark') ? stored
            : (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
        document.documentElement.setAttribute('data-theme', theme);
    } catch (e) {
        document.documentElement.setAttribute('data-theme', 'dark');
    }
})();
</script>
```

- [ ] **Step 4: Create the three-state toggle partial**

Create `src/main/resources/templates/theme-toggle.html`:

```html
<button id="theme-toggle" class="theme-toggle" type="button" aria-label="Toggle color theme" title="Theme">
    <span class="ico ico-system" aria-hidden="true">◐</span>
    <span class="ico ico-light" aria-hidden="true">☀</span>
    <span class="ico ico-dark" aria-hidden="true">☾</span>
</button>
<script>
(function () {
    var root = document.documentElement;
    var btn = document.getElementById('theme-toggle');
    if (!btn) return;
    var mql = window.matchMedia('(prefers-color-scheme: dark)');

    function stored() { try { return localStorage.getItem('wjg-theme'); } catch (e) { return null; } }
    function mode() { var s = stored(); return (s === 'light' || s === 'dark') ? s : 'system'; }
    function resolved() { var m = mode(); return m === 'system' ? (mql.matches ? 'dark' : 'light') : m; }

    function apply() {
        root.setAttribute('data-theme', resolved());
        root.setAttribute('data-theme-mode', mode());
        document.dispatchEvent(new CustomEvent('themechange'));
    }

    btn.addEventListener('click', function () {
        var next = { system: 'light', light: 'dark', dark: 'system' }[mode()];
        try {
            if (next === 'system') localStorage.removeItem('wjg-theme');
            else localStorage.setItem('wjg-theme', next);
        } catch (e) {}
        apply();
    });

    mql.addEventListener('change', function () { if (mode() === 'system') apply(); });

    root.setAttribute('data-theme-mode', mode());
})();
</script>
```

- [ ] **Step 5: Wire the partials into `main.html`**

In `src/main/resources/templates/layouts/main.html`, add the head include immediately after the `<meta name="viewport" ...>` line (line 5), so the theme is set before the stylesheet loads:

```html
    {#include theme-head /}
```

Then replace the existing `<nav class="nav">` block (lines 24 to 27) with this version that adds the toggle:

```html
        <nav class="nav">
            <a href="/">Talks</a>
            <a href="/about/">About</a>
            {#include theme-toggle /}
        </nav>
```

- [ ] **Step 6: Wire the partials into `talk.html`**

In `src/main/resources/templates/layouts/talk.html`, add the head include immediately after the `<meta name="viewport" ...>` line (line 5):

```html
    {#include theme-head /}
```

Then replace the header block (lines 22 to 24) with this version that adds the toggle:

```html
    <header class="site">
        <a href="/">← All talks</a>
        {#include theme-toggle /}
    </header>
```

- [ ] **Step 7: Replace the token palette in `site.css`**

In `src/main/resources/public/site.css`, replace the existing `:root { ... }` block (lines 1 to 10) with the dark and light token sets. The aliases keep every existing rule that references the old variable names working unchanged:

```css
:root,
[data-theme="dark"] {
    color-scheme: dark;
    --ground: #14110D;
    --panel: #1C1813;
    --text: #F1ECE1;
    --dim: #9C9486;
    --faint: #6E665A;
    --accent: #F0572B;
    --gold: #CCA24C;
    --rule: #2C261D;
    /* aliases so existing component rules keep working */
    --bg: var(--ground);
    --paper: var(--panel);
    --ink: var(--text);
    --muted: var(--dim);
    --accent-soft: #2A1D16;
    --recording: var(--accent);
}

[data-theme="light"] {
    color-scheme: light;
    --ground: #F7F4EC;
    --panel: #FFFFFF;
    --text: #1A1510;
    --dim: #6E665A;
    --faint: #9C9486;
    --accent: #D8441E;
    --gold: #A9802F;
    --rule: #E2DACB;
    --bg: var(--ground);
    --paper: var(--panel);
    --ink: var(--text);
    --muted: var(--dim);
    --accent-soft: #F3E2D2;
    --recording: var(--accent);
}
```

- [ ] **Step 8: Add the toggle button styles to `site.css`**

Append to the end of `src/main/resources/public/site.css`:

```css
/* Theme toggle */
.theme-toggle { background: transparent; border: 1px solid var(--rule); color: var(--dim); cursor: pointer; width: 2rem; height: 2rem; border-radius: 999px; display: inline-flex; align-items: center; justify-content: center; padding: 0; font-size: 0.95rem; line-height: 1; }
.theme-toggle:hover { color: var(--accent); border-color: var(--accent); }
.theme-toggle .ico { display: none; }
[data-theme-mode="system"] .theme-toggle .ico-system { display: inline; }
[data-theme-mode="light"] .theme-toggle .ico-light { display: inline; }
[data-theme-mode="dark"] .theme-toggle .ico-dark { display: inline; }
```

- [ ] **Step 9: Run the tests to verify they pass**

Run: `./mvnw test -Dtest=SiteRenderTest`
Expected: PASS (3 tests: the bootstrap plus the two new theme tests).

- [ ] **Step 10: Commit**

```bash
git add src/main/resources/templates/theme-head.html src/main/resources/templates/theme-toggle.html src/main/resources/templates/layouts/main.html src/main/resources/templates/layouts/talk.html src/main/resources/public/site.css src/test/java/com/wjglerum/SiteRenderTest.java
git commit -m "Add light/dark theming with no-FOUC script and three-state toggle"
```

---

## Task 4: Homepage setlist layout, sections, and themed map

**Files:**
- Create: `src/main/resources/templates/tour-block.html`
- Modify: `src/main/resources/content/index.html`
- Modify: `src/main/resources/public/site.css`
- Test: `src/test/java/com/wjglerum/SiteRenderTest.java`

**Interfaces:**
- Consumes: `cdi:setlist.stats/nowPlaying/pastTours/singles` from Task 2 (returning `Stats`, `List<TourGroup>`, `List<Talk>`); `cdi:cities.list` (unchanged); the `themechange` event and `--accent`/`--ground` tokens from Task 3.
- Produces: a homepage with `id="now-playing"`, `id="past-tours"`, `id="singles"` sections and per-tour blocks with `id="tour-<slug>"` (Task 5's talk pages link to these anchors).

- [ ] **Step 1: Write the failing homepage assertions**

Add to `src/test/java/com/wjglerum/SiteRenderTest.java`:

```java
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
```

- [ ] **Step 2: Run to verify they fail**

Run: `./mvnw test -Dtest=SiteRenderTest`
Expected: FAIL (old homepage has none of these sections).

- [ ] **Step 3: Create the tour block partial**

Create `src/main/resources/templates/tour-block.html`:

```html
<article class="tour" id="tour-{tour.slug}">
    <header class="tour-head">
        <div class="tour-status">
            {#if tour.status == 'now-touring'}<span class="pill touring"><span class="dot"></span>Now touring</span>{/if}
            {#if tour.recentlyActive && tour.status != 'now-touring'}<span class="pill new">New</span>{/if}
        </div>
        <h3 class="tour-title">{tour.title}</h3>
        <p class="tour-sub">{tour.subtitle}</p>
        <span class="tour-count"><strong>{tour.dateCount}</strong> {#if tour.dateCount == 1}date{#else}dates{/if}</span>
    </header>
    <ol class="dates">
        {#for show in tour.talks}
        <li class="{#if show.upcoming}upcoming{/if}">
            <a href="{show.url}">
                <span class="d">{show.dateShort}</span>
                <span class="conf">{show.conference}</span>
                <span class="loc">{show.location}</span>
                {#if show.hasVideo}<span class="rec">▸ Recording</span>{/if}
            </a>
        </li>
        {/for}
    </ol>
</article>
```

- [ ] **Step 4: Rewrite the homepage**

Replace the entire contents of `src/main/resources/content/index.html` with:

```html
---
title: On Tour
description: Conference talks given by Willem Jan Glerum, Principal Software Engineer at Lunatech, toured across Europe.
author: Willem Jan Glerum
layout: main
---
{#let stats=cdi:setlist.stats(site.collections.talks)}
<section class="hero">
    <div class="hero-copy">
        <h1 class="display">On Tour</h1>
        <p class="lede">A handful of signature talks, toured across Europe. Mostly Java and Quarkus, lately a lot about virtual threads and AI agents.</p>
    </div>
    <ul class="stats">
        <li><span class="num">{stats.shows}</span><span class="lbl">Shows</span></li>
        <li><span class="num">{stats.signatureTalks}</span><span class="lbl">Signature talks</span></li>
        <li><span class="num">{stats.cities}</span><span class="lbl">Cities</span></li>
        <li><span class="num">{stats.countries}</span><span class="lbl">Countries</span></li>
        <li><span class="num">{stats.sinceYear}</span><span class="lbl">Since</span></li>
    </ul>
</section>
{/let}

<section class="map-section">
    <div id="map" aria-label="Map of tour locations"></div>
</section>

<section class="tours" aria-labelledby="now-playing">
    <h2 id="now-playing">Now Playing</h2>
    {#for tour in cdi:setlist.nowPlaying(site.collections.talks)}
    {#include tour-block /}
    {/for}
</section>

<section class="tours past" aria-labelledby="past-tours">
    <h2 id="past-tours">Past Tours</h2>
    {#for tour in cdi:setlist.pastTours(site.collections.talks)}
    {#include tour-block /}
    {/for}
</section>

<section class="singles" aria-labelledby="singles">
    <h2 id="singles">Singles</h2>
    <ul class="single-list">
        {#for talk in cdi:setlist.singles(site.collections.talks)}
        <li>
            <a href="{talk.url}">
                <span class="d">{talk.dateShort}</span>
                <span class="t">{talk.title}</span>
                <span class="c">{talk.conference}</span>
                <span class="loc">{talk.location}</span>
                {#if talk.hasVideo}<span class="rec">▸ Recording</span>{/if}
            </a>
        </li>
        {/for}
    </ul>
</section>

<script id="talks-data" type="application/json">[
{#for talk in site.collections.talks}
  \{"title":"{talk.title.replace('\"','\\\"')}","url":"{talk.url}","date":"{talk.date.format('MMM yyyy')}","location":"{talk.data('location')}","video":{#if talk.data('video')}true{#else}false{/if},"coordinates":{#let coords=cdi:cities.list.get(talk.data('location'))}{#if coords}[{coords.lat},{coords.lng}]{#else}null{/if}{/let}\}{#if !talk_isLast},{/if}
{/for}
]</script>

<link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=" crossorigin="">
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=" crossorigin=""></script>
{|
<script>
(function () {
    const root = document.documentElement;
    const talks = JSON.parse(document.getElementById('talks-data').textContent);
    const grouped = {};
    talks.forEach(t => {
        if (!t.coordinates) return;
        (grouped[t.location] = grouped[t.location] || { coords: t.coordinates, talks: [] }).talks.push(t);
    });

    const map = L.map('map', { scrollWheelZoom: false, zoomControl: true, attributionControl: true });

    let base = null, labels = null;
    function tiles() {
        const variant = root.getAttribute('data-theme') === 'dark' ? 'dark' : 'light';
        if (base) map.removeLayer(base);
        if (labels) map.removeLayer(labels);
        base = L.tileLayer('https://{s}.basemaps.cartocdn.com/' + variant + '_nolabels/{z}/{x}/{y}{r}.png', {
            subdomains: 'abcd',
            maxZoom: 10,
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> &copy; <a href="https://carto.com/attributions">CARTO</a>'
        }).addTo(map);
        labels = L.tileLayer('https://{s}.basemaps.cartocdn.com/' + variant + '_only_labels/{z}/{x}/{y}{r}.png', {
            subdomains: 'abcd',
            maxZoom: 10
        }).addTo(map);
    }

    function markerIcon() {
        const cs = getComputedStyle(root);
        const accent = cs.getPropertyValue('--accent').trim();
        const ground = cs.getPropertyValue('--ground').trim();
        return L.divIcon({
            className: 'talk-marker',
            html: '<svg width="22" height="22" viewBox="0 0 22 22"><circle cx="11" cy="11" r="6" fill="' + accent + '" stroke="' + ground + '" stroke-width="2"/></svg>',
            iconSize: [22, 22],
            iconAnchor: [11, 11]
        });
    }

    let markers = [];
    const points = [];
    function drawMarkers() {
        markers.forEach(m => map.removeLayer(m));
        markers = [];
        const icon = markerIcon();
        Object.entries(grouped).forEach(([city, data]) => {
            const m = L.marker(data.coords, { icon }).addTo(map);
            const items = data.talks.map(t =>
                '<a href="' + t.url + '"><span class="yr">' + t.date + '</span>' + t.title + '</a>'
            ).join('');
            m.bindPopup('<div class="city">' + city + '</div>' + items);
            markers.push(m);
        });
    }

    Object.values(grouped).forEach(d => points.push(d.coords));
    tiles();
    drawMarkers();
    if (points.length) {
        map.fitBounds(L.latLngBounds(points), { padding: [30, 30], maxZoom: 5 });
    } else {
        map.setView([49, 8], 4);
    }

    document.addEventListener('themechange', function () { tiles(); drawMarkers(); });
})();
</script>
|}
```

- [ ] **Step 5: Add homepage component styles to `site.css`**

Append to the end of `src/main/resources/public/site.css`:

```css
/* Homepage: hero and stats */
.display { font-family: ui-sans-serif, system-ui, sans-serif; text-transform: uppercase; letter-spacing: -0.02em; font-weight: 800; font-size: clamp(2.5rem, 8vw, 5rem); line-height: 0.95; color: var(--text); }
.hero-copy .lede { color: var(--dim); max-width: 48ch; margin-top: 1rem; font-size: 1.05rem; }
.stats { list-style: none; display: flex; flex-wrap: wrap; gap: 2rem; margin-top: 2rem; padding-top: 1.5rem; border-top: 1px solid var(--rule); }
.stats li { display: flex; flex-direction: column; }
.stats .num { font-size: 2rem; font-weight: 800; color: var(--gold); line-height: 1; }
.stats .lbl { font-size: 0.7rem; letter-spacing: 0.1em; text-transform: uppercase; color: var(--faint); margin-top: 0.35rem; }

/* Map */
.map-section { margin: 2.5rem 0; }
#map { height: 360px; width: 100%; border: 1px solid var(--rule); background: var(--panel); }

/* Tour sections */
.tours { margin-top: 3rem; }
.tours h2, .singles h2 { text-transform: uppercase; letter-spacing: 0.06em; font-size: 1rem; color: var(--dim); border-bottom: 1px solid var(--rule); padding-bottom: 0.5rem; margin-bottom: 1.5rem; }
.tour { background: var(--panel); border: 1px solid var(--rule); padding: 1.5rem; margin-bottom: 1.25rem; }
.tours.past .tour { padding: 1rem 1.5rem; }
.tour-status { display: flex; gap: 0.5rem; margin-bottom: 0.5rem; min-height: 1.4rem; }
.pill { font-size: 0.7rem; letter-spacing: 0.08em; text-transform: uppercase; padding: 0.2rem 0.6rem; border-radius: 999px; display: inline-flex; align-items: center; gap: 0.4rem; }
.pill.touring { background: var(--accent); color: var(--ground); }
.pill.new { background: transparent; border: 1px solid var(--gold); color: var(--gold); }
.pill .dot { width: 0.5rem; height: 0.5rem; border-radius: 999px; background: var(--ground); animation: pulse 1.6s ease-in-out infinite; }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.3; } }
@media (prefers-reduced-motion: reduce) { .pill .dot { animation: none; } }
.tour-title { font-size: 1.5rem; font-weight: 700; color: var(--text); letter-spacing: -0.01em; }
.tour-sub { color: var(--dim); margin-top: 0.2rem; }
.tour-count { display: inline-block; margin-top: 0.6rem; color: var(--faint); font-size: 0.85rem; }
.tour-count strong { color: var(--gold); font-size: 1.1rem; }
.dates { list-style: none; margin-top: 1rem; border-top: 1px solid var(--rule); }
.dates li { border-bottom: 1px solid var(--rule); }
.dates li a { display: grid; grid-template-columns: 6rem 1fr auto auto; gap: 1rem; align-items: baseline; padding: 0.6rem 0; color: var(--text); text-decoration: none; }
.dates li a:hover { color: var(--accent); }
.dates .d { color: var(--dim); font-size: 0.85rem; }
.dates .loc { color: var(--faint); font-size: 0.85rem; }
.dates .rec { color: var(--accent); font-size: 0.8rem; }
.dates li.upcoming a { color: var(--gold); }

/* Singles */
.singles { margin-top: 3rem; }
.single-list { list-style: none; }
.single-list li { border-bottom: 1px solid var(--rule); }
.single-list li a { display: grid; grid-template-columns: 6rem 1fr auto auto; gap: 1rem; align-items: baseline; padding: 0.6rem 0; color: var(--text); text-decoration: none; }
.single-list li a:hover { color: var(--accent); }
.single-list .d, .single-list .c, .single-list .loc { color: var(--dim); font-size: 0.85rem; }
.single-list .rec { color: var(--accent); }

@media (max-width: 640px) {
    .dates li a, .single-list li a { grid-template-columns: 1fr; gap: 0.15rem; }
}
```

- [ ] **Step 6: Run the tests to verify they pass**

Run: `./mvnw test -Dtest=SiteRenderTest`
Expected: PASS (all homepage tests plus the earlier theme tests).

- [ ] **Step 7: Commit**

```bash
git add src/main/resources/templates/tour-block.html src/main/resources/content/index.html src/main/resources/public/site.css src/test/java/com/wjglerum/SiteRenderTest.java
git commit -m "Rewrite homepage as setlist with tour sections and themed map"
```

---

## Task 5: Talk detail tour context

**Files:**
- Modify: `src/main/resources/templates/layouts/talk.html`
- Modify: `src/main/resources/public/site.css`
- Test: `src/test/java/com/wjglerum/SiteRenderTest.java`

**Interfaces:**
- Consumes: `cdi:setlist.tour(site.collections.talks, slug)` returning `TourGroup` and `cdi:setlist.siblings(site.collections.talks, slug, currentUrl)` returning `List<Talk>` from Task 2; the `id="tour-<slug>"` anchors on the homepage from Task 4.
- Produces: nothing consumed by later tasks.

- [ ] **Step 1: Write the failing tour-context assertions**

Add to `src/test/java/com/wjglerum/SiteRenderTest.java`:

```java
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
```

- [ ] **Step 2: Run to verify they fail**

Run: `./mvnw test -Dtest=SiteRenderTest`
Expected: FAIL (talk pages have no tour context yet).

- [ ] **Step 3: Add the tour context block to `talk.html`**

In `src/main/resources/templates/layouts/talk.html`, replace the `{#insert /}` line (line 45) with the insert followed by the tour context aside:

```html
        {#insert /}
        {#let slug=page.data('tour')}
        {#if slug}
        {#let tour=cdi:setlist.tour(site.collections.talks, slug)}
        <aside class="tour-context">
            <p class="part-of">Part of the <a href="/#tour-{slug}"><em>{tour.title}</em></a> tour.</p>
            {#let sibs=cdi:setlist.siblings(site.collections.talks, slug, page.url.path)}
            {#if sibs.size > 0}
            <h2 class="also">Also played at</h2>
            <ul class="sibling-list">
                {#for s in sibs}
                <li>
                    <a href="{s.url}">
                        <span class="d">{s.dateShort}</span>
                        <span class="t">{s.title}</span>
                        <span class="c">{s.conference}</span>
                    </a>
                </li>
                {/for}
            </ul>
            {/if}
            {/let}
        </aside>
        {/let}
        {/if}
        {/let}
```

- [ ] **Step 4: Add tour-context styles to `site.css`**

Append to the end of `src/main/resources/public/site.css`:

```css
/* Talk page: tour context */
.tour-context { margin-top: 2.5rem; padding-top: 1.5rem; border-top: 1px solid var(--rule); }
.part-of { color: var(--dim); }
.part-of a { color: var(--accent); text-decoration: none; }
.part-of a:hover { text-decoration: underline; }
.also { font-size: 0.8rem; letter-spacing: 0.08em; text-transform: uppercase; color: var(--faint); margin: 1.25rem 0 0.5rem; }
.sibling-list { list-style: none; }
.sibling-list li { border-bottom: 1px solid var(--rule); }
.sibling-list li a { display: grid; grid-template-columns: 6rem 1fr auto; gap: 1rem; align-items: baseline; padding: 0.5rem 0; color: var(--text); text-decoration: none; }
.sibling-list li a:hover { color: var(--accent); }
.sibling-list .d, .sibling-list .c { color: var(--dim); font-size: 0.85rem; }
```

- [ ] **Step 5: Run the tests to verify they pass**

Run: `./mvnw test -Dtest=SiteRenderTest`
Expected: PASS.

- [ ] **Step 6: Run the full suite**

Run: `./mvnw test`
Expected: PASS (`SetlistTest` and `SiteRenderTest`).

- [ ] **Step 7: Commit**

```bash
git add src/main/resources/templates/layouts/talk.html src/main/resources/public/site.css src/test/java/com/wjglerum/SiteRenderTest.java
git commit -m "Add tour context and sibling links to talk detail pages"
```

---

## Task 6: Scheduled CI rebuild

**Files:**
- Modify: `.github/workflows/deploy.yml`

**Interfaces:**
- Consumes: nothing. Keeps build-time tour status honest by rebuilding daily.
- Produces: nothing consumed by other tasks.

- [ ] **Step 1: Add a daily schedule trigger**

In `.github/workflows/deploy.yml`, replace the `on:` block:

```yaml
on:
  push:
    branches: [main]
  pull_request:
    branches: [main]
  workflow_dispatch:
```

with:

```yaml
on:
  push:
    branches: [main]
  pull_request:
    branches: [main]
  workflow_dispatch:
  schedule:
    # Daily rebuild so computed tour status (now touring vs wrapped) stays current.
    - cron: '17 6 * * *'
```

- [ ] **Step 2: Verify the workflow still parses**

Run: `python3 -c "import yaml,sys; yaml.safe_load(open('.github/workflows/deploy.yml')); print('ok')"`
Expected: `ok`

- [ ] **Step 3: Confirm the scheduled deploy gate still excludes pull requests**

Read `.github/workflows/deploy.yml` and confirm the `deploy` job keeps `if: github.event_name != 'pull_request'`. A `schedule` event is not a pull request, so the daily run deploys. No change needed; just verify.

- [ ] **Step 4: Commit**

```bash
git add .github/workflows/deploy.yml
git commit -m "Rebuild the site daily so tour status stays current"
```

---

## Self-Review

**1. Spec coverage:**

- Group talks into tours as primary unit: Tasks 1, 4 (tour blocks).
- Compute status from dates, no manual upkeep: Task 2 (`statusOf`), Task 6 (daily rebuild).
- Two-state status plus orthogonal "New" badge: Task 2 (`statusOf`, `recentlyActive`), Task 4 (badge gated on `status != 'now-touring'`).
- Injectable date for deterministic tests: Task 2 (`setlist.today` config, `today()`), Task 2 step 6 (`%test.setlist.today`).
- Fail fast on unknown slug: Task 2 (`group` throws), `SetlistTest.unknownSlugFailsFast`.
- Tour title vs talk title rule: Task 4 (tour block uses `tour.title`), Task 5 (siblings use `s.title`).
- Keep geographic map, themed: Task 4 map script with `tiles()` and `themechange` listener.
- Light/dark themes, default system, three-state toggle, no FOUC, reduced motion: Task 3 (partials, tokens), pulse guarded by `prefers-reduced-motion` in Task 4.
- Single source of theme state + `themechange` event: Task 3 toggle dispatches it, Task 4 map consumes it.
- Homepage hero, stats, map, Now Playing, Past Tours, Singles section: Task 4.
- Computed stats with precise city/country definition: Task 2 (`stats`), `SetlistTest.statsCount...`, Task 4 renders them.
- Talk detail tour context, `/#tour-<slug>` link, siblings, single-date has no sibling list: Task 5 and its two tests.
- Retain `cities.yml` / `Cities.java`: untouched, still used by the map JSON in Task 4.
- Restyle about page and shared chrome: Task 3 (main.html chrome and tokens apply to about.html via the shared layout and `site.css`).
- Tests assert sections, active/wrapped tours, stats, talk context, missing slug, theme presence: Tasks 1 through 5.

**2. Placeholder scan:** No TBD, no "add error handling", no "similar to Task N", no "write tests for the above". Every code step shows complete code.

**3. Type consistency:** `Talk`, `TourGroup`, `Stats`, and `Tours.TourMeta` field and accessor names used in `SetlistTest`, `Setlist`, `tour-block.html`, `index.html`, and `talk.html` all match the definitions in Task 2 (`status` string values `"now-touring"`/`"wrapped"`; `dateCount`, `recentlyActive`, `dateShort`, `hasVideo`, `upcoming`, `signatureTalks`, `sinceYear`). Bean method names `nowPlaying`, `pastTours`, `singles`, `stats`, `tour`, `siblings` are identical in the bean and every template call. The map reads `--accent` and `--ground`, both defined in Task 3 tokens.

**Verification risks to watch during execution** (note for the implementer, not gaps in the plan):
- If Qute build validation rejects the `cdi:setlist` namespace, switch the template calls to `inject:setlist` (both are build-validated per the Qute reference); the bean is `@Named` so either resolves.
- If `@QuarkusTest` does not serve Roq pages over HTTP in this version, fall back to asserting against the generated output under `target/roq/`; the render assertions stay the same, only the fetch mechanism changes.
