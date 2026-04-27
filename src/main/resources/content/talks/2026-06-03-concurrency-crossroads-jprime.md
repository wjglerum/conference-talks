---
title: "Concurrency Crossroads: Choosing between Reactive Programming and Virtual Threads in Quarkus"
date: 2026-06-03
layout: talk
conference: jPrime 2026
location: Sofia, Bulgaria
type: deepdive
slides:
video:
---

<div class="talk-header">
    <div class="conference-badge">{page.data("conference")}
        <span class="tag deepdive">Deep Dive</span>
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
    <p>Reactive programming has been the way to go for extra performance and concurrent programming, now we also have Virtual Threads (Project Loom). Which one should we choose? Can we leverage both? What's the best approach to write high-performance non-blocking code in our application? In this talk we'll explain the background behind both paradigms and go over the details together. We will cover the advantages and disadvantages of both paradigms using real-world examples in Quarkus. Furthermore, we will tell you the gotchas so you don't have to discover them yourself and we will also look ahead at what's coming next in future Java versions. We will aim to make this talk as simple as possible and show real code combined with live coding.</p>
</div>
