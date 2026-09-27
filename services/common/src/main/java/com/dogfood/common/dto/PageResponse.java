package com.dogfood.common.dto;

import java.util.List;

/**
 * Paginated response wrapper.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {}
