package com.autowallpaper;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.autowallpaper.custom.SubredditPreference;
import com.autowallpaper.helper.StringHelper;
import com.google.analytics.tracking.android.EasyTracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	public static void setupSettings(Context c) {
		PreferenceManager.setDefaultValues(c, R.xml.settings, false);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);

		setSubredditString(sharedPref);
		
		DOWNLOAD_WIDTH = sharedPref.getInt("download_width", -1);
		DOWNLOAD_HEIGHT = sharedPref.getInt("download_height", -1);

		if (DOWNLOAD_WIDTH == -1 || DOWNLOAD_HEIGHT == -1) {
			WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay(); 
			DOWNLOAD_WIDTH = display.getWidth()*2;
			DOWNLOAD_HEIGHT = display.getHeight();

			Editor e = sharedPref.edit();
			e.putInt("download_width", DOWNLOAD_WIDTH);
			e.putInt("download_height", DOWNLOAD_HEIGHT);
			e.commit();

			Log.e("ASDF", "setting up download size: " + DOWNLOAD_WIDTH + ", " + DOWNLOAD_HEIGHT);
		}
	}

	public static String SUBREDDIT;
	public static HashSet<String> SUBREDDITS = new HashSet<String>();

	public static int DOWNLOAD_WIDTH;
	public static int DOWNLOAD_HEIGHT;

	private static final String[] paidTitles = new String[] {
		"Elemental (nature, natural phenomena)",
		"Synthetic (man-made buildings and things)",
		"Organic (animals, plants, food)",
		"Aesthetic (art, design)",
		"Scholastic (books, history, etc)",
		"Girls", "NSFW"
	};

	private static final String[] allNames = new String[] {
		"custom_subreddits", "free_subreddits", "elemental_subreddits", "synthetic_subreddits",
		"organic_subreddits", "aesthetic_subreddits", "scholastic_subreddits", "girls_subreddits",
		"nsfw_subreddits"
	};
	
	private static final String[] paidNames = new String[] {
		"elemental_subreddits", "synthetic_subreddits", "organic_subreddits",
		"aesthetic_subreddits", "scholastic_subreddits", "girls_subreddits",
		"nsfw_subreddits"
	};
	
	private static final int[] paidEntries = new int[] {
		R.array.elemental, R.array.synthetic, R.array.organic, R.array.aesthetic,
		R.array.scholastic, R.array.girls, R.array.nsfw };

	private static void setSubredditString(SharedPreferences sharedPref) {
		SUBREDDITS.clear();
		
		Map<String, ?> map = sharedPref.getAll();
		for (String name : allNames) {
			if (map.containsKey(name) == false) continue;
			
			Object v = map.get(name);
			if (v instanceof Set) {
				Set<String> s = (Set<String>) v;
				for (String subreddit : s)
					SUBREDDITS.add(subreddit);
			}
			if (v instanceof String) {
				SUBREDDITS.add((String) v);
			}
		}

		SUBREDDITS.remove("");		
		SUBREDDITS.remove(null);
		
		SUBREDDIT = "";
		for (String subreddit : SUBREDDITS) {
			SUBREDDIT += subreddit + "+";
		}
		
		Log.e("ASDF", "Setting subreddit string to: " + SUBREDDIT);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		PreferenceManager.setDefaultValues(this, R.xml.settings, false);
		PreferenceScreen prefScreen = getPreferenceScreen();

		PreferenceCategory paidSubreddits = (PreferenceCategory) 
				prefScreen.findPreference("paid_subreddits");
		for (int i = 0; i < paidTitles.length; i ++) {
			SubredditPreference p = new SubredditPreference(this, 
					paidEntries[i], paidTitles[i], paidNames[i]);
			paidSubreddits.addPreference(p);
		}

		prefScreen.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		prefScreen.findPreference("about").setOnPreferenceClickListener(aboutListener);
		prefScreen.findPreference("reload").setOnPreferenceClickListener(reloadListener);

		setupSettings(this);
	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

	private Toast forceToast;
	private Toast selectedToast;
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPref, String key) {
		setupSettings(this);
		
		if (key.contains("subreddit")) {
			String line = StringHelper.formatSubredditList(SUBREDDITS) + " selected.";
			
			if (selectedToast == null)
				selectedToast = Toast.makeText(this, "", Toast.LENGTH_LONG);
			selectedToast.setText(line);
			selectedToast.show();
			
			resetWallpaper();
		}
	}

	private void resetWallpaper() {
		AutoWallpaper.sForce = true;
		
		if (forceToast == null)
			forceToast = Toast.makeText(this, "", Toast.LENGTH_LONG);
		forceToast.setText("Next time your wallpaper loads, we'll download a new one.");
		forceToast.show();
	}

	private OnPreferenceClickListener reloadListener = new OnPreferenceClickListener() {
		public boolean onPreferenceClick(Preference preference) {
			resetWallpaper();
			return false;
		}
	};

	private OnPreferenceClickListener aboutListener = new OnPreferenceClickListener() {
		public boolean onPreferenceClick(Preference preference) {
			AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
			builder.setTitle("About");
			builder.setMessage("AutoWallpaper updates your wallpaper with daily goodness!");
			builder.setPositiveButton("OK", null);
			builder.create().show();

			return false;
		}		
	};

}
