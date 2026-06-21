# On Tour: redesign of wjglerum.nl

Date: 2026-06-21
Status: approved concept, pending implementation plan

## Summary

Reframe the site from a flat grid of talks into a touring metaphor. Willem Jan does not
give dozens of unique talks: he crafts a handful of signature talks and tours them across
Europe. "Concurrency Crossroads" alone runs across ten dates; "SSO made easy with Quarkus
OIDC" ran across eight over four years. The redesign makes that pattern the structure of
the site.

The homepage leads with a "Now Playing" section (active tours), followed by "Past Tours"
(the back catalogue), and a one-line footer of one-off sessions. A themed geographic map of
the tour route returns to the homepage. The whole site supports light and dark themes,
defaulting to the visitor's system preference with a manual override.

## Goals

- Group talks into tours and present them as the primary unit, not individual sessions.
- Compute tour status (now touring vs wrapped) from dates so the page needs no manual upkeep.
- Keep a geographic map of where talks were given.
- Support light and dark color themes, default to system preference, with a persisted toggle.
- Keep the content-as-data model: a talk is still one markdown file with front matter.

## Non-goals

- No runtime backend. The site stays a static Roq build deployed to GitHub Pages.
- No dedicated per-tour pages in this iteration. A tour is a homepage section plus links
  between sibling talk pages.
- No standalone Atlas/travel page yet. The map stays on the homepage. `cities.yml` and
  `Cities.java` are retained so an Atlas page can be added later without redoing data work.

## Visual direction

Bold, confident, "an engineer's tour ledger." Heavy uppercase grotesk for tour titles and
the hero, a clean sans for body, large gold numerals for date counts. One restrained motion
moment: a pulsing "now touring" indicator. The design avoids the templated cream-and-terracotta
and acid-green-on-black looks.

### Theming

Two themes driven entirely by CSS custom properties on `:root`. Defaults follow
`prefers-color-scheme`. A toggle button in the header overrides the system choice and persists
it in `localStorage` under a key such as `wjg-theme`. To avoid a flash of the wrong theme, a
tiny inline script in `<head>` reads the stored preference and sets a `data-theme` attribute on
`<html>` before first paint. `prefers-reduced-motion` continues to be respected.

Dark palette (primary):

- ground `#14110D`, panel `#1C1813`
- text `#F1ECE1`, dim `#9C9486`, faint `#6E665A`
- accent `#F0572B`, gold `#CCA24C`, rule `#2C261D`

Light palette (counterpart):

- ground `#F7F4EC`, panel `#FFFFFF`
- text `#1A1510`, dim `#6E665A`, faint `#9C9486`
- accent `#D8441E`, gold `#A9802F`, rule `#E2DACB`

The accent and gold shift slightly darker in light mode for contrast. Every color on the page
derives from these tokens, so the two themes are one set of variables swapped by `data-theme`.

## Data model

### Tour membership

Each talk gains an optional `tour` field in its front matter, holding a tour slug:

```yaml
---
title: "Concurrency Crossroads: Choosing between Reactive Programming and Virtual Threads"
conference: Øredev 2026
tour: concurrency-crossroads
---
```

A talk with no `tour` field is a one-off "single."

### Tour metadata

A new `data/tours.yml` maps each slug to its display title and a one-line blurb, mirroring the
existing `cities.yml` structure:

```yaml
list:
  concurrency-crossroads:
    title: "Concurrency Crossroads"
    subtitle: "Reactive Programming vs. Virtual Threads in Quarkus"
  secure-ai-agents:
    title: "Secure AI Agents"
    subtitle: "Building secure AI agents with Quarkus LangChain4j"
```

A `Tours.java` record exposes this as a CDI bean via `@DataMapping("tours")`, exactly as
`Cities.java` does for cities.

### Tour grouping and status

A Qute template extension (for example `TourExtensions`) provides the grouping and ordering
logic the template calls. Given `site.collections.talks` it returns tour groups, each with:

- the tour slug, title, and subtitle from `tours.yml`
- its talks, sorted newest date first, upcoming dates flagged
- a count of dates
- a computed status

Status is derived at build time from `LocalDate.now()`:

- "Now touring" if the tour has any talk dated today or later
- "New" if the tour has a single date and that date falls within the last twelve months
- "Wrapped" otherwise

