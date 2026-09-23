package dev.razzi.watchlist.web.dto;

import dev.razzi.watchlist.domain.InstrumentType;
import dev.razzi.watchlist.domain.WatchlistItem;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Read-only JSON view of one watchlist item. */
public record WatchlistItemResponse(
        Long id,
        String symbol,
        InstrumentType type,
        BigDecimal strike,
        LocalDate expiration,
        String notes) {

    public static WatchlistItemResponse from(WatchlistItem item) {
        return new WatchlistItemResponse(item.getId(), item.getSymbol(), item.getType(),
                item.getStrike(), item.getExpiration(), item.getNotes());
    }
}
