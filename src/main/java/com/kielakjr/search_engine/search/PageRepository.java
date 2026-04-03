package com.kielakjr.search_engine.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PageRepository extends ElasticsearchRepository<PageDocument, String> {

}
