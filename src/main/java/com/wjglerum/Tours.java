package com.wjglerum;

import java.util.Map;

import io.quarkiverse.roq.data.runtime.annotations.DataMapping;

@DataMapping("tours")
public record Tours(Map<String, TourMeta> list) {

    public record TourMeta(String title, String subtitle) {
    }
}
