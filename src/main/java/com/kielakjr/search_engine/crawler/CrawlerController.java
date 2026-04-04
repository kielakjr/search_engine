package com.kielakjr.search_engine.crawler;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import lombok.RequiredArgsConstructor;
import com.kielakjr.search_engine.source.SourceRepository;
import com.kielakjr.search_engine.source.Source;

@RestController
@RequestMapping("/api/crawler")
@RequiredArgsConstructor
public class CrawlerController {
    private final CrawlerService crawlerService;
    private final SourceRepository sourceRepository;

    @PostMapping("/start/{sourceId}")
    public ResponseEntity<String> startCrawl(@PathVariable Long sourceId) {
        Source source = sourceRepository.findById(sourceId)
            .orElseThrow(() -> new RuntimeException("Source not found"));
        crawlerService.startCrawl(source);
        return ResponseEntity.accepted().body("Crawl started for source: " + source.getUrl());
    }
}
