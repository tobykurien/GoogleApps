package com.tobykurien.google_news;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class GoogleNewsActivityv11 extends GoogleNewsActivity {
   protected float startX;
   protected float startY;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      v11 = true; // prevent recursive activity redirects
      super.onCreate(savedInstanceState);
      
      // setup actionbar
      ActionBar ab = getActionBar();
      ab.setDisplayShowTitleEnabled(false);
      ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
      ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
               android.R.layout.simple_list_item_1,
               android.R.id.text1,
               getResources().getStringArray(R.array.sites));
      ab.setListNavigationCallbacks(adapter, new OnNavigationListener() {
         @Override
         public boolean onNavigationItemSelected(int arg0, long arg1) {
            String url = getResources().getStringArray(R.array.sites_url)[arg0];
            openSite(url);
            return true;
         }
      });
   }
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      boolean ret = super.onCreateOptionsMenu(menu);
      menu.findItem(R.id.menu_site).setVisible(false);
      return ret;
   }

   public void offsetViewForOverlayAB(Activity activity, WebView wv) {
      // Toast.makeText(activity, "ab height: " +
      // activity.getActionBar().getHeight(), Toast.LENGTH_LONG).show();
      int marginTop = 72; // activity.getActionBar().getHeight();
      TextView tv = new TextView(activity);
      tv.setHeight(marginTop);
      tv.setVisibility(View.INVISIBLE);
      wv.addView(tv, 0);

      wv.setOnTouchListener(new OnTouchListener() {
         @Override
         public boolean onTouch(View arg0, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
               startY = event.getY();
            }

            if (event.getAction() == MotionEvent.ACTION_MOVE) {
               if (Math.abs(startY - event.getY()) > new ViewConfiguration().getScaledTouchSlop()) {
                  if (startY < event.getY()) 
                     getActionBar().show();
                  else
                     getActionBar().hide();
               }
            }

            return false;
         }
      });
   }
}
