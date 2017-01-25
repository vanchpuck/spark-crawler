package org.izolotov.crawler;

/* SimpleApp.java */
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.spark.api.java.*;
import org.apache.spark.SparkConf;

public class SimpleApp {
	public static void main(String[] args) {
		String logFile = args[0]; 
		SparkConf conf = new SparkConf().setAppName("Simple Application");
		JavaSparkContext sc = new JavaSparkContext(conf);
		JavaRDD<String> urls = sc.textFile(logFile);
		@SuppressWarnings("resource")
		JavaPairRDD<String, List<ParsedPage>> parsed = 
//				urls.mapToPair((String url) -> new Tuple2<String, WebPage>(new URL(url).getHost(), WebPage.blankPage(url))).
				urls.groupBy((String url) -> new URL(url).getHost()).
				mapValues(hostUrls -> new PageFetcher(hostUrls).fetch()).
				mapValues(fetchedPages -> {
					TikaParser parser = new TikaParser();
					return fetchedPages.stream().map(page -> parser.parse(page)).collect(Collectors.toList());
				});
		
		parsed.foreach(pair -> pair._2.forEach(doc -> System.out.println(doc.getUrl()+" _ "+doc.getText().length()+" _ "+doc.getLinks().size())));

		sc.close();		
	}
}
