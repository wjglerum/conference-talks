---
title: SSO made easy with Quarkus OIDC
date: 2022-03-31
layout: talk
conference: Quarkus Meetup 2022
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
    <p>If you build an application you often end up needing authentication and authorization. Instead of building this yourself we can deliver a SSO experience for our users with the Quarkus OIDC extension. During this session we will show you how easy it is to set up SSO in a real Quarkus app, from local dev mode to production.</p>
</div>
