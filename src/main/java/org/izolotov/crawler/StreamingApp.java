package org.izolotov.crawler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import kafka.serializer.StringDecoder;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.rdd.PairRDDFunctions;
import org.apache.spark.streaming.Duration;
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
		
//		inStream.foreachRDD(pairRDD -> pairRDD.mapValues(f));
		inStream.groupByKey().
				mapValues(hostUrls -> new PageFetcher(hostUrls).fetch()).
				mapValues(fetchedPages -> {
					TikaParser parser = new TikaParser();
					return fetchedPages.stream().map(page -> parser.parse(page)).collect(Collectors.toList());
				}).
				foreachRDD(pairRDD -> pairRDD.foreach(item -> System.out.println("key: "+item._1+". value: "+item._2)));
		;
		
		jsc.start();
		jsc.awaitTermination();
	}
	
}
