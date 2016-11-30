//package org.izolotov.crawler;
//
//import static org.junit.Assert.*;
//
//import java.util.ArrayList;
//import java.util.LinkedList;
//import java.util.Queue;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.ScheduledFuture;
//import java.util.concurrent.TimeUnit;
//
//import org.junit.Test;
//
//public class PageFetcherTest {
//
//	private String[] messages = new String[] {"ping1", "ping2", "ping3", "ping4", "ping5", "ping6"};
//	
//	private Queue<String> queue = new LinkedList<>();
//	{
//		queue.add("ping1");
//		queue.add("ping2");
//		queue.add("ping3");
//		queue.add("ping4");
//		queue.add("ping5");
//		queue.add("ping6");
//		queue.add("ping7");
//		queue.add("ping8");
//	}
//	
//	public class Action implements Runnable {
//
//		private String msg;
//		
//		public Action(String msg) {
//			this.msg = msg;
//		}
//		
//		@Override
//		public void run() {			
//			System.out.println("!");
////			System.out.println(queue.remove());
//			try {
//				Thread.sleep(5000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
//	
//	@Test
//	public void test() throws InterruptedException, ExecutionException {
////		PageFetcher pf = new PageFetcher();
////		pf.test();
//		ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);
//		executor.scheduleAtFixedRate(() -> System.out.println("ping"), 0L, 1000L, TimeUnit.MILLISECONDS);
////		executor.scheduleWithFixedDelay(command, 1, delay, unit)(new Action("Hi!"), 0, 2, TimeUnit.SECONDS);
//		
////		while(true) {
////			executor.scheduleAtFixedRate(new Action("Hi!"), 0, 2, TimeUnit.SECONDS);
////		}
//		
////		for(String msg : messages) {
////			executor.scheduleWithFixedDelay(new Action(msg), 1, 2, TimeUnit.SECONDS);
//////			ScheduledFuture<?> future = executor.schedule(new Action(msg), 1, TimeUnit.SECONDS);
//////			System.out.println(future.get());
////		}
//		
////		for(String msg : messages) {
////			executor.schedule(new Action(msg), 1, TimeUnit.SECONDS).get();
//////			ScheduledFuture<?> future = executor.schedule(new Action(msg), 1, TimeUnit.SECONDS);
//////			System.out.println(future.get());
////		}
//		
//		
////		executor.sch scheduleAtFixedRate(command, initialDelay, period, unit)
//	}
//
//}
