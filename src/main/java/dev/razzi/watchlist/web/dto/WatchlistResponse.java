package dev.razzi.watchlist.web.dto;

import dev.razzi.watchlist.domain.Watchlist;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/** Read-only JSON view of a watchlist and its items. */
public class WatchlistResponse {

    private final Long id;
    private final String name;
    private final Instant createdAt;
    private final List<WatchlistItemResponse> items;

    public WatchlistResponse(Long id, String name, Instant createdAt, List<WatchlistItemResponse> items) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.items = items;
    }

    /** Must be called inside a transaction, since it touches the lazy items collection. */
    public static WatchlistResponse from(Watchlist watchlist) {
        List<WatchlistItemResponse> items = watchlist.getItems().stream()
                .map(WatchlistItemResponse::from)
                .collect(Collectors.toList()); // Java 16+ would just use .toList()
        return new WatchlistResponse(watchlist.getId(), watchlist.getName(), watchlist.getCreatedAt(), items);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<WatchlistItemResponse> getItems() {
        return items;
    }
}
