package com.autowallpaper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.autowallpaper.fetcher.RedditUrlFetcher;
import com.autowallpaper.helper.Downloader;
import com.autowallpaper.helper.StringHelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

public class AutoWallpaper extends WallpaperService {

	public static boolean sForce = false;

	private Bitmap resizedBitmap;

	@Override
	public Engine onCreateEngine() {
		return new WallpaperEngine();
	}

	public class WallpaperEngine extends Engine implements
	SharedPreferences.OnSharedPreferenceChangeListener {

		private int desiredWidth;
		private int desiredHeight;

		private Handler handler = new Handler();
		private Paint paint = new Paint();

		private int xPixelOff;

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
			BitmapLoader.setupAlarm(AutoWallpaper.this);

			boolean loaded = load(true);
			if (loaded) return;
			reload();
		}

		/**
		 * Load a bmp. If a new one was loaded, return true;
		 */
		private boolean load(boolean force) {
			boolean loaded = false;

			String name = Settings.SUBREDDIT;
			Bitmap bmp = null;

			// if bmp has been updated or we're being forced to, grab new bitmap
			if (BitmapLoader.isUpdated() || force) {
				bmp = BitmapLoader.getCachedBitmap(AutoWallpaper.this, name);

				// if we sucessfully loaded a bmp, return true
				if (bmp != null) loaded = true;

				// if we couldn't get a new bmp and nothing is being displayed, fallback
				if (bmp == null && resizedBitmap == null) {
					bmp = BitmapFactory.decodeResource(getResources(), R.drawable.background);
				}

				if (bmp != null) {
					resizedBitmap = Downloader.getResizedBitmap(bmp, desiredWidth, desiredHeight);
				}
			}

			return loaded;
		}

		/**
		 * Force download of bmp, then load it.
		 */
		private void reload() {
			final String name = Settings.SUBREDDIT;

			String text = "Downloading background for "
					+ StringHelper.formatSubredditList(Settings.SUBREDDITS);
			Toast.makeText(AutoWallpaper.this, text, Toast.LENGTH_LONG).show();

			new Thread() {public void run() {
				BitmapLoader.download(AutoWallpaper.this, name);

				handler.post(new Runnable() {public void run() {
					load(true);
					draw();
				}});
			}}.start();
		}

		public void draw() {
			if (sForce) {
				sForce = false;
				reload();
			}

			SurfaceHolder holder = getSurfaceHolder();
			if (holder == null) return;

			Canvas canvas = null;
			try {
				canvas = holder.lockCanvas();

				Bitmap bmp = resizedBitmap;
				if (bmp == null) return;

				int dx = (desiredWidth- bmp.getWidth()) / 2;
				int dy = (desiredHeight - bmp.getHeight()) / 2;
				canvas.drawBitmap(bmp, xPixelOff + dx, dy, paint);

			} catch (IllegalArgumentException e) {
			} catch (NullPointerException e) {
			} finally {
				if (canvas != null) 
					holder.unlockCanvasAndPost(canvas);
			}
		}

		@Override
		public void onOffsetsChanged(float xOff, float yOff,
				float xOffStep, float yOffStep, int xPixelOff, int yPixelOff) {
			super.onOffsetsChanged(xOff, yOff, xOffStep, yOffStep, xPixelOff, yPixelOff);

			this.xPixelOff = xPixelOff;

			handler.post(new Runnable() {public void run() {
				draw();
			}});
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format,int width, int height) {
			super.onSurfaceChanged(holder, format, width, height);

			desiredWidth = getDesiredMinimumWidth();
			desiredHeight = getDesiredMinimumHeight();

			handler.post(new Runnable() {public void run() {
				load(true);
				draw();
			}});
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			super.onVisibilityChanged(visible);

			if (visible == false) return;
			load(false);
			draw();
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			handler.post(new Runnable() {public void run() {
				load(false);
				draw();
			}});
		}
	}

}
