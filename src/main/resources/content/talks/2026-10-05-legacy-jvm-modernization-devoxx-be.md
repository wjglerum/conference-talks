---
title: "Legacy JVM modernization with agentic AI: Quarkus vs Spring Boot"
date: 2026-10-05
layout: talk
conference: Devoxx Belgium 2026
conferenceUrl: https://devoxx.be/
location: Antwerp, Belgium
type: deepdive
coSpeaker: Radek Kargul
link: /talks/2026-10-05-legacy-jvm-modernization-devoxx-be/

---

Most modernization talks show a diagram of the old system, a diagram of the new one, and an arrow between them labeled "and then AI migrated it". We want to spend three hours inside that arrow. We bring a real legacy Scala application built on Play and Akka on stage, anonymized from production code, and migrate it live to Java 25 twice. Claude does the typing, while Willem Jan steers it toward Quarkus and Radek explores Spring Boot. We take turns instead of racing side by side, so the room can follow one decision at a time, and stop at each checkpoint to score both the framework and the agent's output on a shared scorecard.

The session has four parts: diagnosing the legacy app and writing characterization tests before touching anything, translating the Scala domain model (case classes, Option, Either, for-comprehensions) to Java (records, sealed interfaces, pattern matching, Optional) with the agent proposing and us reviewing, alternating between the two target frameworks through a series of migration stages, and a final verdict comparing performance, developer experience, and testability &mdash; plus where the agent saved time and where it needed a human override.
