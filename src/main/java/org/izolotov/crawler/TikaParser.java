//package org.izolotov.crawler;
//
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import org.apache.tika.detect.DefaultDetector;
//import org.apache.tika.exception.TikaException;
//import org.apache.tika.metadata.Metadata;
//import org.apache.tika.metadata.TikaCoreProperties;
//import org.apache.tika.parser.AutoDetectParser;
//import org.apache.tika.parser.ParseContext;
//import org.apache.tika.sax.BodyContentHandler;
//import org.apache.tika.sax.LinkContentHandler;
//import org.apache.tika.sax.TeeContentHandler;
//import org.xml.sax.SAXException;
//
//public class TikaParser {
//
//	// TODO choose parser based on mime type
//	private AutoDetectParser parser;
//
//	public TikaParser() {
//		parser = new AutoDetectParser(new DefaultDetector());
//	}
//
//	public ParsedPage parse(WebPage page)  {
//		BodyContentHandler bodyHandler = new BodyContentHandler();
//		LinkContentHandler linkHandler = new LinkContentHandler();
//		TeeContentHandler handler = new TeeContentHandler(bodyHandler, linkHandler);
//		ByteArrayInputStream inputStream = new ByteArrayInputStream(page.getContent().getBytes());
//		Metadata metadata = new Metadata();
//		System.out.println("Content type: "+page.getContentType());
//		metadata.add(TikaCoreProperties.CONTENT_TYPE_HINT, page.getContentType());
//		ParseContext context = new ParseContext();
//		try {
//			parser.parse(inputStream, handler, metadata, context);
//			System.out.println("handler: "+bodyHandler.toString());
//			List<String> links = linkHandler.getLinks().stream().
//					filter(link -> !link.isLink()).
//					map(link -> link.getUri()).collect(Collectors.toList());
//			ParsedPage.Builder builder = new ParsedPage.Builder(page.getUrlString(), ParseStatus.SUCCESS).
//					setText(bodyHandler.toString()).
//					addLink(links);
//			return builder.build();
//		} catch (IOException | SAXException | TikaException e) {
//			return ParsedPage.emptyPage(page.getUrlString(), ParseStatus.FAILURE);
//		}
//	}
//
//}
