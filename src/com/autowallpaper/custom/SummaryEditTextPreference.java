package com.autowallpaper.custom;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

public class SummaryEditTextPreference extends EditTextPreference {

    public SummaryEditTextPreference(Context context) {
        super(context);
    }

    public SummaryEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SummaryEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public String getSummary() {
    	if (getText() == null || getText().length() == 0)
    		return "You can set multiple custom subreddits by" +
    		" separating them with plus signs (i.e. gaming+nintendo+doctorwho)";
    	return getText();
    }
}