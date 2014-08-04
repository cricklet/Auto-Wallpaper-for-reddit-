package com.autowallpaper.fetcher;

import java.util.ArrayList;

import org.apache.http.entity.BufferedHttpEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.autowallpaper.helper.Downloader;

import android.graphics.Bitmap;
import android.util.Log;

public class TumblrFetcher implements ImageUrlFetcher {

	public static final String TUMBLR_URL = "http://api.tumblr.com/v2/blog/";
	public static final String TUMBLR_POSTFIX = "/posts?api_key=RSobjzlroTGuEv50SKI00fFDJwsDoidJuhonHN9pQ3NMNq4ofM";

	private String blog;

	public TumblrFetcher(String blog) {
		this.blog = blog;
	}

	public ArrayList<String> fetchImages() {
		try {
			String url = TUMBLR_URL + blog + TUMBLR_POSTFIX;
			String dataString = Downloader.httpGET(url, false);
			JSONObject data = new JSONObject(dataString);
			JSONArray posts = data.getJSONObject("response").getJSONArray("posts");
			
			ArrayList<String> urls = new ArrayList<String>();
			
			for (int i = 0; i < posts.length(); i ++) {
				try {
					urls.addAll(getPhotos(posts.getJSONObject(i)));
				} catch (JSONException e1) {}
			}

			return urls;
		} catch (Exception e) {
			return new ArrayList<String>();
		}
	}

	private ArrayList<String> getPhotos(JSONObject post) throws JSONException {
		if (post.has("photos") == false) return new ArrayList<String>();
		JSONArray photosArray = post.getJSONArray("photos");

		if (photosArray.length() == 0) return new ArrayList<String>();

		ArrayList<String> urls = new ArrayList<String>();

		for (int i = 0; i < photosArray.length(); i ++) {
			JSONObject photoData = photosArray.getJSONObject(i);
			JSONArray photoOptions = photoData.getJSONArray("alt_sizes");

			String url = photoOptions.getJSONObject(0).getString("url");
			urls.add(url);
		}
		return urls;
	}

}
