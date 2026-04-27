---
title: "Reactive Quarkus: Building Reactive Applications with Java"
date: 2022-06-22
layout: talk
conference: Devoxx Poland 2022
location: Kraków, Poland
type: talk
slides:
video: https://www.youtube.com/watch?v=NVO5YntUBlU
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
    <p>A deep dive into building reactive applications with Quarkus. This talk covers how to leverage the Vert.x reactive engine underneath Quarkus, use Mutiny for composing asynchronous operations, and build high-performance reactive REST APIs and messaging pipelines.</p>
</div>
