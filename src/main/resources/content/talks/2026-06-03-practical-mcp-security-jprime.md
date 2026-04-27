---
title: "Practical MCP Security in Action"
date: 2026-06-03
layout: talk
conference: jPrime 2026
location: Sofia, Bulgaria
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
    <p>What does it take to create a secure MCP server? In this talk we'll explain how MCP Authorization works and cover important security considerations. We'll demonstrate OAuth2 implementation and dynamic client registration using popular MCP clients and Quarkus tools. No prior Quarkus experience is required.</p>
</div>
