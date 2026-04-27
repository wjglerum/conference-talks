---
title: "Hacking the room with Raspberry PI's"
date: 2020-01-17
layout: talk
conference: LunaConf 2020
location: Rotterdam, The Netherlands
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
    <p>A fun, hands-on talk demonstrating how Raspberry Pi's can be used to automate and interact with the physical environment — from reading sensor data to controlling lights and displays. Shows how inexpensive hardware paired with simple code can unlock surprisingly powerful room automation.</p>
</div>
