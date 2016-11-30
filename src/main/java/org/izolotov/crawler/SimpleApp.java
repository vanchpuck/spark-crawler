package org.izolotov.crawler;

/* SimpleApp.java */
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.SparkConf;
import org.izolotov.crawler.Document.Status;
import org.izolotov.crawler.ParsedPage.ParseStatus;

import scala.Tuple2;

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
				})
				
//				mapPartitionsToPair(new PairFlatMapFunction<Iterator<Tuple2<String,List<WebPage>>>, String, List<ParsedPage>>() {
//
//					@Override
//					public Iterator<Tuple2<String, List<ParsedPage>>> call(Iterator<Tuple2<String, List<WebPage>>> t)
//							throws Exception {
//						TikaParser parser = new TikaParser();
//						t.
//						return null;
//					}
//				})
//				mapPartitions((Tuple2<String, List<WebPage>> tuple) -> {
//					TikaParser parser = new TikaParser();
//					List<ParsedPage> parsedPages = new ArrayList<>();
////					tuple._2.stream().map(page -> parser.parse(pare));
//					return Arrays.asList(ParsedPage.emptyPage("", ParseStatus.FAILURE)).iterator();
//				})
//				mapValues(fetched -> new TikaParser().parse(fetched))
				;
		
		parsed.foreach(pair -> pair._2.forEach(doc -> System.out.println(doc.getUrl()+" _ "+doc.getText().length()+" _ "+doc.getLinks().size())));
		
//		JavaPairRDD<String, Iterable<WebPage>> groupedByHost = urlPagePairs.groupByKey();
//		
//		long numAs = urls.filter((String s) -> s.contains("a")).count();
//
//		long numBs = urls.filter((String s) -> s.contains("b")).count();
//		
//		System.out.println("Lines with a: " + numAs + ", lines with b: "
//				+ numBs);
		sc.close();
		
//		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(socketTimeout)
	}
}
