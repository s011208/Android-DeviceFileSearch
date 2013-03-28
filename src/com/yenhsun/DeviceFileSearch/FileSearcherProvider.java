
package com.yenhsun.DeviceFileSearch;

import java.util.HashMap;
import java.util.Map.Entry;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;

public class FileSearcherProvider extends ContentProvider {

    private final String TAG = "com.android.providers.media.FileSearcherProvider";

    private final boolean DEBUG = false;

    /**
     * Format type field
     */
    private final static int format_Directory = 12289;

    /**
     * Image resource field
     */
    private final static int image_file = R.drawable.asus_ep_filemanage_document;

    private final static int image_audio = R.drawable.asus_ep_icon_music_cover;

    private final static int image_video = R.drawable.asus_ep_icon_video_cover;

    private final static int image_photo = R.drawable.asus_ep_icon_photo_cover;

    private final static int image_directory = R.drawable.asus_ep_filemanage_directory;

    private final static int image_general = R.drawable.asus_ep_filemanage_general;

    private final static int image_apk = R.drawable.asus_ep_filemanage_apk;

    private final static int image_zip = R.drawable.asus_ep_icon_zip_cover;

    private final static int image_abu = R.drawable.asus_ep_filemanage_abu;

    /**
     * Column field
     */
    // absolutely file path ex. /mnt/sdcard/....
    private final static String column_data = MediaStore.Files.FileColumns.DATA;

    // Primary Key
    private final static String column_id = MediaStore.Files.FileColumns._ID;

    // file name
    private final static String column_display_name = MediaStore.Files.FileColumns.DISPLAY_NAME;

    // file name
    private final static String column_title = MediaStore.Files.FileColumns.TITLE;

    private final static String column_mime_type = MediaStore.Files.FileColumns.MIME_TYPE;

    // Tracking MediaFile.java for more details
    private final static String column_media_type = MediaStore.Files.FileColumns.MEDIA_TYPE;

    // The followings are for sql order only.
    private final static String column_size = MediaStore.Files.FileColumns.SIZE;

    private final static String column_date = MediaStore.Files.FileColumns.DATE_MODIFIED;

    private final static String internalStoragePath = Environment.getExternalStorageDirectory()
            .getAbsolutePath();

    private final static int lastIndexOfInternalStoragePath = internalStoragePath.lastIndexOf("/");

    /**
     * Standard query language. get rid of the '/storage' because of the
     * consistency with FileManager
     */
    private final static String strSuggestColumnText2AndIntentData = "Case " + " when SUBSTR("
            + column_data + ", 0, " + (internalStoragePath.length() + 1) + ") = '"
            + internalStoragePath + "' then '"
            + Environment.getExternalStorageDirectory().getAbsolutePath() + "' || SUBSTR("
            + column_data + ", " + (internalStoragePath.length() + 1) + ") " + " else "
            + column_data + " end ";

    /**
     * Standard query language. for SearchManager.SUGGEST_COLUMN_TEXT_1
     */
    private final static String strSuggestColumnText1 = "Case " + " when " + column_display_name
            + " not NULL then " + column_display_name + " else case when " + column_title
            + " not NULL then " + column_title + " else substr(" + column_data + ", "
            + (internalStoragePath.length() + 2) + ")" + " end end";

    /**
     * Build projections
     */
    // select columns' name, the null/empty columns may be filled up later
    private static String[] projections = new String[] {
            column_id + " as " + BaseColumns._ID,
            strSuggestColumnText1 + " as " + SearchManager.SUGGEST_COLUMN_TEXT_1,
            strSuggestColumnText2AndIntentData + " as " + SearchManager.SUGGEST_COLUMN_TEXT_2,
            strSuggestColumnText2AndIntentData + " as " + SearchManager.SUGGEST_COLUMN_INTENT_DATA,
            "", // SearchManager.SUGGEST_COLUMN_ICON_1
            column_mime_type + " as " + SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA,

            // do not save in searching history
            "'" + SearchManager.SUGGEST_NEVER_MAKE_SHORTCUT + "' as "
                    + SearchManager.SUGGEST_COLUMN_SHORTCUT_ID
    };

