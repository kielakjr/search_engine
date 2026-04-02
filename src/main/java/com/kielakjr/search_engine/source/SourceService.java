package com.kielakjr.search_engine.source;

import java.util.List;

import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SourceService {
  private final SourceRepository sourceRepository;

  public List<SourceResponse> getAllSources() {
    return sourceRepository.findAll().stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  public SourceResponse createSource(SourceRequest request) {
    if (sourceRepository.findByUrl(request.getUrl()).isPresent()) {
      throw new IllegalArgumentException("Source with the same URL already exists");
    }
    Source source = Source.builder()
        .url(request.getUrl())
        .name(request.getName())
        .active(true)
        .build();
    return toResponse(sourceRepository.save(source));
  }

  public void deleteSource(Long id) {
    Source source = sourceRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Source not found"));
    sourceRepository.delete(source);
  }

  private SourceResponse toResponse(Source source) {
    return SourceResponse.builder()
        .id(source.getId())
        .url(source.getUrl())
        .name(source.getName())
        .active(source.isActive())
        .createdAt(source.getCreatedAt())
        .build();
  }
}
