package dev.razzi.watchlist.repository;

import static org.assertj.core.api.Assertions.assertThat;

import dev.razzi.watchlist.domain.InstrumentType;
import dev.razzi.watchlist.domain.Watchlist;
import dev.razzi.watchlist.domain.WatchlistItem;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

/**
 * JPA "slice" test: starts Hibernate against the in-memory H2 database,
 * nothing else. Each test runs in a transaction that is rolled back afterwards.
 *
 * em.flush() pushes pending SQL to the database; em.clear() empties Hibernate's
 * first-level cache, so the next read really comes from the database instead of memory.
 */
@DataJpaTest
class WatchlistRepositoryTest {

    @Autowired
    private WatchlistRepository repository;

    @Autowired
    private TestEntityManager em;

    @Test
    void findWithItemsById_loadsItemsInInsertionOrder() {
        Watchlist watchlist = new Watchlist("Earnings plays");
        watchlist.addItem(new WatchlistItem("NVDA", InstrumentType.CALL, new BigDecimal("150.00"),
                LocalDate.of(2026, 12, 18), "into earnings"));
        watchlist.addItem(new WatchlistItem("NVDA", InstrumentType.STOCK, null, null, null));
        Long id = em.persistFlushFind(watchlist).getId();
        em.clear();

        Watchlist loaded = repository.findWithItemsById(id).orElseThrow();

        assertThat(loaded.getCreatedAt()).isNotNull();
        assertThat(loaded.getItems())
                .extracting(WatchlistItem::getType)
                .containsExactly(InstrumentType.CALL, InstrumentType.STOCK);
    }

    @Test
    void removingItemFromWatchlist_deletesTheOrphanedRow() {
        Watchlist watchlist = new Watchlist("Swing");
        watchlist.addItem(new WatchlistItem("AMD", InstrumentType.STOCK, null, null, null));
        watchlist.addItem(new WatchlistItem("MSFT", InstrumentType.STOCK, null, null, null));
        Long id = em.persistFlushFind(watchlist).getId();
        em.clear();

        Watchlist loaded = repository.findWithItemsById(id).orElseThrow();
        loaded.removeItem(loaded.getItems().get(0));
        em.flush();
        em.clear();

        Long remaining = em.getEntityManager()
                .createQuery("select count(i) from WatchlistItem i", Long.class)
                .getSingleResult();
        assertThat(remaining).isEqualTo(1L);
    }

    @Test
    void findAllWithItems_returnsEachWatchlistOnce() {
        Watchlist a = new Watchlist("A");
        a.addItem(new WatchlistItem("SPY", InstrumentType.STOCK, null, null, null));
        a.addItem(new WatchlistItem("QQQ", InstrumentType.STOCK, null, null, null));
        em.persist(a);
        em.persist(new Watchlist("B"));
        em.flush();
        em.clear();

        // Without "distinct", the fetch join would return "A" twice (once per item).
        assertThat(repository.findAllWithItems())
                .extracting(Watchlist::getName)
                .containsExactly("A", "B");
    }
}
