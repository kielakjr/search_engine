package com.kielakjr.search_engine.crawler;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CrawlJobRepository extends JpaRepository<CrawlJob, Long> {
  List<CrawlJob> findAllByOrderByCreatedAtDesc();
}
