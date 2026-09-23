package dev.razzi.watchlist.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * JSON body for POST /api/watchlists.
 *
 * A record (Java 16+): the compiler generates the constructor, accessor
 * (name()), equals, hashCode and toString. Jackson and Bean Validation
 * both understand records, so annotations go straight on the components.
 * In the Java 11 version this was a 30-line class with a setter.
 */
public record CreateWatchlistRequest(@NotBlank @Size(max = 80) String name) {
}
