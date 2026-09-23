package dev.razzi.watchlist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import dev.razzi.watchlist.domain.InstrumentType;
import dev.razzi.watchlist.domain.Watchlist;
import dev.razzi.watchlist.domain.WatchlistItem;
import dev.razzi.watchlist.repository.WatchlistRepository;
import dev.razzi.watchlist.web.dto.AddItemRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pure unit test: no Spring context, no database. Mockito fakes the
 * repository so we test only the service's business rules. Runs in milliseconds.
 */
@ExtendWith(MockitoExtension.class)
class WatchlistServiceTest {

    private static final LocalDate NEXT_MONTH = LocalDate.now().plusMonths(1);

    @Mock
    private WatchlistRepository repository;

    @InjectMocks
    private WatchlistService service;

    @Test
    void addItem_rejectsOptionWithoutStrike() {
        AddItemRequest request = new AddItemRequest("AAPL", InstrumentType.CALL, null, NEXT_MONTH, null);

        assertThatThrownBy(() -> service.addItem(1L, request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("strike");
        // Validation fails fast, before touching the database.
        verifyNoInteractions(repository);
    }

    @Test
    void addItem_rejectsStockWithExpiration() {
        AddItemRequest request = new AddItemRequest("AAPL", InstrumentType.STOCK, null, NEXT_MONTH, null);

        assertThatThrownBy(() -> service.addItem(1L, request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("STOCK");
    }

    @Test
    void addItem_normalizesSymbolAndSaves() {
        Watchlist watchlist = new Watchlist("Core");
        when(repository.findWithItemsById(1L)).thenReturn(Optional.of(watchlist));

        service.addItem(1L, new AddItemRequest("  aapl ", InstrumentType.STOCK, null, null, "long-term"));

        assertThat(watchlist.getItems()).singleElement().satisfies(item -> {
            assertThat(item.getSymbol()).isEqualTo("AAPL");
            assertThat(item.getNotes()).isEqualTo("long-term");
        });
        verify(repository).saveAndFlush(watchlist);
    }

    @Test
    void addItem_rejectsDuplicateEvenWithDifferentDecimalScale() {
        Watchlist watchlist = new Watchlist("Earnings");
        watchlist.addItem(new WatchlistItem("NVDA", InstrumentType.CALL, new BigDecimal("150"), NEXT_MONTH, null));
        when(repository.findWithItemsById(1L)).thenReturn(Optional.of(watchlist));

        // 150.00 and 150 are the same strike: BigDecimal.equals would say no, compareTo says yes.
        AddItemRequest sameContract =
                new AddItemRequest("nvda", InstrumentType.CALL, new BigDecimal("150.00"), NEXT_MONTH, null);

        assertThatThrownBy(() -> service.addItem(1L, sameContract))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("already");
    }

    @Test
    void findById_throwsNotFoundForMissingWatchlist() {
        when(repository.findWithItemsById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(42L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Watchlist 42 not found");
    }

    @Test
    void delete_throwsNotFoundForMissingWatchlist() {
        when(repository.existsById(42L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(42L)).isInstanceOf(NotFoundException.class);
    }
}
