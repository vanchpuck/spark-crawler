package org.izolotov.crawler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.EncodingDetector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.LinkContentHandler;
import org.apache.tika.sax.TeeContentHandler;
import org.izolotov.crawler.ParsedPage.ParseMetadata;
import org.izolotov.crawler.ParsedPage.ParseStatus;
import org.xml.sax.SAXException;

import scala.reflect.internal.Trees.TreeContextApiImpl;

public class TikaParser {

	// TODO choose parser based on mime type
//	private HtmlParser parser;
	private AutoDetectParser parser;
	
	public TikaParser() {
//		try {
//			mimeTypes = MimeTypesFactory.create("tika-mimetypes.xml");
//		} catch (MimeTypeException | IOException e) {
//			mimeTypes = MimeTypesFactory.create();
//		}
//		parser = new HtmlParser();
		parser = new AutoDetectParser(new DefaultDetector());
	}
	
	public ParsedPage parse(WebPage page)  {
		BodyContentHandler bodyHandler = new BodyContentHandler();
		LinkContentHandler linkHandler = new LinkContentHandler();
		TeeContentHandler handler = new TeeContentHandler(bodyHandler, linkHandler);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(page.getContent().getBytes());
		Metadata metadata = new Metadata();
		System.out.println("Content type: "+page.getContentType());
		EncodingDetector ed;
		metadata.add(TikaCoreProperties.CONTENT_TYPE_HINT, page.getContentType());
		metadata.add(Metadata.CONTENT_ENCODING, "cp1251");
		ParseContext context = new ParseContext();
//		linkHandler.getLinks().get(0).is
		try {
			parser.parse(inputStream, handler, metadata, context);
			System.out.println("handler: "+bodyHandler.toString());
			List<String> links = linkHandler.getLinks().stream().
//					filter(link -> (link.isAnchor() || link.isScript() || link.isIframe() || link.isImage())).
					filter(link -> !link.isLink()).
					map(link -> link.getUri()).collect(Collectors.toList());
			ParsedPage.Builder builder = new ParsedPage.Builder(page.getUrl(), ParseStatus.SUCCESS).
					setText(bodyHandler.toString()).
					addLink(links);
			return builder.build();
		} catch (IOException | SAXException | TikaException e) {
			return ParsedPage.emptyPage(page.getUrl(), ParseStatus.FAILURE);
		}
	}
	
//	public Iterable<Document> parse(Iterable<WebPage> pages) {
//		List<Document> docs = new ArrayList<>();
//		for(WebPage page : pages) {
//			try {
//				docs.add(parse(page));
//			} catch (Exception e) {
//				e.printStackTrace();
//				docs.add(new ParsedPage(page.getUrl(), "Error!"));
//			}
//		}		
//		return docs;
//	}
	
}
