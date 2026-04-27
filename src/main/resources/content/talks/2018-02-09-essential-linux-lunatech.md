---
title: Essential Linux
date: 2018-02-09
layout: talk
conference: Lunatech 2018
location: Rotterdam, The Netherlands
type: workshop
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
    </div>
    <div class="links">
        {#if page.data("video")}<a class="btn btn-primary" href="{page.data("video")}" target="_blank">▶ Watch recording</a>{/if}
        {#if page.data("slides")}<a class="btn btn-secondary" href="{page.data("slides")}" target="_blank">📊 Slides</a>{/if}
    </div>
</div>

<div class="abstract">
    <h2>Abstract</h2>
    <p>An internal workshop covering essential Linux skills for developers — navigating the command line, managing processes, working with files and permissions, and understanding the Linux ecosystem well enough to be productive on any Linux-based system.</p>
</div>
