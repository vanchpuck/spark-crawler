//package org.izolotov.crawler;
//
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//
//import kafka.serializer.StringDecoder;
//
//import org.izolotov.crawler.HbaseClient;
//import org.apache.hadoop.conf.Configuration;
//import org.apache.hadoop.hbase.HBaseConfiguration;
//import org.apache.hadoop.hbase.TableName;
//import org.apache.hadoop.hbase.client.Connection;
//import org.apache.hadoop.hbase.client.ConnectionFactory;
//import org.apache.hadoop.hbase.client.Table;
//import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
//import org.apache.hadoop.mapred.JobConf;
//import org.apache.hadoop.mapreduce.Job;
//import org.apache.spark.SparkConf;
//import org.apache.spark.SparkContext;
//import org.apache.spark.api.java.JavaPairRDD;
//import org.apache.spark.api.java.JavaSparkContext;
//import org.apache.spark.api.java.function.PairFlatMapFunction;
//import org.apache.spark.api.java.function.VoidFunction;
//import org.apache.spark.broadcast.Broadcast;
//import org.apache.spark.streaming.Duration;
//import org.apache.spark.streaming.api.java.JavaPairDStream;
//import org.apache.spark.streaming.api.java.JavaPairInputDStream;
//import org.apache.spark.streaming.api.java.JavaStreamingContext;
//import org.apache.spark.streaming.kafka.KafkaUtils;
//
//import org.izolotov.crawler.fetch.PageFetcher;
//import scala.reflect.ClassTag;
//
//public class StreamingApp {
//
//	public static void main(String[] args) throws Exception {
//		SparkConf sparkConf = new SparkConf().setAppName("SparkStreamingCrawler");
//		JavaStreamingContext jsc = new JavaStreamingContext(sparkConf, new Duration(Integer.parseInt(args[1])));
//        jsc.sparkContext().getConf()
//		Set<String> topics = new HashSet<>();
//		topics.add(args[0]);
//
//		Map<String, String> kafkaParams = new HashMap<>();
//		kafkaParams.put("metadata.broker.list", "localhost:9092");
//		kafkaParams.put("group.id", "test1");
//		kafkaParams.put("zookeeper.connect", "localhost:2181");
//
//		Configuration conf = HBaseConfiguration.create();
//		Job job = Job.getInstance(conf);
//		job.getConfiguration().set(TableOutputFormat.OUTPUT_TABLE, "mytable");
//		job.setOutputFormatClass(TableOutputFormat.class);
//
//		JavaPairInputDStream<String, String> inStream =
//				KafkaUtils.createDirectStream(
//						jsc, String.class, String.class, StringDecoder.class, StringDecoder.class, kafkaParams, topics);
//
//		JavaPairDStream<String, Iterable<String>> groupedByHost = inStream.groupByKey();
//		JavaPairDStream<String, Iterable<WebPage>> fetchedByHost = groupedByHost.
//				mapValues(hostUrls -> new PageFetcher.Builder().
//                        setMinDelay(jsc.sparkContext().getConf().getLong("fetch.delay.min", 5000L)).
//                        setConnectionTimeLimit(jsc.sparkContext().getConf().getLong("fetch.connection.time.limit", 5000L)).
//                        build().fetch(hostUrls));
////		fetchedByHost.mapPartitionsToPair(new PairFlatMapFunction<Iterator<Tuple2<String,Iterable<WebPage>>>, K2, V2>() {
////			public Iterable<scala.Tuple2<K2,V2>> call(Iterator<Tuple2<String,java.lang.Iterable<WebPage>>> t) throws Exception {
////
////			};
////		})
////		fetchedByHost.foreachRDD(new VoidFunction<JavaPairRDD<String, Iterable<WebPage>>>() {
////
////			@Override
////			public void call(JavaPairRDD<String, Iterable<WebPage>> pairRDD) throws Exception {
////				pairRDD.ma
////			}
////		});
//
//		//Store to HBase
////		fetchedByHost.foreachRDD(new VoidFunction<JavaPairRDD<String, Iterable<WebPage>>>() {
////			@Override
////			public void call(JavaPairRDD<String, Iterable<WebPage>> pairRDD) throws Exception {
////				pairRDD.foreach(tuple -> {
////					Configuration conf = HBaseConfiguration.create();
////					HbaseClient client = new HbaseClient(conf);
////					tuple._2.forEach(page -> {
////						System.out.println(page.getUrl()+" "+page.getHttpStatusCode());
////						try {
////							client.write("spark_test", "value");
////						} catch (Exception e) {
////							throw new RuntimeException(e);
////						}
////					});
////					client.close();
////				});
////			}
////
////		});
//
//
//		jsc.start();
//		jsc.awaitTermination();
//	}
//
//}
