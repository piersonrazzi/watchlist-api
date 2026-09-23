package dev.razzi.watchlist.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** One tracked instrument: a stock, or a call/put with a strike and expiration. */
@Entity
@Table(name = "watchlist_items")
public class WatchlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // LAZY: loading an item doesn't automatically load its parent watchlist.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "watchlist_id")
    private Watchlist watchlist;

    @Column(nullable = false, length = 10)
    private String symbol;

    // STRING, not ORDINAL: storing the enum's name survives reordering the enum.
    @Enumerated(EnumType.STRING)
    @Column(name = "instrument_type", nullable = false, length = 5)
    private InstrumentType type;

    // BigDecimal, never double, for prices.
    @Column(precision = 12, scale = 2)
    private BigDecimal strike;

    private LocalDate expiration;

    @Column(length = 280)
    private String notes;

    protected WatchlistItem() {
    }

    public WatchlistItem(String symbol, InstrumentType type, BigDecimal strike, LocalDate expiration, String notes) {
        this.symbol = symbol;
        this.type = type;
        this.strike = strike;
        this.expiration = expiration;
        this.notes = notes;
    }

    void setWatchlist(Watchlist watchlist) {
        this.watchlist = watchlist;
    }

    public Long getId() {
        return id;
    }

    public Watchlist getWatchlist() {
        return watchlist;
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
