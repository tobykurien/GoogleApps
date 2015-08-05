package com.tobykurien.google_news;

import java.util.Set;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

import com.tobykurien.google_news.utils.Settingsv11;
import com.tobykurien.google_news.webviewclient.WebClient;
import com.tobykurien.google_news.webviewclient.WebClientv11;

/**
 * Extensions to the main activity for Android 3.0+
 * 
 * @author toby
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class GoogleNewsActivityv11 extends GoogleNewsActivity implements OnSharedPreferenceChangeListener {
	// variables to track dragging for actionbar auto-hide
	protected float startX;
	protected float startY;
	boolean skipInitialLoad = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		v11 = true; // prevent recursive activity redirects
		super.onCreate(savedInstanceState);

		if (getIntent().getDataString() != null) {
			skipInitialLoad = true;
		}

		setupActionBar();
		
		PreferenceManager.getDefaultSharedPreferences(this)
			.registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// reload the actionbar when preferences change
		setupActionBar();
	}

	private void setupActionBar() {
		// setup actionbar
		ActionBar ab = getActionBar();
		ab.setDisplayShowTitleEnabled(false);
		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// get which sites to show
		Set<String> sites = Settingsv11.getSettings(this).getEnabledSites();
		String[] displaySites = null;
		if (sites == null || sites.size() == 0) {
			displaySites = getResources().getStringArray(R.array.sites);
		} else {
			displaySites = sites.toArray(new String[] {});
		}

		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1,
				displaySites);
		ab.setListNavigationCallbacks(adapter, new OnNavigationListener() {
			@Override
			public boolean onNavigationItemSelected(int arg0, long arg1) {
				// find the URL for the selected site by name
				String name = adapter.getItem(arg0);
				String url = null;
				String[] sites = getResources().getStringArray(R.array.sites);
				for (int i=0; i < sites.length; i++) {
					if (name.equals(sites[i])) {
						url = getResources().getStringArray(R.array.sites_url)[i];
					}
				}

				if (!skipInitialLoad && url != null) openSite(url);
				else skipInitialLoad = false;
				return true;
			}
		});

		autohideActionbar();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean ret = super.onCreateOptionsMenu(menu);
		menu.findItem(R.id.menu_site).setVisible(false);
		return ret;
	}

	@Override
	protected WebClient getWebViewClient(ProgressBar pb) {
		return new WebClientv11(this, wv, pb);
	}

	/**
	 * Attempt to make the actionBar auto-hide and auto-reveal based on drag,
	 * but unfortunately makes the bit under the actionbar mostly inaccessible,
	 * so leaving this out for now.
	 * 
	 * @param activity
	 * @param wv
	 */
	public void autohideActionbar() {
		wv.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				if (settings.isHideActionbar()) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						startY = event.getY();
					}

					if (event.getAction() == MotionEvent.ACTION_MOVE) {
						// avoid juddering by waiting for large-ish drag
						if (Math.abs(startY - event.getY()) > new ViewConfiguration()
								.getScaledTouchSlop() * 5) {
							if (startY < event.getY())
								getActionBar().show();
							else
								getActionBar().hide();
						}
					}
				}

				return false;
			}
		});
	}
}
