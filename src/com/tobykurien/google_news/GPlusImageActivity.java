package com.tobykurien.google_news;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

public class GPlusImageActivity extends Activity {
    Uri uri;
    ImageView iv;

    DownloadManager dm;
    long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gplus_image);
        Intent i = getIntent();
        uri = Uri.parse(i.getStringExtra("url"));
        dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        id = dm.enqueue(new DownloadManager.Request(uri));
        BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String act = intent.getAction();

                if(DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(act)){
                    DownloadManager.Query dmq = new DownloadManager.Query();
                    dmq.setFilterById(id);
                    Cursor c = dm.query(dmq);
                    if(c.moveToFirst()) {
                        iv = (ImageView) findViewById(R.id.imageView);
                        String urlStr = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        iv.setImageURI(Uri.parse(urlStr));
                    }
                }
            }
        };

        registerReceiver(br, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    public void onPause(){
        dm.remove(id);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gplus_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
