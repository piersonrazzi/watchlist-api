package dev.razzi.watchlist.web;

import dev.razzi.watchlist.service.WatchlistService;
import dev.razzi.watchlist.web.dto.AddItemRequest;
import dev.razzi.watchlist.web.dto.CreateWatchlistRequest;
import dev.razzi.watchlist.web.dto.WatchlistResponse;
import java.net.URI;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * HTTP layer only: parse the request, validate it (@Valid), call the service,
 * and pick the status code. No business logic lives here.
 */
@RestController
@RequestMapping("/api/watchlists")
public class WatchlistController {

    private final WatchlistService service;

    public WatchlistController(WatchlistService service) {
        this.service = service;
    }

    /** 201 Created + a Location header pointing at the new resource. */
    @PostMapping
    public ResponseEntity<WatchlistResponse> create(@Valid @RequestBody CreateWatchlistRequest request,
                                                    UriComponentsBuilder uriBuilder) {
        WatchlistResponse created = service.create(request);
        URI location = uriBuilder.path("/api/watchlists/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public List<WatchlistResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public WatchlistResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/{id}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public WatchlistResponse addItem(@PathVariable Long id, @Valid @RequestBody AddItemRequest request) {
        return service.addItem(id, request);
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(@PathVariable Long id, @PathVariable Long itemId) {
        service.removeItem(id, itemId);
    }
}
