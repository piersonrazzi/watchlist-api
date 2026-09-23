package dev.razzi.watchlist.service;

/** Thrown when a requested watchlist or item doesn't exist. Mapped to HTTP 404. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
