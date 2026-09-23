package dev.razzi.watchlist.web.dto;

import dev.razzi.watchlist.domain.InstrumentType;
import dev.razzi.watchlist.domain.WatchlistItem;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Read-only JSON view of one watchlist item. */
public class WatchlistItemResponse {

    private final Long id;
    private final String symbol;
    private final InstrumentType type;
    private final BigDecimal strike;
    private final LocalDate expiration;
    private final String notes;

    public WatchlistItemResponse(Long id, String symbol, InstrumentType type, BigDecimal strike,
                                 LocalDate expiration, String notes) {
        this.id = id;
        this.symbol = symbol;
        this.type = type;
        this.strike = strike;
        this.expiration = expiration;
        this.notes = notes;
    }

    public static WatchlistItemResponse from(WatchlistItem item) {
        return new WatchlistItemResponse(item.getId(), item.getSymbol(), item.getType(),
                item.getStrike(), item.getExpiration(), item.getNotes());
    }

    public Long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public InstrumentType getType() {
        return type;
    }

    public BigDecimal getStrike() {
        return strike;
    }

    public LocalDate getExpiration() {
        return expiration;
    }

    public String getNotes() {
        return notes;
    }
}
