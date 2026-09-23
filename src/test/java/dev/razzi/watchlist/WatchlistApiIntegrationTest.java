package dev.razzi.watchlist;

import static org.assertj.core.api.Assertions.assertThat;

import dev.razzi.watchlist.domain.InstrumentType;
import dev.razzi.watchlist.web.dto.AddItemRequest;
import dev.razzi.watchlist.web.dto.CreateWatchlistRequest;
import dev.razzi.watchlist.web.dto.WatchlistItemResponse;
import dev.razzi.watchlist.web.dto.WatchlistResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * End-to-end test: boots the whole application on a random port with a real
 * embedded Tomcat and H2, then drives it over real HTTP, the way a client would.
 * Slowest test type, so we keep one happy-path lifecycle here and put edge
 * cases in the faster unit and slice tests.
 *
 * Migration notes:
 * - Boot 2.7/3.x used TestRestTemplate + Jackson JsonNode trees.
 * - Boot 4 / Spring Framework 7 adds RestTestClient: a fluent client with
 *   built-in assertions. Because the DTOs are records, we send and receive
 *   them directly instead of building maps and walking JSON trees.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class WatchlistApiIntegrationTest {

    @Autowired
    private RestTestClient client;

    @Test
    void watchlistLifecycle() {
        // 1. Create a watchlist -> 201 + Location header
        WatchlistResponse created = client.post().uri("/api/watchlists")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateWatchlistRequest("Swing trades"))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("Location")
                .expectBody(WatchlistResponse.class)
                .returnResult().getResponseBody();
        assertThat(created).isNotNull();
        long id = created.id();

        // 2. Add a put option (the service upper-cases the symbol)
        LocalDate expiration = LocalDate.now().plusMonths(2);
        WatchlistResponse withItem = client.post().uri("/api/watchlists/{id}/items", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AddItemRequest("tsla", InstrumentType.PUT, new BigDecimal("200"), expiration, null))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(WatchlistResponse.class)
                .returnResult().getResponseBody();
        assertThat(withItem).isNotNull();
        WatchlistItemResponse item = withItem.items().get(0);
        assertThat(item.symbol()).isEqualTo("TSLA");
        assertThat(item.expiration()).isEqualTo(expiration);

        // 3. Dates must travel as ISO-8601 strings, not numeric arrays
        client.get().uri("/api/watchlists/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.items[0].expiration").isEqualTo(expiration.toString());

        // 4. Remove the item, then confirm it's gone
        client.delete().uri("/api/watchlists/{id}/items/{itemId}", id, item.id())
                .exchange()
                .expectStatus().isNoContent();
        client.get().uri("/api/watchlists/{id}", id)
                .exchange()
                .expectBody().jsonPath("$.items").isEmpty();

        // 5. Delete the watchlist; fetching it now returns a 404 ProblemDetail
        client.delete().uri("/api/watchlists/{id}", id)
                .exchange()
                .expectStatus().isNoContent();
        client.get().uri("/api/watchlists/{id}", id)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }
}
