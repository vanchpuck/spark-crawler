package org.izolotov.crawler;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.junit.Test;

import com.google.common.base.Optional;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;

public class TikaParserTest {

	TikaParser parser = new TikaParser();
	
	@Test
	public void test() throws Exception {
//		String url = "http://hbase.apache.org/";
		String url = "http://redirekt.info/stranica-s-ustanovlennym-redirectom.html";
		
		PageFetcher fetcher = new PageFetcher(Arrays.asList(url));
//		WebPage page = fetcher.fetch().get(0);
//		System.out.println(page.getContentType());
//		System.out.println(page.getContent());
		WebPage page = fetcher.fetch().get(0);
		System.out.println("Status: "+page.getHttpStatusCode());
		
		ParsedPage doc = parser.parse(page);
		System.out.println("Url: "+doc.getUrl());
		System.out.println("Text: "+doc.getText());
		System.out.println("_");
		doc.getLinks().forEach(link -> System.out.println(link));
		System.out.println("_");
		
//		OptimaizeLangDetector detector = new OptimaizeLangDetector();
//		detector.loadModels();
//		System.out.println("Models loaded");
//        detector.addText(doc.getText());
//        System.out.println("Tika lang: "+detector.detect().getLanguage());
//        System.out.println("_");
//        
//      List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();
//
//    //build language detector:
//    LanguageDetector languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
//            .withProfiles(languageProfiles)
//            .shortTextAlgorithm(30)
//            .build();
//
//    //create a text object factory
//    TextObjectFactory textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();
//
//    //query:
//    TextObject textObject = textObjectFactory.forText(doc.getText());
//    Optional<LdLocale> lang = languageDetector.detect(textObject);
//
//    System.out.println("Opt: "+lang.get().getLanguage());
//    if(lang.orNull() != null) {
//    	
//    }
//    System.out.println("_");
		
//		String content = "<!DOCTYPE html><html><meta http-equiv=\"Content-Type\" content=\"text/html;charset=ISO-8859-1\"><body><h1>My First Heading</h1><p>My first paragraph.</p></body></html>";
//		WebPage page= new WebPage("url", content, "text/html", 200);
//		List<WebPage> list = new ArrayList<WebPage>();
//		list.add(page);
//		
//		parser.parse(list).forEach(p -> System.out.println(p.getText()));
		
//		System.out.println(parser.parse(page).getText());
//		System.out.println(parser.parse(list).getText());
	}

}
