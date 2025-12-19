package com.siupo.restaurant.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageableUtil {
    private PageableUtil() {}

    public static Pageable create(int page, int size, String sortBy) {
        Sort sort = parseSort(sortBy);
        return PageRequest.of(page, size, sort);
    }

    private static Sort parseSort(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "id");
        }
        if (sortBy.contains(",")) {
            String[] parts = sortBy.split(",");
            String field = parts[0].trim();
            String direction = parts.length > 1 ? parts[1].trim() : "asc";
            return Sort.by(Sort.Direction.fromString(direction), field);
        }
        return Sort.by(Sort.Direction.ASC, sortBy.trim());
    }
}
