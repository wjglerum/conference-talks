---
title: "Virtual Threads vs Reactive Programming in Quarkus"
date: 2025-09-16
layout: talk
conference: Quarkus Insights 2025
location: Online
type: podcast
slides:
video: https://www.youtube.com/watch?v=YX7NJxOUMWU
---

<div class="talk-header">
    <div class="conference-badge">{page.data("conference")}
        <span class="tag podcast">Podcast</span>
    </div>
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
    <p>Reactive programming has been the way to go for extra performance and concurrent programming, now we also have virtual threads. What should we choose? Can we leverage both? What's the best approach to write non-blocking code? In this episode we'll explain the background behind both paradigms, cover the advantages and disadvantages of both using real-world examples in Quarkus, and help you understand which paradigm to choose for your next project.</p>
</div>
