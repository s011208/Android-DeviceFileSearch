package com.yenhsun.DeviceFileSearch;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

public class ProxyActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		ContentResolver cr = getContentResolver();
//		Uri media_uri = MediaStore.Files.getContentUri("external");
//		Cursor c = cr
//				.query(media_uri, new String[] { "_data" }, "", null, null);
//
//		while (c.moveToNext()) {
//			Log.e("QQQQ", c.getString(0));
//		}
//		c.close();
	}
}