    /**
     * The external storage uri
     */
    private final static Uri FileUri = MediaStore.Files.getContentUri("external");

    /**
     * Build the map for icons. Using the file extension is more specific (but
     * more inefficient). Otherwise, it will use the media_type directly (more
     * general).
     * 
     * @return A icons map. <innerK(media_type, ext), icon>
     */
    private static HashMap<innerK, Integer> IconMap() {
        // <innerK(ext), icon>
        // <innerK(media_type, ext), icon>
        HashMap<innerK, Integer> IconMap = new HashMap<innerK, Integer>();
        IconMap.put(new innerK(".zip"), image_zip); // apk
        IconMap.put(new innerK(".apk"), image_apk); // apk
        IconMap.put(new innerK(".abu"), image_abu); // abu
        IconMap.put(new innerK(".eml"), image_general); // eml
        IconMap.put(new innerK(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE, null), image_photo);
        IconMap.put(new innerK(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO, null), image_audio);
        IconMap.put(new innerK(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO, null), image_video);
        IconMap.put(new innerK(MediaStore.Files.FileColumns.MEDIA_TYPE_PLAYLIST, null), image_audio);

        return IconMap;
    }

    private final static String strIconMap = buildIconMap();

    private static String buildIconMap() {
        StringBuilder rtnSb = new StringBuilder();
        for (Entry<innerK, Integer> e : IconMap().entrySet()) {
            if (e.getKey().ext != null) {
                rtnSb.append(" WHEN ");
                rtnSb.append("substr(" + column_display_name + " , -4) = '");
                rtnSb.append(e.getKey().ext);
                rtnSb.append("' THEN ");
                rtnSb.append(e.getValue());
            } else if (e.getKey().media_type != -1) {
                rtnSb.append(" WHEN ");
                rtnSb.append(column_media_type);
                rtnSb.append(" = ");
                rtnSb.append(e.getKey().media_type);
                rtnSb.append(" THEN ");
                rtnSb.append(e.getValue());
            }
            // else using general file icon
        }
        return rtnSb.toString();
    }

    /**
     * The inner class for IconMap Key.
     * 
     * @author Yen-Hsun_Huang
     */
    private static class innerK {
        public int media_type = -1;

        public String ext = null;

        innerK(String ext) {
            this.ext = ext;
        }

        innerK(int media_type, String ext) {
            this.media_type = media_type;
            this.ext = ext;
        }
    }

    /**
     * The sort order for files, image = 1, audio = 2, video = 3, document = 10,
     * book = 11, zip = 12, abu = 13, apk = 14. Because directory is append to
     * the file query, directory doesn't need to be ordered.
     */
    private final static int SortOrder_zip = 1000;

    private final static int SortOrder_eml = 1005;

    private final static int SortOrder_abu = 1010;

    private final static int SortOrder_apk = 1020;

    private final static int SortOrder_pub = 1025;

    private final static String strSortOrderForFiles = "CASE " + " WHEN substr("
            + column_display_name + " , -4) = '.zip' " + " THEN " + SortOrder_zip + " WHEN substr("
            + column_display_name + " , -4) = '.eml' " + " THEN " + SortOrder_eml + " WHEN substr("
            + column_display_name + " , -4) = '.abu' " + " THEN " + SortOrder_abu + " WHEN substr("
            + column_display_name + " , -4) = '.apk' " + " THEN " + SortOrder_apk + " WHEN substr("
            + column_display_name + " , -4) = '.pub' " + " THEN " + SortOrder_pub + " ELSE "
            + column_media_type + " END " // order by type
            + " , " + column_display_name + " COLLATE NOCASE "// order by
                                                              // name
            + " , " + column_size // order by size
            + " ," + column_date + " desc "// order by date
    ;

