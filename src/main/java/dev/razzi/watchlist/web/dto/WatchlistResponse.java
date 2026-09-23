package dev.razzi.watchlist.web.dto;

import dev.razzi.watchlist.domain.Watchlist;
import java.time.Instant;
import java.util.List;

/** Read-only JSON view of a watchlist and its items. */
public record WatchlistResponse(
        Long id,
        String name,
        Instant createdAt,
        List<WatchlistItemResponse> items) {

    /** Must be called inside a transaction, since it touches the lazy items collection. */
    public static WatchlistResponse from(Watchlist watchlist) {
        List<WatchlistItemResponse> items = watchlist.getItems().stream()
                .map(WatchlistItemResponse::from)
                .toList(); // Java 16+: returns an unmodifiable list
        return new WatchlistResponse(watchlist.getId(), watchlist.getName(), watchlist.getCreatedAt(), items);
    }
}
