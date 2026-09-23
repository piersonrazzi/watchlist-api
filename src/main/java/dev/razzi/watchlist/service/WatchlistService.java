package dev.razzi.watchlist.service;

import dev.razzi.watchlist.domain.InstrumentType;
import dev.razzi.watchlist.domain.Watchlist;
import dev.razzi.watchlist.domain.WatchlistItem;
import dev.razzi.watchlist.repository.WatchlistRepository;
import dev.razzi.watchlist.web.dto.AddItemRequest;
import dev.razzi.watchlist.web.dto.CreateWatchlistRequest;
import dev.razzi.watchlist.web.dto.WatchlistResponse;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic and transaction boundaries.
 *
 * Every public method runs in a transaction (class-level @Transactional).
 * Entities are converted to response DTOs *inside* the transaction, so lazy
 * collections can still load. We also set spring.jpa.open-in-view=false, so
 * the controller never touches the database directly.
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
        Watchlist saved = repository.save(new Watchlist(request.getName().trim()));
        return WatchlistResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<WatchlistResponse> findAll() {
        return repository.findAllWithItems().stream()
                .map(WatchlistResponse::from)
                .collect(Collectors.toList());
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

        String symbol = request.getSymbol().trim().toUpperCase(Locale.ROOT);
        boolean duplicate = watchlist.getItems().stream().anyMatch(existing ->
                existing.getSymbol().equals(symbol)
                        && existing.getType() == request.getType()
                        && equalsNullable(existing.getStrike(), request.getStrike())
                        && equalsNullable(existing.getExpiration(), request.getExpiration()));
        if (duplicate) {
            throw new InvalidRequestException("That instrument is already on this watchlist");
        }

        watchlist.addItem(new WatchlistItem(symbol, request.getType(), request.getStrike(),
                request.getExpiration(), request.getNotes()));
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
        InstrumentType type = request.getType();
        boolean hasStrike = request.getStrike() != null;
        boolean hasExpiration = request.getExpiration() != null;

        if (type.isOption() && (!hasStrike || !hasExpiration)) {
            throw new InvalidRequestException(type + " options require both strike and expiration");
        }
        if (!type.isOption() && (hasStrike || hasExpiration)) {
            throw new InvalidRequestException("STOCK items must not have a strike or expiration");
        }
    }

    private static boolean equalsNullable(Object a, Object b) {
        if (a == null || b == null) {
            return a == b;
        }
        // compareTo-style equality for BigDecimal: 150 and 150.00 are the same strike.
        if (a instanceof java.math.BigDecimal decimal && b instanceof java.math.BigDecimal decimal1) {
            return decimal.compareTo(decimal1) == 0;
        }
        return a.equals(b);
    }
}
