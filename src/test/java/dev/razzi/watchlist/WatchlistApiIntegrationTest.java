package dev.razzi.watchlist;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * End-to-end test: boots the whole application on a random port with a real
 * embedded Tomcat and H2, then drives it over real HTTP, the way a client would.
 * Slowest test type, so we keep one happy-path lifecycle here and put edge
 * cases in the faster unit and slice tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WatchlistApiIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void watchlistLifecycle() {
        // 1. Create a watchlist
        ResponseEntity<JsonNode> created =
                rest.postForEntity("/api/watchlists", Map.of("name", "Swing trades"), JsonNode.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getHeaders().getLocation()).isNotNull();
        long id = created.getBody().get("id").asLong();

        // 2. Add a put option (symbol gets upper-cased by the service)
        Map<String, Object> put = new LinkedHashMap<>();
        put.put("symbol", "tsla");
        put.put("type", "PUT");
        put.put("strike", 200);
        put.put("expiration", LocalDate.now().plusMonths(2).toString());
        ResponseEntity<JsonNode> withItem =
                rest.postForEntity("/api/watchlists/" + id + "/items", put, JsonNode.class);
        assertThat(withItem.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode item = withItem.getBody().get("items").get(0);
        assertThat(item.get("symbol").asText()).isEqualTo("TSLA");
        long itemId = item.get("id").asLong();

        // 3. Remove the item, then confirm it's gone
        rest.delete("/api/watchlists/" + id + "/items/" + itemId);
        JsonNode afterRemoval = rest.getForObject("/api/watchlists/" + id, JsonNode.class);
        assertThat(afterRemoval.get("items")).isEmpty();

        // 4. Delete the watchlist; fetching it now returns 404
        rest.delete("/api/watchlists/" + id);
        ResponseEntity<JsonNode> gone = rest.getForEntity("/api/watchlists/" + id, JsonNode.class);
        assertThat(gone.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
