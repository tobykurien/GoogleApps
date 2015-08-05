package com.tobykurien.google_news;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebSettings.TextSize;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.tobykurien.google_news.utils.Settings;
import com.tobykurien.google_news.webviewclient.WebClient;

public class GoogleNewsActivity extends Activity implements
		SharedPreferences.OnSharedPreferenceChangeListener {
	private final int DIALOG_SITE = 1;
	private final int DIALOG_TEXT_SIZE = 2;

	private boolean load_images = true;
	private MenuItem stopMenu = null;

	protected boolean v11 = false; // flag to prevent recursive call to v11
									// activity

	WebView wv;
	Settings settings;
	long id;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT >= 11 && !v11) {
			// v11 flag will be set by GoogleNewsActivityv11
			Intent i = new Intent(this, GoogleNewsActivityv11.class);
			if (getIntent().getAction() != null)
				i.setAction(getIntent().getAction());
			if (getIntent().getData() != null)
				i.setData(getIntent().getData());
			startActivity(i);
			finish();
			return;
		}

		settings = Settings.getSettings(this);
		if (settings.isFullscreen()) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		setContentView(R.layout.main);
		CookieSyncManager.createInstance(this);

		wv = getWebView();
		if (wv == null) {
			finish();
			return;
		}

		setupWebView();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// reload settings
		settings = Settings.getSettings(this);
		setupWebView();
	}

	@SuppressLint("JavascriptInterface")
	protected void setupWebView() {
		final ProgressBar pb = getProgressBar();
		if (pb != null)
			pb.setVisibility(View.VISIBLE);

		// WebView.enablePlatformNotifications();
		WebSettings settings = wv.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(false);
		settings.setAllowFileAccess(false);

		// Enable local database.
		settings.setDatabaseEnabled(true);
		String databasePath = this.getApplicationContext()
				.getDir("database", Context.MODE_PRIVATE).getPath();
		settings.setDatabasePath(databasePath);

		// Enable manifest cache.
		String cachePath = this.getApplicationContext()
				.getDir("cache", Context.MODE_PRIVATE).getPath();
		settings.setAppCachePath(cachePath);
		settings.setAllowFileAccess(true);
		settings.setAppCacheEnabled(true);
		settings.setDomStorageEnabled(true);
		settings.setAppCacheMaxSize(1024 * 1024 * 8);
		settings.setCacheMode(WebSettings.LOAD_DEFAULT);
		settings.setLoadsImagesAutomatically(Settings.getSettings(this)
				.isLoadImages());

		// set preferred text size
		setTextSize();

		String userAgent = Settings.getSettings(this).getUserAgent();
		if (!userAgent.equals("")) {
			wv.getSettings().setUserAgentString(userAgent);
		}

		wv.setWebViewClient(getWebViewClient(pb));

		wv.addJavascriptInterface(new Object() {
			// attempt to override the _window function used by Google+ mobile
			// app
			public void open(String url, String stuff, String otherstuff,
					String morestuff, String yetmorestuff, String yetevenmore) {
				throw new IllegalStateException(url); // to indicate success
			}
		}, "window");

		wv.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				Intent i;
				String url = wv.getHitTestResult().getExtra();

				if (url != null) {
					i = new Intent(android.content.Intent.ACTION_VIEW);
					i.setData(Uri.parse(url));
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(i);
					return true;
				}
				return false;
			}
		});

		if (getIntent().getDataString() != null) {
			openSite(getIntent().getDataString());
		} else {
			openSite(getSiteUrl());
		}
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
	 * Return the web view client for the web view
	 * 
	 * @param pb
	 * @return
	 */
	protected WebClient getWebViewClient(final ProgressBar pb) {
		return new WebClient(this, wv, pb);
	}

	/**
	 * Return the site URL to load
	 * 
	 * @return
	 */
	public String getSiteUrl() {
		return getResources().getStringArray(R.array.sites_url)[0];
	}

	public void openSite(String url) {
		wv.loadUrl(url);
	}

	public void setTextSize() {
		TextSize textSize = TextSize.NORMAL;

		int size = Settings.getSettings(this).getFontSize();
		switch (size) {
		case 0:
			textSize = TextSize.SMALLEST;
			break;
		case 1:
			textSize = TextSize.SMALLER;
			break;
		case 2:
			textSize = TextSize.NORMAL;
			break;
		case 3:
			textSize = TextSize.LARGER;
			break;
		case 4:
			textSize = TextSize.LARGEST;
			break;
		}

		wv.getSettings().setTextSize(textSize);
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
		stopMenu = menu.findItem(R.id.menu_stop);
		menu.findItem(R.id.menu_toggle_images)
				.setIcon(
						Settings.getSettings(this).isLoadImages() ? R.drawable.ic_action_image
								: R.drawable.ic_action_broken_image);
		return true;
	}

	public void updateStopItem(boolean pageLoaded) {
		if (pageLoaded) {
			stopMenu.setTitle(R.string.menu_refresh);
			stopMenu.setIcon(R.drawable.ic_action_refresh);
		} else {
			stopMenu.setTitle(R.string.menu_stop);
			stopMenu.setIcon(R.drawable.ic_action_stop);
		}		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_site:
			showDialog(DIALOG_SITE);
			return true;
		case R.id.menu_stop:
			if(getString(R.string.menu_stop).equals(stopMenu.getTitle())) {
				wv.stopLoading();
			} else {
				wv.reload();
			}
			return true;
		case R.id.menu_toggle_images:
			load_images = !load_images;
			Toast.makeText(this, "Toggle images", Toast.LENGTH_SHORT).show();
			SharedPreferences pref = PreferenceManager
					.getDefaultSharedPreferences(GoogleNewsActivity.this);
			pref.edit().putBoolean("load_images", load_images).apply();
			item.setIcon(load_images ? R.drawable.ic_action_image
					: R.drawable.ic_action_broken_image);
			wv.getSettings().setLoadsImagesAutomatically(load_images);
			return true;
		case R.id.menu_settings:
			// showDialog(DIALOG_TEXT_SIZE);
			Intent i = new Intent(this, Preferences.class);
			startActivity(i);
			return true;
		}
		return false;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;

		switch (id) {
		case DIALOG_SITE:
			dialog = new AlertDialog.Builder(this).setTitle("Select Site")
					.setItems(R.array.sites, new OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
							String url = getResources().getStringArray(
									R.array.sites_url)[arg1];
							openSite(url);
						}
					}).create();
			return dialog;
		case DIALOG_TEXT_SIZE:
			dialog = new AlertDialog.Builder(this)
					.setTitle(R.string.menu_text_size)
					.setItems(R.array.text_sizes, new OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
							SharedPreferences pref = PreferenceManager
									.getDefaultSharedPreferences(GoogleNewsActivity.this);
							pref.edit().putInt("text_size", arg1).commit();
							setTextSize();
						}
					}).create();
			return dialog;
		}

		return super.onCreateDialog(id);
	}
}