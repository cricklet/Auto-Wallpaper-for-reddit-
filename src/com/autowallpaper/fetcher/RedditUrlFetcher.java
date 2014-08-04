package com.autowallpaper.fetcher;

import java.util.ArrayList;

import org.apache.http.entity.BufferedHttpEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.autowallpaper.helper.Downloader;

import android.graphics.Bitmap;
import android.util.Log;

public class RedditUrlFetcher implements ImageUrlFetcher {

	public static final String REDDIT_URL = "http://www.reddit.com/r/";
	public static final String REDDIT_POSTFIX = ".json";

	private String subreddit;

	public RedditUrlFetcher(String subreddit) {
		this.subreddit = subreddit;
	}

	public ArrayList<String> fetchImages() {
		try {
			String url = REDDIT_URL + subreddit + REDDIT_POSTFIX;
			String dataString = Downloader.httpGET(url, false);

			JSONObject data = new JSONObject(dataString);
			JSONArray posts = data.getJSONObject("data").getJSONArray("children");

			ArrayList<String> urls = new ArrayList<String>();

			for (int i = 0; i < posts.length(); i ++) {
				try {
					urls.add(getUrl(posts.getJSONObject(i)));
				} catch (JSONException e1) {}
			}

			return urls;
		} catch (Exception e) {
			return new ArrayList<String>();
		}
	}

	private String getUrl(JSONObject o) throws JSONException {
		return o.getJSONObject("data").getString("url");
	}
}
