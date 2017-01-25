package org.izolotov.crawler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import kafka.serializer.StringDecoder;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;

public class StreamingApp {

	public static void main(String[] args) throws InterruptedException {
		SparkConf sparkConf = new SparkConf().setAppName("SparkStreamingCrawler");
		JavaStreamingContext jsc = new JavaStreamingContext(sparkConf, new Duration(Integer.parseInt(args[1])));
		
		Set<String> topics = new HashSet<>();
		topics.add(args[0]);
		
		Map<String, String> kafkaParams = new HashMap<>();
		kafkaParams.put("metadata.broker.list", "localhost:9092");
		kafkaParams.put("group.id", "test1");
		kafkaParams.put("zookeeper.connect", "localhost:2181");
		
		JavaPairInputDStream<String, String> inStream = 
				KafkaUtils.createDirectStream(
						jsc, String.class, String.class, StringDecoder.class, StringDecoder.class, kafkaParams, topics);
		
		JavaPairDStream<String, Iterable<String>> groupedByHost = inStream.groupByKey();
		JavaPairDStream<String, Iterable<WebPage>> fetchedByHost = groupedByHost.
				mapValues(hostUrls -> new PageFetcher(hostUrls).fetch());
		fetchedByHost.foreachRDD(new VoidFunction<JavaPairRDD<String, Iterable<WebPage>>>() {			
			@Override
			public void call(JavaPairRDD<String, Iterable<WebPage>> pairRDD) throws Exception {
				pairRDD.foreach(tuple -> tuple._2.forEach(
						page -> System.out.println(page.getUrl()+" "+page.getHttpStatusCode())));
			}

		});
		
		jsc.start();
		jsc.awaitTermination();
	}	
	
}
