package dev.razzi.watchlist.web.dto;

import dev.razzi.watchlist.domain.InstrumentType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JSON body for POST /api/watchlists/{id}/items.
 *
 * Field-level rules are annotations here. The cross-field rule
 * (options need strike + expiration, stocks must not have them) lives
 * in the service, because it depends on more than one field.
 */
public record AddItemRequest(
        @NotBlank
        @Pattern(regexp = "^[A-Za-z.]{1,10}$", message = "must be 1-10 letters (dots allowed, e.g. BRK.B)")
        String symbol,

        @NotNull
        InstrumentType type,

        @Positive
        BigDecimal strike,

        @FutureOrPresent
        LocalDate expiration,

        @Size(max = 280)
        String notes) {
}
