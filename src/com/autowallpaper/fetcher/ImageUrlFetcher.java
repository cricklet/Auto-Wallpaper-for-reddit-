package com.autowallpaper.fetcher;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.http.entity.BufferedHttpEntity;
import org.json.JSONException;

import com.autowallpaper.R;
import android.graphics.Bitmap;
import android.util.Log;

public interface ImageUrlFetcher {
	
	public ArrayList<String> fetchImages();
	
}
