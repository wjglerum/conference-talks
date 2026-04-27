---
title: Local AI Agents
date: 2025-04-11
layout: talk
conference: Lunatech 2025
location: Rotterdam, The Netherlands
type: techtalk
slides:
video:
---

<div class="talk-header">
    <div class="conference-badge">{page.data("conference")}
        <span class="tag techtalk">Tech Talk</span>
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
    <p>An internal tech talk exploring the landscape of locally-run AI agents — running large language models on your own hardware using tools like Ollama, building agentic workflows without sending data to the cloud, and the practical trade-offs between local and cloud-hosted AI for enterprise development.</p>
</div>
