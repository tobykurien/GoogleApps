package com.tobykurien.google_news;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

public class GoogleNewsActivity extends Activity {
   private final int DIALOG_SITE = 1;

   WebView wv;

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
      CookieSyncManager.createInstance(this);

      wv = getWebView();
      if (wv == null) {
         finish();
         return;
      }

      final ProgressBar pb = getProgressBar();
      if (pb != null) pb.setVisibility(View.VISIBLE);

      WebView.enablePlatformNotifications();
      WebSettings settings = wv.getSettings();
      settings.setJavaScriptEnabled(true);
      settings.setJavaScriptCanOpenWindowsAutomatically(false);

      // Enable local database.
      settings.setDatabaseEnabled(true);
      String databasePath = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
      settings.setDatabasePath(databasePath);

      // Enable manifest cache.
      String cachePath = this.getApplicationContext().getDir("cache", Context.MODE_PRIVATE).getPath();
      settings.setAppCachePath(cachePath);
      settings.setAllowFileAccess(true);
      settings.setAppCacheEnabled(true);
      settings.setDomStorageEnabled(true);
      settings.setAppCacheMaxSize(1024 * 1024 * 8);
      settings.setCacheMode(WebSettings.LOAD_DEFAULT);

      // wv.getSettings().setUserAgentString("android");
      wv.setWebViewClient(new WebViewClient() {
         @Override
         public void onPageFinished(WebView view, String url) {
            if (pb != null) pb.setVisibility(View.GONE);
            
            // Google+ workaround to prevent opening of blank window
            wv.loadUrl("javascript:_window=function(url){ location.href=url; }");

            CookieSyncManager.getInstance().sync();
            super.onPageFinished(view, url);
         }

         @Override
         public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d("Google", "loading " + url);

            if (pb != null) pb.setVisibility(View.VISIBLE);
            super.onPageStarted(view, url, favicon);
         }

         @Override
         public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Uri uri = Uri.parse(url);
            Log.d("Google", "should override " + uri);
            if ((uri.getScheme().equals("http") || uri.getScheme().equals("https")) && !isGoogleSite(uri)) {
               Intent i = new Intent(android.content.Intent.ACTION_VIEW);
               i.setData(uri);
               startActivity(i);
               return true;
            } else if (uri.getScheme().equals("mailto")) {
               Intent i = new Intent(android.content.Intent.ACTION_SEND);
               i.putExtra(android.content.Intent.EXTRA_EMAIL, url);
               i.setType("text/html");
               startActivity(i);
               return true;
            } else if (uri.getScheme().equals("market")) {
               Intent i = new Intent(android.content.Intent.ACTION_VIEW);
               i.setData(uri);
               i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
               startActivity(i);
               return true;
            }

            return super.shouldOverrideUrlLoading(view, url);
         }

         @Override
         public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            // TODO Auto-generated method stub
            super.onReceivedError(view, errorCode, description, failingUrl);
            Toast.makeText(GoogleNewsActivity.this, description, Toast.LENGTH_LONG).show();
         }
      });

      wv.addJavascriptInterface(new Object() {
         // attempt to override the _window function used by Google+ mobile app
         public void open(String url, String stuff, String otherstuff, String morestuff, String yetmorestuff, String yetevenmore) {
            throw new IllegalStateException(url); // to indicate success
         }
      }, "window");

      wv.setOnLongClickListener(new OnLongClickListener() {
         @Override
         public boolean onLongClick(View arg0) {
            String url = wv.getHitTestResult().getExtra();
            if (url != null) {
               Intent i = new Intent(android.content.Intent.ACTION_VIEW);
               i.setData(Uri.parse(url));
               i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
               startActivity(i);
               return true;
            }

            return false;
         }
      });

      openSite(getSiteUrl());
   }

   /**
    * Return the title bar progress bar to indicate progress
    * 
    * @return
    */
   public ProgressBar getProgressBar() {
      return (ProgressBar) findViewById(R.id.site_progress);
   }

   /**
    * Return the web view in which to display the site
    * 
    * @return
    */
   public WebView getWebView() {
      return (WebView) findViewById(R.id.site_webview);
   }

   /**
    * Return the site URL to load
    * 
    * @return
    */
   public String getSiteUrl() {
      return getResources().getStringArray(R.array.sites_url)[0];
   }

   @Override
   protected void onStart() {
      super.onStart();
   }

   private boolean isGoogleSite(Uri uri) {
      // String url = uri.toString();
      String host = uri.getHost();
      String[] googleSites = getResources().getStringArray(R.array.google_sites);
      for (String site : googleSites) {
         if (host.toLowerCase().endsWith(site.toLowerCase())) { return true; }
      }
      return false;
   }

   public void openSite(String url) {
      wv.loadUrl(url);
   }
   
   @Override
   public boolean onKeyDown(int keyCode, KeyEvent event) {
      if ((keyCode == KeyEvent.KEYCODE_BACK) && wv.canGoBack()) {
         wv.goBack();
         return true;
      }
      return super.onKeyDown(keyCode, event);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      super.onCreateOptionsMenu(menu);
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.menu, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case R.id.menu_site:
            showDialog(DIALOG_SITE);
            return true;
         case R.id.menu_exit:
            finish();
            return true;
      }
      return false;
   }

   @Override
   protected Dialog onCreateDialog(int id) {
      Dialog dialog = null;

      switch (id) {
         case DIALOG_SITE:
            dialog = new AlertDialog.Builder(this).setTitle("").setSingleChoiceItems(R.array.sites, 0, new OnClickListener() {
               @Override
               public void onClick(DialogInterface arg0, int arg1) {
                  arg0.dismiss();
                  String url = getResources().getStringArray(R.array.sites_url)[arg1];
                  openSite(url);
               }
            }).create();
            return dialog;
      }

      return super.onCreateDialog(id);
   }
}