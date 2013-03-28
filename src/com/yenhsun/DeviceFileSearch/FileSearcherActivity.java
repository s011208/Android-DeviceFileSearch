
package com.yenhsun.DeviceFileSearch;

import java.io.File;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class FileSearcherActivity extends Activity {
    String Tag = this.getClass().getName();

    boolean DEBUG = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (DEBUG)
            Log.d(Tag, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        onNewIntent(getIntent());
    }

    protected void onNewIntent(Intent intent) {
        if (DEBUG)
            Log.d(Tag, "onNewIntent");
        HandleIntentAction(intent);
        finish();
    }

    /**
     * @param intent from getIntent()
     */
    private void HandleIntentAction(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            String filePath = intent.getDataString();
            String MIME_TYPE = intent.getStringExtra(SearchManager.EXTRA_DATA_KEY);

            File file = new File(filePath);
            // if filePath is a directory
            if (file.isDirectory()) {
                HandleDirectory(intent);
            }
            // if filePath is a file
            else {
                HandleFile(intent, file, MIME_TYPE);
            }
        }
    }

    private void HandleFile(Intent intent, File file, String MIME_TYPE) {
        try {
            if (DEBUG)
                Log.d(Tag, "HandleFile");

            if (".apk".equals(getExtension(file.getPath()))) {
                if (DEBUG)
                    Log.d(Tag, "Is APK");
                Intent targetIntent = new Intent(Intent.ACTION_VIEW);
                targetIntent.setDataAndType(Uri.fromFile(file),
                        "application/vnd.android.package-archive");
                startActivity(targetIntent);
            } else if (".zip".equals(getExtension(file.getPath()))) {
                if (DEBUG)
                    Log.d(Tag, "Is zip");
                PackageManager pm = getPackageManager();
                Intent targetIntent = pm.getLaunchIntentForPackage("com.asus.filemanager");
                targetIntent.setAction(Intent.ACTION_VIEW);
                targetIntent.setDataAndType(Uri.parse("file://" + intent.getDataString()),
                        "application/zip");
                targetIntent.putExtra("path", intent.getDataString());
                targetIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(targetIntent);
            } else {
                if (DEBUG)
                    Log.d(Tag, "others");
                Intent targetIntent = new Intent(Intent.ACTION_VIEW);
                targetIntent.setDataAndType(Uri.fromFile(file), MIME_TYPE);
                startActivity(targetIntent);
            }
        } catch (Exception e) {
            // cannot handle this data type
            if (DEBUG)
                Log.d(Tag, "Exception :" + e.toString());
            Toast.makeText(getBaseContext(), "Cannot handle this file", Toast.LENGTH_SHORT).show();
        }
    }

    private void HandleDirectory(Intent intent) {
        if (DEBUG)
            Log.d(Tag, "HandleDirectory");
        try {
            PackageManager pm = getPackageManager();

            Intent targetIntent = pm.getLaunchIntentForPackage("com.asus.filemanager");
            targetIntent.setAction(Intent.ACTION_VIEW);
            targetIntent.setType("text/plain");
            targetIntent.putExtra("path", intent.getDataString());
            targetIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(targetIntent);
            if (DEBUG)
                Log.d(Tag, "intent data : " + intent.getDataString());
        } catch (Exception e) {
            if (DEBUG)
                Log.d(Tag, "Exception :" + e.toString());
            Toast.makeText(getBaseContext(), "Cannot handle this directory", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public String getExtension(String path) {
        int dot = path.lastIndexOf(".");
        return dot == -1 ? "" : path.substring(dot).toLowerCase();
    }
}
