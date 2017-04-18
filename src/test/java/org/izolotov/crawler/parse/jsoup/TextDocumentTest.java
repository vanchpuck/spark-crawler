package org.izolotov.crawler.parse.jsoup;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.izolotov.crawler.WebPage;
import org.izolotov.crawler.fetch.FetchFlag;
import org.junit.Test;

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

        WebPage page = WebPage.of("http://localhost/simple_page.html");
        FetchFlag.SUCCESS.setStatus(page);
        page.setContentType(ContentType.TEXT_HTML.toString());
        page.setHttpStatusCode(HttpStatus.SC_OK);
        page.setContent(content);

        TextDocument doc = new TextDocument(page);
        assertThat(doc.getText().orNull(), is("Test title. This is a paragraph with line breaks. Relative URL. Absolute URL."));
        assertThat(doc.getOutlinks(), containsInAnyOrder(
                "http://localhost/absolute_path.html",
                "http://localhost/relative_path.html",
                "http://localhost/image_link.html"
        ));
    }

}
