package dev.razzi.watchlist.service;

/**
 * Thrown for business-rule violations that simple field annotations can't
 * express (e.g. "options need a strike, stocks must not have one").
 * Mapped to HTTP 400.
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
