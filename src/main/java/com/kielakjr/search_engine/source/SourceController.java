package com.kielakjr.search_engine.source;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sources")
@RequiredArgsConstructor
public class SourceController {
  private final SourceService sourceService;

  @GetMapping
  public ResponseEntity<List<SourceResponse>> getAllSources() {
    return ResponseEntity.ok(sourceService.getAllSources());
  }

  @PostMapping
  public ResponseEntity<SourceResponse> createSource(@Valid @RequestBody SourceRequest source) {
    return ResponseEntity.ok(sourceService.createSource(source));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteSource(@PathVariable Long id) {
    sourceService.deleteSource(id);
    return ResponseEntity.noContent().build();
  }
}
