package org.izolotov.crawler.parse.jsoup;

//import static org.hamcrest.collection.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

import com.google.gson.Gson;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.izolotov.crawler.WebPage;
import org.izolotov.crawler.fetch.FetchFlag;
import org.izolotov.crawler.parse.ParseFlag;
import org.izolotov.crawler.parse.TextDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import sun.util.locale.ParseStatus;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class TextDocumentTest {

    @Test
    public void parseTest() throws IOException {
        Path path = new File(getClass().getClassLoader().getResource("test_pages/simple_page.html").getFile()).toPath();
        String content = new String(Files.readAllBytes(path), Charset.forName("UTF-8"));

        WebPage page = newSuccessWebPage("http://localhost/simple_page.html", content);

        TextDocument doc = new TextDocument(new JsoupDocumentBuilder(page));
        assertThat(ParseFlag.SUCCESS.check(doc), is(Boolean.TRUE));
        assertThat(doc.getUrl().toString(), is("http://localhost/simple_page.html"));
        assertThat(doc.getText().orNull(), is("Test title. This is a paragraph with line breaks. Relative URL. Absolute URL."));
        assertThat(doc.getOutlinks(), containsInAnyOrder(
                "http://localhost/absolute_path.html",
                "http://localhost/relative_path.html",
                "http://localhost/image_link.html"
        ));
    }

    @Test
    public void parseMetaRedirectTest() throws IOException {
        Path path = new File(getClass().getClassLoader().getResource("test_pages/delayed_redirect.html").getFile()).toPath();
        String content = new String(Files.readAllBytes(path), Charset.forName("UTF-8"));

        WebPage page = newSuccessWebPage("http://localhost/delayed_redirect.html", content);

        TextDocument doc = new TextDocument(new JsoupDocumentBuilder(page));
        assertThat(doc.getText().orNull(), is("Redirecting in 3 seconds..."));
        assertThat(ParseFlag.META_REDIRECT.check(doc), is(Boolean.TRUE));
        assertThat(doc.getOutlinks(), empty());
    }

    private WebPage newSuccessWebPage(String url, String content) {
        WebPage page = WebPage.of(url);
        FetchFlag.SUCCESS.setStatus(page);
        page.setContentType(ContentType.TEXT_HTML.toString());
        page.setHttpStatusCode(HttpStatus.SC_OK);
        page.setContent(content);
        return page;
    }

}
