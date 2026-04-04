package com.kielakjr.search_engine.search;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {
  private final SearchService searchService;

  @GetMapping
  public List<SearchResponse> search(@RequestParam String query) {
    return searchService.search(query);
  }
}
