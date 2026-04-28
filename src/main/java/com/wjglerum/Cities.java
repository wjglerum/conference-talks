package com.wjglerum;

import java.util.Map;

import io.quarkiverse.roq.data.runtime.annotations.DataMapping;

@DataMapping("cities")
public record Cities(Map<String, Coordinates> list) {

    public record Coordinates(double lat, double lng) {
    }
}