Tour ordering on the homepage: "Now touring" and "New" tours render together under "Now
Playing" (ordered by their next or most recent date, soonest upcoming first), then "Wrapped"
tours render under "Past Tours" by most recent date. One-offs are collected into a separate
"singles" group rendered as the footer line.

### Tour assignment for existing content

All existing talk files get a `tour` slug as part of this work:

- `concurrency-crossroads`: the Virtual Threads vs Reactive and Concurrency Crossroads sessions
  (Riviera DEV 2025, Java Forum Nord 2025, Quarkus Insights 2025, JavaCro 2025, J-Fall 2025,
  Devoxx Morocco 2025, OCX 2026, Devoxx France 2026, jPrime 2026, Øredev 2026) - 10 dates
- `secure-ai-agents`: Local AI Agents and the Building Secure AI Agents workshops and talk
  (Lunatech 2025, JavaZone 2025, Devoxx Belgium 2025, JavaLand 2026, Devoxx Poland 2026,
  JavaZone 2026) - 6 dates
- `practical-mcp-security`: Practical MCP Security (jPrime 2026) - 1 date, "New"
- `sso-quarkus-oidc`: SSO made easy with Quarkus OIDC (Quarkus Meetup 2022, JavaZone 2022,
  Riviera DEV 2024, LunaConf 2024, OCX 2024, jPrime 2025, JavaCro 2025, Devoxx Morocco 2025)
  - 8 dates
- `reactive-quarkus`: Reactive Quarkus (LunaConf 2021, Devoxx Ukraine 2021, Devoxx Poland 2022,
  Devoxx Morocco 2022) - 4 dates
- `scala-iot`: the Scala era (Scala Days 2016, Scala Days 2017, ScalaUA 2018, Scala Days 2018)
  - 4 dates
- singles (no `tour`): Essential Linux (2018), Hacking the Room with Raspberry Pis (2020),
  Quarkus on Java 21 (2023)

This totals 36 shows across 6 signature tours plus 3 one-offs.

## Pages and templates

### Homepage (`content/index.html`)

Rewritten to the setlist layout:

1. Hero: bold uppercase headline and intro copy.
2. Stats ticker, all values computed from the collection (see below).
3. Themed geographic map of the route (see Map).
4. "Now Playing": active tours, each a block with status pill, title, subtitle, a gold date
   count, and the tour's dates as rows (date, conference, city, type and recording flags,
   upcoming highlighted).
5. "Past Tours": wrapped tours, same structure, more compact.
6. Footer line listing the one-off sessions.

### Talk detail (`talk.html`)

Restyled to the themed palette. Adds tour context: a "Part of the *Concurrency Crossroads*
tour" line and an "also played at" list linking the sibling talks in the same tour, built from
the collection by matching `tour` slug.

### About (`about.html`) and shared chrome (`layouts/main.html`)

Restyled to the themed palette. The header gains the light/dark toggle button. Header, footer,
and navigation carry across both themes via the shared tokens.

### Map

A geographic Leaflet map as on the current site, themed to match:

- Dark theme uses CARTO `dark_nolabels` plus `dark_only_labels` tiles; light theme uses
  `light_nolabels` plus `light_only_labels`.
- Markers use the accent color with a stroke in the ground color.
- The map re-themes when the visitor toggles light/dark.
- `cities.yml` and `Cities.java` continue to supply coordinates. A talk whose location is not
  in `cities.yml` drops off the map, so new locations must be added there (unchanged behavior).
- The Qute raw block and escaped-brace JSON technique in `index.html` is preserved; edits to
  that region stay careful per the existing CLAUDE.md note.

## Computed statistics

All hero stats come from the talks collection at build time, no hand-maintained numbers:

- Shows: collection size (36)
- Signature talks: distinct `tour` slugs (6)
- Cities: distinct physical locations, excluding "Online" (19)
- Countries: distinct country component of each location, excluding "Online" (12)
- Since: earliest talk year (2016)

## Testing

Extend the existing `@QuarkusTest` plus REST Assured suite to assert:

- the homepage renders the "Now Playing" and "Past Tours" sections
- a known active tour (Concurrency Crossroads) appears with its date count
- a known wrapped tour (SSO made easy) appears under Past Tours
- computed stats render (for example the shows count)
- a talk detail page renders its tour context and sibling links

## Future work (out of scope)

- A dedicated Atlas page that makes the map the hero, reusing `cities.yml`.
- Per-tour pages with full date lists and abstracts.
