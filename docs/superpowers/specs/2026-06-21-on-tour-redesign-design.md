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
- Compute tour status (now touring vs wrapped) from dates so the page needs no manual upkeep,
  kept honest by a scheduled CI rebuild.
- Keep a geographic map of where talks were given.
- Support light and dark color themes, defaulting to system preference, with a three-state
  toggle (light, dark, system) that persists an explicit override.
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
`prefers-color-scheme`. `prefers-reduced-motion` continues to be respected.

#### Theme state machine

The toggle is three-state: light, dark, and system. This matters because the moment a binary
toggle writes `localStorage` it stops following the operating system, and a binary control
gives the visitor no way back to "follow my OS." The three states are:

- **system** (the default, and the value when no override is stored): no `localStorage` key is
  set, and the page follows `prefers-color-scheme`. A `matchMedia` change listener re-applies
  the theme live if the OS switches between light and dark.
- **light** / **dark**: an explicit override, stored in `localStorage` under `wjg-theme`.

Clicking the toggle cycles system to light to dark and back to system. Choosing system removes
the stored key so the page resumes following the OS.

#### Single source of theme state and FOUC avoidance

The resolved theme ("light" or "dark") lives in exactly one place: a `data-theme` attribute on
`<html>`. To avoid a flash of the wrong theme, a tiny inline script in `<head>` runs before
first paint: it reads `wjg-theme` from `localStorage` (or falls back to `prefers-color-scheme`)
and sets `data-theme` accordingly.

The toggle button, the `matchMedia` listener, and the map all read and react to that one
attribute rather than each re-deriving the theme. When the resolved theme changes, the code
that changes it sets `data-theme` and dispatches a `themechange` CustomEvent on `document`.
Any component that needs to react (notably the map) listens for that event. This keeps the
three scripts that touch theme state (FOUC script, toggle, OS listener) from drifting out of
sync.

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

#### Tour title vs talk title

The tour title in `tours.yml` is a deliberate umbrella, not a copy of any one talk's title.
Individual talks keep their own front-matter `title` and may differ from the tour title by
design: "Concurrency Crossroads" covers talks titled "Virtual Threads vs Reactive Programming
in Quarkus", and "Secure AI Agents" covers a "Local AI Agents" session. The rule is:

- The tour display title (and subtitle) is used for the tour block heading on the homepage and
  for the "Part of the *<tour>* tour" line on talk pages.
- Each individual talk keeps its own title in the date rows and on its own detail page.
- The "also played at" sibling list shows each sibling's own talk title and date, not the tour
  title.

#### Missing or mistyped slug fails the build

Unlike a location missing from `cities.yml` (which silently drops a marker off the map), the
tour is the primary structural unit, so a `tour:` slug with no entry in `tours.yml` is a build
error. `TourExtensions` validates every referenced slug against `tours.yml` and fails fast with
the offending slug and file name. This prevents a typo from silently producing an untitled
tour.

### Tour grouping and status

A Qute template extension (for example `TourExtensions`) provides the grouping and ordering
logic the template calls. Given `site.collections.talks` it returns tour groups, each with:

- the tour slug, title, and subtitle from `tours.yml`
- its talks, sorted newest date first, upcoming dates flagged
- a count of dates
- a computed status and a "recently active" flag

#### Status is two states, not three

The earlier draft had a third "New" status that only ever applied to a single past date and
then promoted a finished one-off into the active section. That conflated "happened recently"
with "currently touring" and behaved differently for single vs multi-date tours. It is
replaced by a clean split plus an orthogonal badge:

- **Now touring**: the tour has any talk dated today or later.
- **Wrapped**: every date is in the past.

Independently of status, a tour carries a boolean `recentlyActive`: its most recent date falls
within the last twelve months. This rule is identical for single and multi-date tours. It
drives a small "New" badge on the tour block and also governs placement: a recently active tour
is surfaced under "Now Playing" even after it has wrapped, because a tour toured within the last
year is still current in spirit. A recently-finished tour therefore appears under Now Playing
with a "New" badge (not a "Now touring" pill, which is reserved for tours with an upcoming
date). Only tours whose most recent date is older than twelve months drop to "Past Tours".

#### Build-time evaluation needs a scheduled rebuild

Status is computed at build time. Because the site is static and normally only rebuilds on a
content push, a tour would otherwise keep showing "Now touring" for months after its last date
passed. To keep status honest without manual upkeep, CI gains a scheduled rebuild (a daily
GitHub Actions `schedule` cron that runs the same Roq build and deploy). Status then lags
reality by at most a day. The "current date" used for evaluation is supplied by an injectable
time source (see Testing) so it is deterministic in tests rather than reading the wall clock
directly.

#### Ordering on the homepage

- "Now Playing" holds both genuinely active tours and recently active (wrapped within twelve
  months) tours. Active tours come first, ordered by soonest upcoming date; the recently active
  tours follow, ordered by most recent date, newest first.
- "Past Tours" holds tours whose most recent date is older than twelve months, by most recent
  date, newest first.
- One-offs (talks with no `tour`) are collected into a "Singles" group (see Homepage).

Each date row shows the date, conference, city, the talk type badge (Conference, Meetup, Deep
Dive, Workshop, Podcast, Tech Talk, Booth Demo), and a recording flag when a video exists. The
type is modelled as a `TalkType` enum that pairs the front matter `type` slug with its display
label, so the slug-to-label mapping lives in one place and a typo fails the build.

