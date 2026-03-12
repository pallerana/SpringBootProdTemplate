package com.example.account.util;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Utility class for common DTO/entity conversions using functional mappers.
 */
public final class DtoConverter {

    private DtoConverter() {
        // Utility class
    }

    /**
     * Convert a single source object using the provided mapper function.
     *
     * @param source the source instance (may be null)
     * @param mapper mapping function (must not be null)
     * @param <S>    source type
     * @param <T>    target type
     * @return mapped instance or null if source is null
     */
    public static <S, T> T convert(S source, Function<S, T> mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        return source == null ? null : mapper.apply(source);
    }

    /**
     * Convert a collection of source objects using the provided mapper function.
     *
     * @param sources collection of source instances (may be null or empty)
     * @param mapper  mapping function (must not be null)
     * @param <S>     source type
     * @param <T>     target type
     * @return immutable list of mapped instances (never null)
     */
    public static <S, T> List<T> convertList(Collection<S> sources, Function<S, T> mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        if (sources == null || sources.isEmpty()) {
            return List.of();
        }
        return sources.stream()
                .map(mapper)
                .toList();
    }
}

