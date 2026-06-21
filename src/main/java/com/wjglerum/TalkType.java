package com.wjglerum;

import java.util.Arrays;

import io.quarkus.qute.TemplateData;

/**
 * The kind of session a talk is. The {@code slug} matches the front matter
 * {@code type} value and the CSS badge class; the {@code label} is the display text.
 */
@TemplateData
public enum TalkType {

    TALK("talk", "Conference"),
    MEETUP("meetup", "Meetup"),
    DEEPDIVE("deepdive", "Deep Dive"),
    WORKSHOP("workshop", "Workshop"),
    PODCAST("podcast", "Podcast"),
    TECHTALK("techtalk", "Tech Talk"),
    BOOTH("booth", "Booth Demo");

    private final String slug;
    private final String label;

    TalkType(String slug, String label) {
        this.slug = slug;
        this.label = label;
    }

    public String slug() {
        return slug;
    }

    public String label() {
        return label;
    }

    /**
     * Resolves a front matter {@code type} value to a {@link TalkType}. Fails fast on an
     * unknown value so a typo in a talk file surfaces at build time rather than rendering wrong.
     */
    public static TalkType from(String value) {
        return Arrays.stream(values())
                .filter(t -> t.slug.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Unknown talk type \"" + value + "\"; expected one of "
                                + Arrays.toString(Arrays.stream(values()).map(TalkType::slug).toArray())));
    }
}
