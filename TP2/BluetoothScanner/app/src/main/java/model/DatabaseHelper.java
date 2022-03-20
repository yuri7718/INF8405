package model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;

public class DatabaseHelper extends SQLiteOpenHelper {

    private final Context context;

    public static final String DATABASE_NAME = "BluetoothDevices.db";
    public static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "detected_devices";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_LAT = "lat";
    private static final String COLUMN_LNG = "lng";
    private static final String COLUMN_TYPE = "type";
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

    void addDevice(String address, String name, double lat, double lng, int type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_ADDRESS, address);
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_LAT, lat);
        cv.put(COLUMN_LNG, lng);
        cv.put(COLUMN_TYPE, type);
        cv.put(COLUMN_FAVORITE, 0);

        long result = db.replace(TABLE_NAME, null, cv);
        if (result == -1) {
            Toast.makeText(context, "Failed to add device to database", Toast.LENGTH_SHORT).show();
        }

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

    public boolean updateFavorite(String addr) {
        String query = "SELECT " + COLUMN_FAVORITE + " FROM " + TABLE_NAME + " WHERE " + COLUMN_ADDRESS + "='" + addr +"';";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        int favorite = -1;
        if (db != null) {
            cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                favorite = cursor.getInt(0);
            }
        }
        if (favorite == -1) return false;

        db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        favorite = favorite == 0 ? 1: 0;    // toggle favorite value
        cv.put(COLUMN_FAVORITE, favorite);
        int res = db.update(TABLE_NAME, cv, COLUMN_ADDRESS + "=?", new String[] {addr});
        return favorite == 1;
    }
}
