package com.wjglerum;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkiverse.roq.frontmatter.runtime.model.DocumentPage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named("setlist")
@ApplicationScoped
public class Setlist {

    private static final DateTimeFormatter SHORT = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter LONG = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);

    private final Tours tours;
    private final String todayOverride;

    public Setlist(Tours tours,
            @ConfigProperty(name = "setlist.today", defaultValue = "") String todayOverride) {
        this.tours = tours;
        this.todayOverride = todayOverride;
    }

    LocalDate today() {
        return todayOverride.isBlank() ? LocalDate.now() : LocalDate.parse(todayOverride);
    }

    // ---- template entry points ----

    public List<TourGroup> nowPlaying(List<DocumentPage> pages) {
        LocalDate today = today();
        return group(toTalks(pages), tours.list(), today).stream()
                .filter(g -> g.status().equals("now-touring"))
                .sorted(Comparator.comparing(g -> nextOrLastDate(g, today)))
                .toList();
    }

    public List<TourGroup> pastTours(List<DocumentPage> pages) {
        return group(toTalks(pages), tours.list(), today()).stream()
                .filter(g -> g.status().equals("wrapped"))
                .sorted(Comparator.comparing(Setlist::mostRecent).reversed())
                .toList();
    }

    public List<Talk> singles(List<DocumentPage> pages) {
        return toTalks(pages).stream()
                .filter(t -> t.tour() == null)
                .sorted(Comparator.comparing(Talk::date).reversed())
                .toList();
    }

    public Stats statsForPages(List<DocumentPage> pages) {
        return stats(toTalks(pages));
    }

    public TourGroup tour(List<DocumentPage> pages, String slug) {
        if (slug == null) {
            return null;
        }
        return group(toTalks(pages), tours.list(), today()).stream()
                .filter(g -> g.slug().equals(slug))
                .findFirst().orElse(null);
    }

    public TourGroup tour(List<DocumentPage> pages, Object slug) {
        return tour(pages, slug == null ? null : slug.toString());
    }

    public List<Talk> siblings(List<DocumentPage> pages, String slug, String currentUrl) {
        if (slug == null) {
            return List.of();
        }
        return toTalks(pages).stream()
                .filter(t -> slug.equals(t.tour()) && !t.url().equals(currentUrl))
                .sorted(Comparator.comparing(Talk::date).reversed())
                .toList();
    }

    public List<Talk> siblings(List<DocumentPage> pages, Object slug, Object currentUrl) {
        return siblings(pages,
                slug == null ? null : slug.toString(),
                currentUrl == null ? null : currentUrl.toString());
    }

    // ---- adaptation from Roq pages to Talk ----

    private List<Talk> toTalks(List<DocumentPage> pages) {
        LocalDate today = today();
        List<Talk> talks = new ArrayList<>();
        for (DocumentPage p : pages) {
            LocalDate date = p.date().toLocalDate();
            String location = str(p.data("location"));
            talks.add(new Talk(
                    str(p.data("tour")),
                    p.title(),
                    p.url().path(),
                    date,
                    date.format(SHORT),
                    date.format(LONG),
                    date.getYear(),
                    location,
                    country(location),
                    str(p.data("conference")),
                    str(p.data("type")),
                    p.data("video") != null,
                    !date.isBefore(today)));
        }
        return talks;
    }

    private static String str(Object o) {
        return o == null ? null : o.toString();
    }

    private static String country(String location) {
        if (location == null || location.equals("Online")) {
            return null;
        }
        int comma = location.lastIndexOf(',');
        return comma < 0 ? null : location.substring(comma + 1).trim();
    }

    // ---- pure logic (unit-tested directly) ----

    static List<TourGroup> group(List<Talk> talks, Map<String, Tours.TourMeta> meta, LocalDate today) {
        Map<String, List<Talk>> byTour = new LinkedHashMap<>();
        for (Talk t : talks) {
            if (t.tour() == null) {
                continue;
            }
            if (!meta.containsKey(t.tour())) {
                throw new IllegalStateException("Talk \"" + t.title()
                        + "\" references unknown tour slug \"" + t.tour() + "\" not present in tours.yml");
            }
            byTour.computeIfAbsent(t.tour(), k -> new ArrayList<>()).add(t);
        }
        List<TourGroup> groups = new ArrayList<>();
        for (Map.Entry<String, List<Talk>> e : byTour.entrySet()) {
            List<Talk> sorted = e.getValue().stream()
                    .sorted(Comparator.comparing(Talk::date).reversed())
                    .toList();
            Tours.TourMeta m = meta.get(e.getKey());
            groups.add(new TourGroup(e.getKey(), m.title(), m.subtitle(),
                    sorted, sorted.size(), statusOf(sorted, today), recentlyActive(sorted, today)));
        }
        return groups;
    }

    static String statusOf(List<Talk> group, LocalDate today) {
        boolean active = group.stream().anyMatch(t -> !t.date().isBefore(today));
        return active ? "now-touring" : "wrapped";
    }

    static boolean recentlyActive(List<Talk> group, LocalDate today) {
        LocalDate cutoff = today.minusMonths(12);
        return group.stream().map(Talk::date).max(Comparator.naturalOrder())
                .map(d -> !d.isBefore(cutoff)).orElse(false);
    }

    static Stats stats(List<Talk> talks) {
        int shows = talks.size();
        long signature = talks.stream().map(Talk::tour).filter(Objects::nonNull).distinct().count();
        long cities = talks.stream().map(Talk::location)
                .filter(l -> l != null && !l.equals("Online")).distinct().count();
        long countries = talks.stream().map(Talk::country).filter(Objects::nonNull).distinct().count();
        int since = talks.stream().mapToInt(Talk::year).min().orElse(0);
        return new Stats(shows, (int) signature, (int) cities, (int) countries, since);
    }

    private static LocalDate nextOrLastDate(TourGroup g, LocalDate today) {
        return g.talks().stream().map(Talk::date)
                .filter(d -> !d.isBefore(today)).min(Comparator.naturalOrder())
                .orElseGet(() -> mostRecent(g));
    }

    private static LocalDate mostRecent(TourGroup g) {
        return g.talks().stream().map(Talk::date).max(Comparator.naturalOrder()).orElse(LocalDate.MIN);
    }
}
