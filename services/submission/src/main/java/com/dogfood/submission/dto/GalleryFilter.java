package com.dogfood.submission.dto;

import java.util.List;
import java.util.UUID;

public record GalleryFilter(
    String search,
    UUID trackId,
    List<String> tags
) {}
