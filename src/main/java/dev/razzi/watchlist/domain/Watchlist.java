package dev.razzi.watchlist.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Aggregate root: a named list of instruments.
 *
 * Note the javax.persistence imports. In Spring Boot 3 these become
 * jakarta.persistence, which is the single biggest mechanical change
 * in a 2.x to 3.x migration.
 */
@Entity
@Table(name = "watchlists")
public class Watchlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // The watchlist owns its items: saving/deleting the watchlist cascades,
    // and removing an item from this list deletes its row (orphanRemoval).
    @OneToMany(mappedBy = "watchlist", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<WatchlistItem> items = new ArrayList<>();

    /** Required by JPA; not for application code. */
    protected Watchlist() {
    }

    public Watchlist(String name) {
        this.name = name;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    /** Keeps both sides of the bidirectional relationship in sync. */
    public void addItem(WatchlistItem item) {
        items.add(item);
        item.setWatchlist(this);
    }

    public void removeItem(WatchlistItem item) {
        items.remove(item);
        item.setWatchlist(null);
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

    public List<WatchlistItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
