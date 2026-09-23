package dev.razzi.watchlist.web;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.razzi.watchlist.service.NotFoundException;
import dev.razzi.watchlist.service.WatchlistService;
import dev.razzi.watchlist.web.dto.CreateWatchlistRequest;
import dev.razzi.watchlist.web.dto.WatchlistResponse;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Web-layer "slice" test: Spring starts only the MVC pieces (controllers,
 * JSON conversion, validation, @RestControllerAdvice). The service is
 * replaced with a Mockito mock, so no database is involved.
 *
 * Migration note: @MockBean is deprecated in Boot 3.4 and removed in Boot 4,
 * where it becomes @MockitoBean.
 */
@WebMvcTest(WatchlistController.class)
class WatchlistControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @MockBean
    private WatchlistService service;

    @Test
    void create_returns201WithLocationHeader() throws Exception {
        when(service.create(any())).thenReturn(
                new WatchlistResponse(7L, "Tech", Instant.parse("2026-01-01T00:00:00Z"), List.of()));

        mvc.perform(post("/api/watchlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new CreateWatchlistRequest("Tech"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/watchlists/7")))
                .andExpect(jsonPath("$.name").value("Tech"));
    }

    @Test
    void create_blankName_returns400AndNeverCallsService() throws Exception {
        mvc.perform(post("/api/watchlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());

        verifyNoInteractions(service);
    }

    @Test
    void getMissingWatchlist_returns404WithMessage() throws Exception {
        when(service.findById(99L)).thenThrow(new NotFoundException("Watchlist 99 not found"));

        mvc.perform(get("/api/watchlists/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Watchlist 99 not found"));
    }

    @Test
    void addItem_unknownInstrumentType_returns400() throws Exception {
        mvc.perform(post("/api/watchlists/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbol\": \"AAPL\", \"type\": \"FUTURE\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }
}
