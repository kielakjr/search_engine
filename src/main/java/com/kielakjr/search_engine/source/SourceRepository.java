package com.kielakjr.search_engine.source;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SourceRepository extends JpaRepository<Source, Long> {
  public Optional<Source> findByUrl(String url);
}
