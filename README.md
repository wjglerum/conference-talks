# conference-talks

The source for [wjglerum.nl](https://wjglerum.nl/), my running list of conference talks, podcasts and workshops since 2018. Built as a static site with [Quarkus Roq](https://iamroq.dev/), deployed to GitHub Pages.

## Run locally

```sh
./mvnw quarkus:dev
```

Open http://localhost:8080/. Hot reload picks up changes to talks, templates and styles.

## Add a talk

Drop a new markdown file under `src/main/resources/content/talks/` named `YYYY-MM-DD-slug.md` and fill in the front matter:

```yaml
---
title: Your talk title
date: 2026-09-01
layout: talk
conference: Some Conference 2026
conferenceUrl: https://example.com/
location: Berlin, Germany
type: talk           # talk | deepdive | workshop | podcast | techtalk
slides: https://...  # Google Slides, reveal.js, etc. (optional)
video: https://...   # YouTube link (optional)
demo: https://...    # GitHub repo with the code (optional)
link: /talks/2026-09-01-your-talk-slug/
---

<div class="abstract">
    <h2>Abstract</h2>
    <p>One short paragraph.</p>
</div>
```

If the location is new, add its coordinates to `src/main/resources/data/cities.yml`. The map only shows pins for cities listed there; otherwise the marker silently drops out.

## Layout

```
src/main/
  java/com/wjglerum/Cities.java       # @DataMapping record exposing cities.yml as a CDI bean
  resources/
    application.properties             # site.url, talks collection config
    content/index.html                 # landing page (hero + Leaflet map + grid)
    content/talks/*.md                 # one file per talk
    data/cities.yml                    # location -> { lat, lng }
    public/site.css                    # shared stylesheet
    public/favicon.svg                 # cream paper, terracotta dot
    templates/layouts/main.html        # listing layout
    templates/layouts/talk.html        # talk detail layout
.github/workflows/deploy.yml           # build + push to gh-pages
```

## Deploy

Pushing to `main` triggers `.github/workflows/deploy.yml`, which runs `quarkiverse/quarkus-roq@v1` to generate `target/roq/` and uploads it to GitHub Pages. The custom domain `wjglerum.nl` is set in repo settings, served from the `gh-pages` environment.
