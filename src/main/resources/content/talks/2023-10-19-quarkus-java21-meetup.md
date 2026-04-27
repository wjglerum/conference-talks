---
title: "Quarkus & Java 21"
date: 2023-10-19
layout: talk
conference: Quarkus Meetup 2023
location: Rotterdam, Netherlands
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
    <p>Exploring what Java 21 brings to Quarkus applications — from virtual threads (Project Loom) to pattern matching and record patterns. This talk demonstrates how Java 21 features integrate with Quarkus and what developers can immediately benefit from when upgrading.</p>
</div>
