package com.autowallpaper.custom;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;

import com.autowallpaper.helper.StringHelper;
import com.h6ah4i.android.compat.preference.MultiSelectListPreferenceCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.FrameLayout.LayoutParams;

public class SubredditPreference extends MultiSelectListPreferenceCompat {
	
	public SubredditPreference(Context context) {
		this(context, null);
	}
	
	public SubredditPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SubredditPreference(Context context, int entries, String title, String name) {
		super(context);
		
		this.setEntries(entries);
		this.setEntryValues(entries);
		this.setTitle(title);
		this.setKey(name);
	}

    @Override
    public CharSequence getSummary() {
		Set<String> values = getValues();
		
    	if (values.size() == 0)
    		return "None selected";
    	
    	return StringHelper.formatSubredditList(values) + " selected";
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
		notifyChanged();
		super.onDialogClosed(positiveResult);
	}
	


}
