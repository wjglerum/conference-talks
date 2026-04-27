---
title: "Building secure AI agents with Quarkus LangChain4j"
date: 2025-10-06
layout: talk
conference: Devoxx Belgium 2025
location: Antwerp, Belgium
type: workshop
coSpeaker: Radek Kargul
slides:
video:
---

<div class="talk-header">
    <div class="conference-badge">{page.data("conference")}
        <span class="tag workshop">Workshop</span>
    </div>
    <h1>{page.title}</h1>
    <div class="meta">
        <span>📅 {page.date.format('MMMM d, yyyy')}</span>
        <span>📍 {page.data("location")}</span>
        {#if page.data("coSpeaker")}<span>👥 with {page.data("coSpeaker")}</span>{/if}
    </div>
    <div class="links">
        {#if page.data("video")}<a class="btn btn-primary" href="{page.data("video")}" target="_blank">▶ Watch recording</a>{/if}
        {#if page.data("slides")}<a class="btn btn-secondary" href="{page.data("slides")}" target="_blank">📊 Slides</a>{/if}
    </div>
</div>

<div class="abstract">
    <h2>Abstract</h2>
    <p>A hands-on workshop where participants build and secure AI agents using Quarkus and LangChain4j. You'll set up a Quarkus project with the LangChain4j extension, implement AI-powered features, and add robust security measures to prevent prompt injection, data leakage, and other AI-specific vulnerabilities.</p>
</div>
