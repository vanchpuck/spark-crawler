package org.izolotov.crawler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;

public class ParsedPage implements Document {

	public enum ParseStatus implements Document.Status {
		SUCCESS(0),
		FAILURE(1);
		private int code;
		private ParseStatus(int code) {
			this.code = code;
		}
		@Override
		public int getCode() {
			return code;
		}
	}
	
	public static class ParseMetadata implements Document.Metadata {
		private final Map<String, List<String>> data;
		
		public ParseMetadata() {
			data = new HashMap<>();
		}
		@Override
		public void set(String key, String value) {
			data.put(key, Arrays.asList(value));
		}
		@Override
		public void set(String key, Iterable<String> values) {
			List<String> listValues = new ArrayList<>();
			values.forEach(val -> listValues.add(val));
			data.put(key, listValues);
		}
		@Override
		public Optional<String> getFirst(String key) {
			List<String> values = data.get(key);
			return CollectionUtils.isEmpty(values) ? Optional.empty() : Optional.ofNullable(values.get(0));
		}
		@Override
		public Iterable<String> get(String key) {
			return data.getOrDefault(key, Collections.emptyList());
		}
	}
	
	public static class Builder {
		private String bUrl;
		private String bText;
		private Status bStatus;
		private List<String> bLinks;
		
		public Builder(String url, Status status) {
			bUrl = url;
			bStatus = status;
			bText = "";
			bLinks = new ArrayList<>();
		}
		
		public Builder addLinks(String ... urls) {
			for(String link : urls) {
				bLinks.add(link);
			}
			return this;
		}
		
		public Builder addLink(Collection<String> urls) {
			bLinks.addAll(urls);
			return this;
		}
		
		public Builder setText(String text) {
			bText = text;
			return this;
		}
		
		public ParsedPage build() {
			return new ParsedPage(bUrl, bStatus, bText, new ArrayList<>(bLinks));
		}
	}
	
	
	private String url;
	private String text;
	private Status status;
	private Metadata metadata = new ParseMetadata();
	private List<String> links;
		
	protected ParsedPage(String url, Status status, String text, List<String> links) {
		this.url = url;
		this.text = text;
		this.status = status;
		this.links = links;
	}
	
	public static ParsedPage emptyPage(String url, Status status) {
		return new ParsedPage(url, status, "", Collections.emptyList());
	}
	
	@Override
	public String getText() {
		return text;
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public Status getStatus() {
		return status;
	}
	
	@Override
	public Metadata getMetadata() {
		return metadata;
	}
	
	public List<String> getLinks() {
		return links;
	}

}
