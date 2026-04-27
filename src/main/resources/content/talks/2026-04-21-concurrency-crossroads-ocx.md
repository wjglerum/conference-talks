---
title: "Concurrency Crossroads: Choosing between Reactive Programming & Virtual Threads"
date: 2026-04-21
layout: talk
conference: OCX 2026
location: Brussels, Belgium
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
    <p>This talk compares Reactive Programming and Java Virtual Threads (Project Loom) for high-performance, non-blocking applications. Using real Quarkus examples and live coding, it explores advantages, drawbacks, and future trends, helping developers understand both paradigms and choose the optimal approach for their next Java project.</p>
</div>
