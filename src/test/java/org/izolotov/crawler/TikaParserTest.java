package org.izolotov.crawler;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.junit.Ignore;
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
	
	@Ignore
	@Test
	public void test() throws Exception {
//		String url = "http://hbase.apache.org/";
		String url = "http://redirekt.info/stranica-s-ustanovlennym-redirectom.html";
		
		PageFetcher fetcher = new PageFetcher(Arrays.asList(url));
//		WebPage page = fetcher.fetch().get(0);
//		System.out.println(page.getContentType());
//		System.out.println(page.getContent());
//		WebPage page = fetcher.fetch().get(0);
		
		System.out.println("------------------------");
		for (WebPage page : fetcher.fetch()) {
			System.out.println("Url: "+page.getUrl());
			System.out.println("Status: "+page.getHttpStatusCode());
			System.out.println("------------------------");
		}
		
		
//		System.out.println("Status: "+page.getHttpStatusCode());
//		
//		ParsedPage doc = parser.parse(page);
//		System.out.println("Url: "+doc.getUrl());
//		System.out.println("Text: "+doc.getText());
//		System.out.println("_");
//		doc.getLinks().forEach(link -> System.out.println(link));
//		System.out.println("_");
		
		

	}

}
