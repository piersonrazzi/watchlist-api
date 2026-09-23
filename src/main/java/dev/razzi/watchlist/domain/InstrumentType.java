package dev.razzi.watchlist.domain;

/** What a watchlist entry tracks: the underlying stock, or a call/put contract on it. */
public enum InstrumentType {
    STOCK,
    CALL,
    PUT;

    public boolean isOption() {
        return this == CALL || this == PUT;
    }
}
