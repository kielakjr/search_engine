package com.kielakjr.search_engine.search;

import java.time.LocalDateTime;

import org.springframework.data.elasticsearch.annotations.Document;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(indexName = "pages")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageDocument {
    @Id
    private String id;
    private String url;
    private String title;
    private String content;
    private String domain;
    private LocalDateTime crawledAt;
}
