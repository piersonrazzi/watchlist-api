package dev.razzi.watchlist.service;

import dev.razzi.watchlist.domain.InstrumentType;
import dev.razzi.watchlist.domain.Watchlist;
import dev.razzi.watchlist.domain.WatchlistItem;
import dev.razzi.watchlist.repository.WatchlistRepository;
import dev.razzi.watchlist.web.dto.AddItemRequest;
import dev.razzi.watchlist.web.dto.CreateWatchlistRequest;
import dev.razzi.watchlist.web.dto.WatchlistResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic and transaction boundaries.
 *
 * Every public method runs in a transaction (class-level @Transactional).
 * Entities are converted to response DTOs *inside* the transaction, so lazy
 * collections can still load. spring.jpa.open-in-view=false keeps the
 * controller from touching the database directly.
 */
@Service
@Transactional
public class WatchlistService {

    private final WatchlistRepository repository;

    // Constructor injection: dependencies are explicit and the class is easy to unit test.
    public WatchlistService(WatchlistRepository repository) {
        this.repository = repository;
    }

    public WatchlistResponse create(CreateWatchlistRequest request) {
        Watchlist saved = repository.save(new Watchlist(request.name().trim()));
        return WatchlistResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<WatchlistResponse> findAll() {
        return repository.findAllWithItems().stream()
                .map(WatchlistResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public WatchlistResponse findById(Long id) {
        return WatchlistResponse.from(load(id));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Watchlist " + id + " not found");
        }
        repository.deleteById(id);
    }

    public WatchlistResponse addItem(Long watchlistId, AddItemRequest request) {
        validateInstrumentFields(request);
        Watchlist watchlist = load(watchlistId);

        String symbol = request.symbol().trim().toUpperCase(Locale.ROOT);
        boolean duplicate = watchlist.getItems().stream().anyMatch(existing ->
                existing.getSymbol().equals(symbol)
                        && existing.getType() == request.type()
                        && sameStrike(existing.getStrike(), request.strike())
                        && Objects.equals(existing.getExpiration(), request.expiration()));
        if (duplicate) {
            throw new InvalidRequestException("That instrument is already on this watchlist");
        }

        watchlist.addItem(new WatchlistItem(symbol, request.type(), request.strike(),
                request.expiration(), request.notes()));
        // Flush so the database assigns the new item's id before we build the response.
        repository.saveAndFlush(watchlist);
        return WatchlistResponse.from(watchlist);
    }

    public void removeItem(Long watchlistId, Long itemId) {
        Watchlist watchlist = load(watchlistId);
        WatchlistItem item = watchlist.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "Item " + itemId + " not found on watchlist " + watchlistId));
        // orphanRemoval=true on the entity turns this into a DELETE at commit.
        watchlist.removeItem(item);
    }

    private Watchlist load(Long id) {
        return repository.findWithItemsById(id)
                .orElseThrow(() -> new NotFoundException("Watchlist " + id + " not found"));
    }

    /** Cross-field rule: options need strike + expiration; stocks must have neither. */
    private static void validateInstrumentFields(AddItemRequest request) {
        InstrumentType type = request.type();
        boolean hasStrike = request.strike() != null;
        boolean hasExpiration = request.expiration() != null;

        if (type.isOption() && (!hasStrike || !hasExpiration)) {
            throw new InvalidRequestException(type + " options require both strike and expiration");
        }
        if (!type.isOption() && (hasStrike || hasExpiration)) {
            throw new InvalidRequestException("STOCK items must not have a strike or expiration");
        }
    }

    /**
     * Compares strikes by numeric value: 150 and 150.00 are the same strike.
     * (BigDecimal.equals would say they differ, because their scale differs.)
     */
    private static boolean sameStrike(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) {
            return a == b;
        }
        return a.compareTo(b) == 0;
    }
}
