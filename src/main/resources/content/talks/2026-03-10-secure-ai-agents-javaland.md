---
title: "Secure AI agents with Quarkus LangChain4j"
date: 2026-03-10
layout: talk
conference: JavaLand 2026
location: Rust, Germany
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
    <p>AI agents are increasingly used within enterprise applications and securing them has become a critical concern. You for example don't want your agent to drop your database or accidentally execute privileged actions on your API. In this session, we will use live coding to show you how to build yet simple but secure AI agents using Quarkus LangChain4j.</p>
    <p>This talk is suitable for developers new to AI and more experienced developers who want to learn how to secure AI agents in their projects. Some knowledge of Java, Quarkus and LangChain4j is helpful but not required.</p>
</div>
