package com.kielakjr.search_engine.crawler;

import org.jsoup.nodes.Document;
import java.io.IOException;

public interface JsoupFetcher {
  Document fetch(String url) throws IOException;
}
