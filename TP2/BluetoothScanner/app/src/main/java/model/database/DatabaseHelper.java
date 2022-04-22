package model.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;
import androidx.annotation.Nullable;

/**
 * Configure SQLite local database
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private final Context context;

    public static final String DATABASE_NAME = "BluetoothDevices.db";
    public static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "detected_devices";

    // define column names for SQL table
    private static final String COLUMN_ADDRESS  = "address";
    private static final String COLUMN_NAME     = "name";
    private static final String COLUMN_LAT      = "lat";
    private static final String COLUMN_LNG      = "lng";
    private static final String COLUMN_TYPE     = "type";
    private static final String COLUMN_FAVORITE = "favorite";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ADDRESS + " TEXT PRIMARY KEY, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_LAT + " REAL, " +
                COLUMN_LNG + " REAL, " +
                COLUMN_TYPE + " INTEGER, " +
                COLUMN_FAVORITE + " INTEGER);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * Add device to SQL table
     */
    public boolean addDevice(String address, String name, double lat, double lng, int type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_ADDRESS, address);
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_LAT, lat);
        cv.put(COLUMN_LNG, lng);
        cv.put(COLUMN_TYPE, type);
        cv.put(COLUMN_FAVORITE, 0); // set default value to 0

        // replace returns row id or -1 if an error occurred
        long res = db.replace(TABLE_NAME, null, cv);
        return res >= 0;
    }

    public Cursor readAllData() {
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if (db != null) {
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    public Cursor readFavorites() {
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_FAVORITE + "=1;";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if (db != null) {
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    /**
     * Giving mac address, set favorite value to 1 if it was 0 or vice versa
     * Return true if the device is added to favorites else false
     */
    @SuppressLint("Recycle")
    public boolean toggleFavorite(String addr) {
        // read favorite value
        String query = "SELECT " + COLUMN_FAVORITE + " FROM " + TABLE_NAME + " WHERE " +
                COLUMN_ADDRESS + "='" + addr +"';";
        SQLiteDatabase db = this.getReadableDatabase();

        int favorite = -1;
        Cursor cursor = null;
        if (db != null) {
            cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                favorite = cursor.getInt(0);
            }
        }

        // show error if input address is not in the table
        if (favorite == -1) {
            Toast.makeText(context, "Failed to get favorite value", Toast.LENGTH_SHORT).show();
        }

        // change favorite value
        db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        // toggle favorite value
        favorite = favorite == 0 ? 1: 0;
        cv.put(COLUMN_FAVORITE, favorite);

        // update returns the number of rows affected
        int res = db.update(TABLE_NAME, cv, COLUMN_ADDRESS + "=?", new String[] {addr});
        if (res != 1) {
            Toast.makeText(context, "Failed to update favorite value", Toast.LENGTH_SHORT).show();
        }

        return favorite == 1;
    }
}
