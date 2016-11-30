package org.izolotov.crawler;

import java.util.Optional;

public interface Document {

	public static interface Status {
		public int getCode();
	}
	
	public static interface Metadata {
		
		public void set(String key, String value);
		
		public void set(String key, Iterable<String> values);
		
		public Optional<String> getFirst(String key);
		
		public Iterable<String> get(String key);
	}
	
	public String getUrl();
	
	public String getText();
	
	public Status getStatus();
	
	public Metadata getMetadata();	
}
