package org.izolotov.crawler;

import java.util.Arrays;
import java.util.List;

public class LocalRunner {

	public static void main(String[] args) {
		String url = "http://bigtop.apache.org/";
		List<WebPage> pages = new PageFetcher(Arrays.asList(url)).fetch();
		System.out.println("-----");
//		pages.forEach((WebPage page)->System.out.println(page.getUrl()+" "+page.getStatus()+" "+page.getContent()+"."));
	}
	
}
