package com.tobykurien.google_news;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

public class GoogleNewsActivity extends Activity {
   boolean siteLoaded = false;
   WebView wv;

   String[] googleSites = new String[]{ 
     "google.com", "youtube.com",
     "google.co.za"
   };
   
   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
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
      return "https://mobile.google.com/";
   }

   @Override
   protected void onStart() {
      super.onStart();

      wv = getWebView();
      if (wv == null) {
         finish();
         return;
      }
      
      if (siteLoaded) return;
               
      final ProgressBar pb = getProgressBar();
      if (pb != null)
         pb.setVisibility(View.VISIBLE);

      //wv.loadData("<html><head></head><body>Loading Google News...</body></html>",  "text/html", null);
      WebView.enablePlatformNotifications();
      wv.getSettings().setJavaScriptEnabled(true);
      wv.loadUrl(getSiteUrl());
      
      // wv.getSettings().setUserAgentString("android");
      wv.setWebViewClient(new WebViewClient() {
         @Override
         public void onPageFinished(WebView view, String url) {
            if (pb != null)
               pb.setVisibility(View.GONE);
            siteLoaded = true;
            super.onPageFinished(view, url);
         }

         @Override
         public void onPageStarted(WebView view, String url, Bitmap favicon) {
            siteLoaded = false;
            if (pb != null) pb.setVisibility(View.VISIBLE);
            super.onPageStarted(view, url, favicon);
         }

         @Override
         public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Uri uri = Uri.parse(url);
            if ((uri.getScheme().equals("http") || uri.getScheme().equals("https")) 
                     && !isGoogleSite(uri.getHost())) {
               Intent i = new Intent(android.content.Intent.ACTION_VIEW);
               i.setData(uri);
               i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
               startActivity(i);
               return true;
            } else if (uri.getScheme().equals("mailto")) {
               Intent i = new Intent(android.content.Intent.ACTION_SEND);
               i.putExtra(android.content.Intent.EXTRA_EMAIL, url);
               i.setType("text/html");
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
   }
   
   private boolean isGoogleSite(String host) {
      for (String site : googleSites) {
         if (host.toLowerCase().endsWith(site.toLowerCase())) {
            return true;
         }
      }
      return false;
   }
   
   @Override
   public boolean onKeyDown(int keyCode, KeyEvent event) {
       if ((keyCode == KeyEvent.KEYCODE_BACK) && wv.canGoBack()) {
           wv.goBack();
           return true;
       }
       return super.onKeyDown(keyCode, event);
   }    
}