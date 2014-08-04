package com.autowallpaper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;

import org.apache.http.entity.BufferedHttpEntity;

import com.autowallpaper.fetcher.ImageUrlFetcher;
import com.autowallpaper.fetcher.RedditUrlFetcher;
import com.autowallpaper.helper.Downloader;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.service.wallpaper.WallpaperService;
import android.util.Log;

public class BitmapLoader extends BroadcastReceiver {

	private static boolean sUpdated;

	/**
	 * save which urls have been recently tried
	 */
	private static HashSet<String> seenUrls = new HashSet<String>();

	public static boolean isUpdated() {
		boolean result = sUpdated;
		sUpdated = false;
		return result;
	}

	public static Bitmap getCachedBitmap(Context context, String name) {
		Log.e("ASDF", "get cached bitmap: " + name);
		try {
			FileInputStream in = context.openFileInput(name);
			Bitmap bmp = BitmapFactory.decodeStream(in);
			if (bmp != null && bmp.getWidth() > 10 && bmp.getHeight() > 10)
				return bmp;
		} catch (Exception e) {Log.e("ASDF", "" + e);}

		return null;
	}

	private static void cacheBitmap(Context context, Bitmap bmp, String name) {
		Log.e("ASDF", "saving cached bitmap: " + bmp + ", " + name);
		Log.e("ASDF", "" + context.getFilesDir());
		try {
			FileOutputStream out = context.openFileOutput(name, Context.MODE_PRIVATE);
			bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.close();
		} catch (Exception e) {Log.e("ASDF", "" + e);}
	}

	/**
	 * get a bitmap using urls found through the fetcher
	 */
	private static Bitmap downloadBitmap(ImageUrlFetcher fetcher) {
		Bitmap bmp = downloadBitmapHelper(fetcher.fetchImages(),
				Settings.DOWNLOAD_WIDTH, Settings.DOWNLOAD_HEIGHT);
		return bmp;
	}

	/**
	 * loop through urls and grab the first bitmap that works
	 * skip any urls that have been seen already
	 */
	private static Bitmap downloadBitmapHelper(ArrayList<String> urls, int width, int height) {
		for (String url : urls) {
			if (seenUrls.contains(url))
				continue;
			seenUrls.add(url);

			Log.e("ASDF", "   download bitmap: " + url);
			Bitmap bitmap = downloadBitmapHelper(url, width, height);
			Log.e("ASDF", "   downloaded: " + bitmap);
			if (bitmap != null)
				Log.e("ASDF", "   dim: " + bitmap.getWidth() + ", " + bitmap.getHeight());


			if (bitmap != null) {
				return bitmap;
			}
		}

		return null;
	}

	/**
	 * get the bitmap from a url
	 */
	private static Bitmap downloadBitmapHelper(String url, int width, int height) {
		if (url.contains("imgur") && !url.contains(".jpg")) {
			Bitmap b = downloadBitmapHelper(url + ".jpg", width, height);
			if (b != null) return b;
		}

		BufferedHttpEntity entity = Downloader.getEntityFromUrl(url);
		if (entity == null) return null;

		Log.e("ASDF", "   downloading for dim: " + width + ", " + height);
		Bitmap bitmap = Downloader.getBitmapFromEntity(entity, width, height);

		if (bitmap != null) {
			Log.e("ASDF", "   downloaded dim: " + bitmap.getWidth() + ", " + bitmap.getHeight());
			
			if (bitmap.getWidth() < width && bitmap.getHeight() < height) return null;
			return bitmap;
		}

		return null;
	}

	/**
	 * fetch and save bitmap
	 */
	public static void download(Context context, String name) {
		if (!Downloader.isConnected(context)) return;

		Log.e("ASDF", "download bitmap: " + name);

		RedditUrlFetcher fetcher = new RedditUrlFetcher(name);
		Bitmap bmp = downloadBitmap(fetcher);
		if (bmp == null) return;

		sUpdated = true;
		cacheBitmap(context, bmp, name);

		Log.e("ASDF", "downloaded: " + bmp);
		bmp.recycle();
	}


	@Override
	public void onReceive(final Context context, Intent intent) {
		Log.e("ALARM", "onReceive()");
		
		new Thread() {public void run() {
			download(context, Settings.SUBREDDIT);
		}}.start();
		
		// if this wallpaper is no longer set, remove the alarm
		if (isWallpaperSet(context) == false) {
			Log.e("ALARM", "onReceive() no longer set, remove alarm");
			removeAlarm(context);
		}
	}
	
	private static boolean isWallpaperSet(Context context) {
		// check whether this wallpaper is running
		WallpaperManager wpm = (WallpaperManager)
				context.getSystemService(WallpaperService.WALLPAPER_SERVICE);
		WallpaperInfo info = wpm.getWallpaperInfo();
		
		ComponentName thisComponent = new ComponentName(context, AutoWallpaper.class);
		if (info != null && info.getComponent().equals(thisComponent)) {
			return true;
		} else {
			return false;
		}
	}
	
	private static PendingIntent getPendingIntent(Context context) {
		Intent intent = new Intent(context, BitmapLoader.class);
		PendingIntent pending = PendingIntent.getBroadcast(context,
				0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		return pending;
	}

	public static void removeAlarm(Context context) {
		Log.e("ALARM", "removeAlarm() remove alarm");
		// remove the alarm
		AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
		am.cancel(getPendingIntent(context));
	}

	public static void setupAlarm(Context context) {
		// remove the old alarm
		removeAlarm(context);
		
		// never set alarm if the wallpaper isn't set
		if (isWallpaperSet(context) == false) {
			Log.e("ALARM", "setupAlarm() wallpaper isn't set, skip");
			return;
		}

		Log.e("ALARM", "setupAlarm() set repeating alarm");
		// set a new repeating alarm for every fifteen minutes
		PendingIntent pending = getPendingIntent(context);
		AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
		
		Calendar nowCalendar = Calendar.getInstance();
		nowCalendar.setTimeInMillis(System.currentTimeMillis());
		long now = nowCalendar.getTimeInMillis();
		long interval = AlarmManager.INTERVAL_DAY / 12l;
		//long interval = 30000;
		am.setRepeating(AlarmManager.RTC, now + interval, interval, pending);
	}

}
