package com.tobykurien.google_news;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.settings);
      
      if (Build.VERSION.SDK_INT < 11) {
         // older versions of Android don't support all settings
         getPreferenceScreen().findPreference("block_3rd_party").setEnabled(false);
         getPreferenceScreen().findPreference("hide_actionbar").setEnabled(false);
      }
   }
}