### Tour assignment for existing content

All existing talk files get a `tour` slug as part of this work:

- `concurrency-crossroads`: the Virtual Threads vs Reactive and Concurrency Crossroads sessions
  (Riviera DEV 2025, Java Forum Nord 2025, Quarkus Insights 2025, JavaCro 2025, J-Fall 2025,
  Devoxx Morocco 2025, OCX 2026, Devoxx France 2026, jPrime 2026, Øredev 2026) - 10 dates
- `secure-ai-agents`: Local AI Agents and the Building Secure AI Agents workshops and talk
  (Lunatech 2025, JavaZone 2025, Devoxx Belgium 2025, JavaLand 2026, Devoxx Poland 2026,
  JavaZone 2026) - 6 dates
- `practical-mcp-security`: Practical MCP Security (jPrime 2026) - 1 date, wrapped (its date is
  in the past), carries the "New" badge while within twelve months of that date
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
   upcoming highlighted). Each tour block carries an `id` of `tour-<slug>` so talk pages can
   link to it.
5. "Past Tours": wrapped tours, same structure, more compact. A "New" badge marks tours active
   within the last twelve months.
6. "Singles": the one-off sessions, rendered as a compact but visible section (not a buried
   footer line), each a single row with date, title, conference, city, and recording flag.
   These are real talks (Essential Linux, Hacking the Room with Raspberry Pis, Quarkus on
   Java 21) and should remain scannable rather than hidden.

### Talk detail (`talk.html`)

Restyled to the themed palette. Adds tour context: a "Part of the *Concurrency Crossroads*
tour" line and an "also played at" list linking the sibling talks in the same tour, built from
the collection by matching `tour` slug.

Because this iteration has no per-tour page (see Non-goals), the "Part of the *<tour>* tour"
text links back to the homepage tour block anchor (`/#tour-<slug>`) rather than to a dead or
missing destination. A single-date tour shows the "Part of" line with no "also played at"
list. A talk with no `tour` shows neither.

### About (`about.html`) and shared chrome (`layouts/main.html`)

Restyled to the themed palette. The header gains the light/dark toggle button. Header, footer,
and navigation carry across both themes via the shared tokens.

### Map

A geographic Leaflet map as on the current site, themed to match:

- Dark theme uses CARTO `dark_nolabels` plus `dark_only_labels` tiles; light theme uses
  `light_nolabels` plus `light_only_labels`.
- Markers use the accent color with a stroke in the ground color.
- The map re-themes when the resolved theme changes. It does not re-derive the theme itself:
  on init it reads `data-theme` from `<html>`, and it listens for the `themechange` CustomEvent
  (see Theming) to swap the tile layers and re-color markers. This keeps the map a consumer of
  the single source of theme state rather than a fourth place that computes it.
- `cities.yml` and `Cities.java` continue to supply coordinates. A talk whose location is not
  in `cities.yml` drops off the map, so new locations must be added there (unchanged behavior).
- The Qute raw block and escaped-brace JSON technique in `index.html` is preserved; edits to
  that region stay careful per the existing CLAUDE.md note. The added theme logic should be
  kept small and confined to the existing script block: read `data-theme`, build the two tile
  layers, and swap them on `themechange`. No new templated braces are introduced inside the raw
  block.

## Computed statistics

All hero stats come from the talks collection at build time, no hand-maintained numbers. The
counts below are what the current content produces; they are examples, not values to maintain
by hand. If a count looks wrong after implementation, the data or the extension is wrong, not
this prose.

- Shows: collection size (currently 36)
- Signature talks: distinct `tour` slugs (currently 6)
- Cities: distinct `location` strings excluding "Online" (currently 19). Note this is computed
  from the collection's `location` values, which is a different source from `cities.yml`; a
  location present in the collection but absent from `cities.yml` still counts here while
  dropping off the map. Keep the two in step.
- Countries: distinct country component (the part after the comma) of each `location`,
  excluding "Online" (currently 12)
- Since: earliest talk year (currently 2016)

## Testing

Status is time-dependent, so tests must not read the wall clock. The "current date" used by
`TourExtensions` comes from an injectable time source (a CDI `Clock` bean, or a configurable
date that defaults to the system clock). Tests inject a fixed date so assertions about active
vs wrapped do not rot as real time passes. Without this, "Concurrency Crossroads is active"
would start failing the day its last date goes by.

Extend the existing `@QuarkusTest` plus REST Assured suite to assert, with a fixed injected
date:

- the homepage renders the "Now Playing", "Past Tours", and "Singles" sections
- a tour with a date at or after the injected date appears under Now Playing with its date count
- a tour with all dates before the injected date appears under Past Tours
- the "New" badge appears on a tour whose most recent date is within twelve months of the
  injected date, and is absent otherwise
- a slug referenced in front matter but missing from `tours.yml` fails the build (negative test)
- the three-state theme toggle and the no-FOUC inline script are present in the rendered head
- computed stats render (for example the shows count)
- a talk detail page renders its tour context, the `/#tour-<slug>` link, and sibling links, and
  a single-date tour renders the "Part of" line with no sibling list

## Future work (out of scope)

- A dedicated Atlas page that makes the map the hero, reusing `cities.yml`.
- Per-tour pages with full date lists and abstracts.
