package com.wjglerum;

import java.util.List;

public record TourGroup(
        String slug,
        String title,
        String subtitle,
        List<Talk> talks,
        int dateCount,
        String status,
        boolean recentlyActive) {
}
