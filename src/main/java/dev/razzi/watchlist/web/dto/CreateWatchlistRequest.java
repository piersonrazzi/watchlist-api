package dev.razzi.watchlist.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * JSON body for POST /api/watchlists.
 *
 * Java 11 has no records, so request DTOs are plain classes with a no-arg
 * constructor and setters for Jackson. After moving to Java 21 in the
 * Boot 3 migration, these collapse into one-line records.
 */
public class CreateWatchlistRequest {

    @NotBlank
    @Size(max = 80)
    private String name;

    public CreateWatchlistRequest() {
    }

    public CreateWatchlistRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
