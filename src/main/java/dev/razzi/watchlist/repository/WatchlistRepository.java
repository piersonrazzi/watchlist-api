package dev.razzi.watchlist.repository;

import dev.razzi.watchlist.domain.Watchlist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data generates the implementation at startup: save, findById,
 * deleteById, existsById, etc. all come for free from JpaRepository.
 */
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    /*
     * "left join fetch" loads each watchlist AND its items in one SQL query.
     * Without it, listing 50 watchlists would run 1 query for the lists plus
     * 50 more for their items: the N+1 query problem.
     *
     * Hibernate 5 needed "select distinct" here, or a watchlist with 3 items
     * came back 3 times. Hibernate 6+ de-duplicates fetch-join results itself,
     * so "distinct" was removed in the Boot 3 migration.
     * WatchlistRepositoryTest.findAllWithItems_returnsEachWatchlistOnce proves it.
     */
    @Query("select w from Watchlist w left join fetch w.items order by w.id")
    List<Watchlist> findAllWithItems();

    @Query("select w from Watchlist w left join fetch w.items where w.id = :id")
    Optional<Watchlist> findWithItemsById(@Param("id") Long id);
}
