package dev.razzi.watchlist.web.dto;

import dev.razzi.watchlist.domain.InstrumentType;
import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * JSON body for POST /api/watchlists/{id}/items.
 *
 * Field-level rules live here as annotations. The cross-field rule
 * (options need strike + expiration, stocks must not have them) lives
 * in the service, because it depends on more than one field.
 */
public class AddItemRequest {

    @NotBlank
    @Pattern(regexp = "^[A-Za-z.]{1,10}$", message = "must be 1-10 letters (dots allowed, e.g. BRK.B)")
    private String symbol;

    @NotNull
    private InstrumentType type;

    @Positive
    private BigDecimal strike;

    @FutureOrPresent
    private LocalDate expiration;

    @Size(max = 280)
    private String notes;

    public AddItemRequest() {
    }

    public AddItemRequest(String symbol, InstrumentType type, BigDecimal strike, LocalDate expiration, String notes) {
        this.symbol = symbol;
        this.type = type;
        this.strike = strike;
        this.expiration = expiration;
        this.notes = notes;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public InstrumentType getType() {
        return type;
    }

    public void setType(InstrumentType type) {
        this.type = type;
    }

    public BigDecimal getStrike() {
        return strike;
    }

    public void setStrike(BigDecimal strike) {
        this.strike = strike;
    }

    public LocalDate getExpiration() {
        return expiration;
    }

    public void setExpiration(LocalDate expiration) {
        this.expiration = expiration;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
