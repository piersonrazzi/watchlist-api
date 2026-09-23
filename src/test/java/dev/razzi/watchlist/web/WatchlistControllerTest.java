package dev.razzi.watchlist.web;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.razzi.watchlist.service.NotFoundException;
import dev.razzi.watchlist.service.WatchlistService;
import dev.razzi.watchlist.web.dto.WatchlistResponse;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Web-layer "slice" test: Spring starts only the MVC pieces (controllers,
 * JSON conversion, validation, @RestControllerAdvice). The service is
 * replaced with a Mockito mock, so no database is involved.
 *
 * Migration notes:
 * - Boot 4 moved @WebMvcTest to the spring-boot-webmvc-test module
 *   (package org.springframework.boot.webmvc.test.autoconfigure).
 * - @MockBean (Boot) was replaced by @MockitoBean (Spring Framework 6.2+).
 * - Request bodies are Java text blocks, so the test no longer needs a
 *   Jackson ObjectMapper (whose package changed in Jackson 3).
 */
@WebMvcTest(WatchlistController.class)
class WatchlistControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private WatchlistService service;

    @Test
    void create_returns201WithLocationHeader() throws Exception {
        when(service.create(any())).thenReturn(
                new WatchlistResponse(7L, "Tech", Instant.parse("2026-01-01T00:00:00Z"), List.of()));

        mvc.perform(post("/api/watchlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Tech"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/watchlists/7")))
                .andExpect(jsonPath("$.name").value("Tech"))
                .andExpect(jsonPath("$.createdAt").value("2026-01-01T00:00:00Z"));
    }

    @Test
    void create_blankName_returns400AndNeverCallsService() throws Exception {
        mvc.perform(post("/api/watchlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.name").exists());

        verifyNoInteractions(service);
    }

    @Test
    void getMissingWatchlist_returns404ProblemDetail() throws Exception {
        when(service.findById(99L)).thenThrow(new NotFoundException("Watchlist 99 not found"));

        mvc.perform(get("/api/watchlists/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Watchlist 99 not found"))
                .andExpect(jsonPath("$.instance").value("/api/watchlists/99"));
    }

    @Test
    void addItem_unknownInstrumentType_returns400() throws Exception {
        mvc.perform(post("/api/watchlists/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"symbol": "AAPL", "type": "FUTURE"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

        verifyNoInteractions(service);
    }
}
