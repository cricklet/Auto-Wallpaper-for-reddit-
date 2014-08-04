package com.autowallpaper;

import com.google.analytics.tracking.android.EasyTracker;

import android.app.Application;

public class MainApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		
		EasyTracker.getInstance().setContext(this);		
		Settings.setupSettings(this);
	}

}
