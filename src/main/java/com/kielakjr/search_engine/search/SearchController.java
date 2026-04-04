package com.kielakjr.search_engine.search;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {
  private final SearchService searchService;

  @GetMapping
  public ResponseEntity<List<SearchResponse>> search(
    @RequestParam String query,@RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(required = false) String domain) {
    return ResponseEntity.ok(searchService.search(query, page, size, domain));
  }
}
