package com.kielakjr.search_engine.search;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
  List<SearchHistory> findByUserIdOrderByTimestampDesc(Long userId);

}
