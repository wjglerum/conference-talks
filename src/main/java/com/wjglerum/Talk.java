package com.wjglerum;

import java.time.LocalDate;

public record Talk(
        String tour,
        String title,
        String url,
        LocalDate date,
        String dateShort,
        String dateLong,
        int year,
        String location,
        String country,
        String conference,
        String type,
        boolean hasVideo,
        boolean upcoming) {
}
