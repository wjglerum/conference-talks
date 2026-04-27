---
title: "Virtual Threads vs Reactive Programming in Quarkus"
date: 2025-07-09
layout: talk
conference: Riviera DEV 2025
location: Sophia Antipolis, France
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
    <p>Reactive programming has been the way to go for extra performance and concurrent programming, now we also have virtual threads. What should we choose? Can we leverage both? What's the best approach to write non-blocking code? In this talk we'll explain the background behind both paradigms combined with some live coding. We will cover the advantages and disadvantages of both of them using real-world examples in Quarkus. We will aim to make this talk as simple as possible and show some real code combined with live coding. After this talk you will understand both paradigms and be able to choose the right paradigm for your next project.</p>
</div>
