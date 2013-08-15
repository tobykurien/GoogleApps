package com.tobykurien.google_news;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ApiLevel11 {
   public static void hideActionBar(Activity activity) {
      activity.getActionBar().hide();
   }

   public static void showActionBar(Activity activity) {
      activity.getActionBar().show();
   }

   public static void offsetViewForOverlayAB(Activity activity, WebView wv) {
      //Toast.makeText(activity, "ab height: " + activity.getActionBar().getHeight(), Toast.LENGTH_LONG).show();
      int marginTop = 72; //activity.getActionBar().getHeight();
      TextView tv = new TextView(activity);
      tv.setHeight(marginTop);
      tv.setVisibility(View.INVISIBLE);
      wv.addView(tv, 0);
   }
}
