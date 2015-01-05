package com.tobykurien.google_news.utils;

import java.util.Set;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class Settingsv11 extends Settings {

	protected Settingsv11(SharedPreferences preferences) {
		super(preferences);
	}

	public static Settingsv11 getSettings(Context context) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context.getApplicationContext());
		return new Settingsv11(preferences);
	}

	public Set<String> getEnabledSites() {
		return pref.getStringSet("enabled_sites", (Set<String>) null);
	}
}
