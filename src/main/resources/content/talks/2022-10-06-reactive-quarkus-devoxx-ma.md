---
title: Reactive Quarkus
date: 2022-10-06
layout: talk
conference: Devoxx Morocco 2022
location: Agadir, Morocco
type: talk
slides:
video:
---

<div class="talk-header">
    <div class="conference-badge">{page.data("conference")}</div>
    <h1>{page.title}</h1>
    <div class="meta">
        <span>📅 {page.date.format('MMMM d, yyyy')}</span>
        <span>📍 {page.data("location")}</span>
    </div>
    <div class="links">
        {#if page.data("video")}<a class="btn btn-primary" href="{page.data("video")}" target="_blank">▶ Watch recording</a>{/if}
        {#if page.data("slides")}<a class="btn btn-secondary" href="{page.data("slides")}" target="_blank">📊 Slides</a>{/if}
    </div>
</div>

<div class="abstract">
    <h2>Abstract</h2>
    <p>An introduction to reactive programming with Quarkus, exploring how to build reactive applications with Java using Quarkus and the Mutiny reactive library. Covers the reactive model, non-blocking I/O, and how to get started with reactive Quarkus applications.</p>
</div>
