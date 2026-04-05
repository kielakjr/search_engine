package com.kielakjr.search_engine.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class DefaultJsoupFetcher implements JsoupFetcher {
  @Override
  public Document fetch(String url) throws IOException {
    return Jsoup.connect(url).timeout(5000).get();
  }
}
