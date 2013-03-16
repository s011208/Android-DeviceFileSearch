package com.yenhsun.DeviceFileSearch;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class FilesProvider extends ContentProvider {

	private static final String TAG = "QQQQQ";

	private static final boolean DEBUG = true;

	private static final String COLUMN_DATA = "_data";

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getType(Uri uri) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean onCreate() {
		Log.e(TAG, "ONCREATE");
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Log.e(TAG, "QQQQQQQQery");
		ContentResolver cr = getContext().getContentResolver();
		Uri media_uri = MediaStore.Files.getContentUri("external");
		for (String s : projection) {
			Log.e(TAG, s);
		}
		Cursor c = cr.query(media_uri, new String[] { COLUMN_DATA }, "", null,
				COLUMN_DATA + " asc");
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

}