    /**
     * The sort order files for directories.
     */
    private final static String strSortOrderForDirectories = "CASE " + " WHEN " + column_title
            + " IS NOT NULL " + " THEN 0 ELSE 1 END, " + column_title + " COLLATE NOCASE, "
            + column_data + " COLLATE NOCASE, " + column_date;

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    /**
     * Set the query arguments at first and get two query results, one from
     * files query, the other from directories. Finally, this method will return
     * a cursor which merges from two query results.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        if (DEBUG)
            Log.d(TAG, "query");
        Cursor[] cursors = new Cursor[2];

        // escape the special characters
        selectionArgs[0] = selectionArgs[0].replace("\\", "\\\\").replace("%", "\\%")
                .replace(".", "\\.").replace("_", "\\_").replace("*", "\\");
        try {
            cursors[1] = ReturnCursor_Without_Directory(selectionArgs[0]);
            cursors[0] = ReturnCursor_Only_Directory(selectionArgs[0]);

            return new MergeCursor(cursors);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    /**
     * Return a cursor, files only.
     * 
     * @param queryString The search key
     * @param projections the selection columns
     * @param sortOrder order by MediaStore.Files.FileColumns.MIME_TYPE and
     *            MediaStore.Files.FileColumns.TITLE
     * @return A cursor built from query result
     */
    private Cursor ReturnCursor_Without_Directory(String queryString) {
        /**
         * Sort Order TYPE_FILE = 0; //ignore TYPE_IMAGE = 1; TYPE_AUDIO = 2;
         * TYPE_PLAYLIST = 3; //ignore TYPE_VIDEO = 4; TYPE_DOCUMENT = 5;
         * TYPE_BOOK = 6; TYPE_ZIP = 7; //ignore? filemanager cannot handle it,
         * either. TYPE_ABU = 8; TYPE_APK = 9; TYPE_DIR = 10;
         */

        projections[4] = "CASE" + strIconMap + " else " + image_general + " END as "
                + SearchManager.SUGGEST_COLUMN_ICON_1;

        // where clause: excluding directory and unknown files
        String selections = "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + " != "
                + MediaStore.Files.FileColumns.MEDIA_TYPE_NONE + " or substr("
                + column_display_name + " , -4) = '.zip'" + " or substr(" + column_display_name
                + " , -4) = '.eml'" + " or substr(" + column_display_name + " , -4) = '.abu'"
                + " or substr(" + column_display_name + " , -4) = '.pub'" + " or substr("
                + column_display_name + " , -4) = '.apk'" // union
                + ") and " + column_display_name + " like '%" + queryString + "%' ESCAPE '\\'" // if
                                                                                               // matched
                // hide "hidden files
                + " and " + column_data + " not like '%/.%'";
        // return a new cursor
        return new CrossProcessCursorWrapper(getContext().getContentResolver().query(FileUri,
                projections, selections, null, strSortOrderForFiles));

    }

    /**
     * Return a cursor, directories only.
     * 
     * @param queryString The search key
     * @param projections the selection columns
     * @param sortOrder order by MediaStore.Files.FileColumns.MIME_TYPE and
     *            MediaStore.Files.FileColumns.TITLE
     * @return A cursor built from query result
     */
    private Cursor ReturnCursor_Only_Directory(String queryString) {
        // SearchManager.SUGGEST_COLUMN_ICON_1
        projections[4] = image_directory + " as " + SearchManager.SUGGEST_COLUMN_ICON_1;
        // where clause
        String selections = "( " + column_title + " like '%" + queryString + "%'" + " or ("
                + column_title + " IS NULL" + " and substr(" + column_data + ", "
                + (lastIndexOfInternalStoragePath + 1) + ") like '%" + queryString + "%'))"
                + " and format = " + format_Directory // directory
                                                      // only
                + " and " + column_data + " not like '%/.%'" // hide
                                                             // "hidden directories"
        ;

        // return a new cursor
        return new CrossProcessCursorWrapper(getContext().getContentResolver().query(FileUri,
                projections, selections, null, strSortOrderForDirectories));
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
