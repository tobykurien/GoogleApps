package com.tobykurien.google_news;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class GoogleNewsActivity extends Activity {
   boolean siteLoaded = false;
   
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
      return "https://news.google.com/";
   }

   @Override
   protected void onStart() {
      super.onStart();

      WebView wv = getWebView();
      if (wv == null) {
         finish();
         return;
      }
      
      if (siteLoaded) return;
               
      final ProgressBar pb = getProgressBar();
      if (pb != null)
         pb.setVisibility(View.VISIBLE);

      //wv.loadData("<html><head></head><body>Loading Google News...</body></html>",  "text/html", null);
      wv.enablePlatformNotifications();
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
                     && !uri.getHost().endsWith("google.com")) {
               Intent i = new Intent(android.content.Intent.ACTION_VIEW);
               i.setData(uri);
               startActivity(Intent.createChooser(i, "Open Url"));
               return true;
            } else if (uri.getScheme().equals("mailto")) {
               Intent i = new Intent(android.content.Intent.ACTION_SEND);
               i.putExtra(android.content.Intent.EXTRA_EMAIL, url);
               i.setType("text/html");
               startActivity(Intent.createChooser(i, "Send Email"));
               return true;
            }

            return super.shouldOverrideUrlLoading(view, url);
         }
      });
   }
}