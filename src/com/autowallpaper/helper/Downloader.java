package com.autowallpaper.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Downloader {

	public static final String TAG = "Downloader";

	public static boolean isConnected(Context context) {
		if (context != null) {
			ConnectivityManager connectivity = (ConnectivityManager)
					context.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				NetworkInfo[] infos = connectivity.getAllNetworkInfo();
				if (infos != null)
					for (NetworkInfo info : infos) {
						if (info.isConnected())
							return true;
					}
			}
		}

		return false;
	}

	private static String convertStreamToString(InputStream inputStream)
			throws UnsupportedEncodingException, IOException {
		if (inputStream != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(
						inputStream, "UTF-8"), 1024);
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				inputStream.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}

	public static String addParameter(String url, String key, String value) {
		try {
			value = URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {}

		if (value == null)
			return url;
		if (key == null)
			return url;

		if (url.contains("?"))
			url += "&";
		else
			url += "?";

		url += key + "=" + value.replace("&=", "");

		return url;
	}

	private static URI getUriFromUrlString(String urlString) throws Exception {
		URL url = new URL(urlString);
		URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(),
				url.getPort(), url.getPath(), url.getQuery(), null);
		return uri;
	}

	public static void httpPOSTNonBlocking(final String urlString,
			final List<NameValuePair> params) {
		new Thread() {public void run() {
			httpPOST(urlString, params);
		}}.start();
	}

	public static String httpPOST(String urlString, List<NameValuePair> params) {
		Log.v(TAG, "http post: " + urlString + " " + params.toString().substring(0, 20));
		try {
			HttpClient client = new DefaultHttpClient();
			HttpPost request = new HttpPost();
			request.setURI(getUriFromUrlString(urlString));
			request.setEntity(new UrlEncodedFormEntity(params));

			HttpResponse response = client.execute(request);
			InputStream stream = response.getEntity().getContent();
			return convertStreamToString(stream);

		} catch (Exception e) {
			Log.e(TAG, "ERROR: " + urlString + "\n" + Log.getStackTraceString(e));
			return null;
		}
	}

	private static HashMap<String, String> getCache = new HashMap<String, String>();

	public static boolean isGETCached(String urlString) {
		return getCache.containsKey(urlString);
	}

	public static void httpGETNonBlocking(final String url, final boolean cache) {
		new Thread() {public void run() {
			httpGET(url, cache);
		}}.start();
	}

	public static String httpGET(String urlString, boolean cache) {
		Log.v(TAG, "http get: " + urlString);
		if (cache && getCache.containsKey(urlString))
			return getCache.get(urlString);

		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet();
			request.setURI(getUriFromUrlString(urlString));

			HttpResponse response = client.execute(request);
			InputStream stream = response.getEntity().getContent();
			String result = convertStreamToString(stream);

			if (cache)
				getCache.put(urlString, result);

			return result;

		} catch (Exception e) {
			Log.e(TAG, "ERROR: " + urlString + "\n" + Log.getStackTraceString(e));
			return "";
		}
	}

	public static BufferedHttpEntity getEntityFromUrl(String url) {
		if (url == null)
			return null;

		HttpGet httpRequest = new HttpGet(URI.create(url));
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response;
		try {
			response = (HttpResponse) httpclient.execute(httpRequest);
		} catch (ClientProtocolException e1) {
			Log.e(TAG, "ERROR: " + url + "\n" + Log.getStackTraceString(e1));
			return null;
		} catch (IOException e1) {
			Log.e(TAG, "ERROR: " + url + "\n" + Log.getStackTraceString(e1));
			return null;
		}
		HttpEntity entity = response.getEntity();
		try {
			return new BufferedHttpEntity(entity);
		} catch (IOException e1) {
			Log.e(TAG, "ERROR: " + url + "\n" + Log.getStackTraceString(e1));
			return null;
		}
	}

	public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
		if (bm == null) return null;
		if (newWidth < 100 || newHeight < 100) return bm;

		int width = bm.getWidth();
		int height = bm.getHeight();

		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		float scale = Math.max(scaleWidth, scaleHeight);

		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);

		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
		return resizedBitmap;
	}

	private static int determineSampleSize(BitmapFactory.Options options,
			int minWidth, int minHeight) throws IOException {
		int w = options.outWidth;
		int h = options.outHeight;

		// if the images are too small
		if (w < minWidth || h < minHeight)
			return -1;

		// if minWidth is weird
		if (minWidth <= 0 || minHeight <= 0)
			return -1;
		
		// if the proportions are weird
		if (w*2 < h || h*2 < w)
			return -1;

		Log.e("ASDF", "   converting " + w + ", " + h + " => "
				+ minWidth + ", " + minHeight);

		int sampleSize = 1;
		double scale = 1;
		while (w * scale * 0.5 >= minWidth || h * scale * 0.5 >= minHeight) {
			sampleSize *= 2;
			scale *= 0.5;
		}

		Log.e("ASDF", "   " + scale + " x" + sampleSize);

		return sampleSize;
	}

	private static int determineSampleSize(BufferedHttpEntity entity,
			int minWidth, int minHeight) throws IOException {
		// determine sampling size
		BitmapFactory.Options options = new BitmapFactory.Options();

		// if repeateable, find sampling rate first
		if (entity.isRepeatable()) {
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(entity.getContent(), null, options);

			return determineSampleSize(options, minWidth, minHeight);
		} else {
			return 1;
		}
	}

	public static Bitmap getBitmapFromEntity(BufferedHttpEntity entity, 
			int minWidth, int minHeight) {
		if (entity == null)
			return null;

		// convert to bitmap
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inJustDecodeBounds = false;
		try {
			options.inSampleSize = determineSampleSize(entity, minWidth, minHeight);
			if (options.inSampleSize == -1) return null;
		} catch (IOException e1) {
			Log.e(TAG, "ERROR: " + "\n" + Log.getStackTraceString(e1));
			return null;
		}
		try {
			return BitmapFactory.decodeStream(entity.getContent(), null, options);
		} catch (IOException e1) {
			Log.e(TAG, "ERROR: " + "\n" + Log.getStackTraceString(e1));
			return null;
		}
	}

}
