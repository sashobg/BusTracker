package bg.sasho.bustracker.database;

import android.database.Cursor;
import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper  {

    private static String DB_NAME = "sofia.db";
    private static String DB_PATH = "";
    private static String TABLE_SOFIA_NAME = "SOF_STAT";
    private static final int DB_VERSION = 1;

    private SQLiteDatabase mDataBase;
    private final Context mContext;
    private boolean mNeedUpdate = false;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        if (android.os.Build.VERSION.SDK_INT >= 17)
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        else
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        this.mContext = context;

        copyDataBase();

        this.getReadableDatabase();
    }

    public void updateDataBase() throws IOException {
        if (mNeedUpdate) {
            File dbFile = new File(DB_PATH + DB_NAME);
            if (dbFile.exists())
                dbFile.delete();

            copyDataBase();

            mNeedUpdate = false;
        }
    }

    private boolean checkDataBase() {
        File dbFile = new File(DB_PATH + DB_NAME);
        return dbFile.exists();
    }

    private void copyDataBase() {
        if (!checkDataBase()) {
            this.getReadableDatabase();
            this.close();
            try {
                copyDBFile();
            } catch (IOException mIOException) {
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }

    private void copyDBFile() throws IOException {
        InputStream mInput = mContext.getAssets().open(DB_NAME);
        //InputStream mInput = mContext.getResources().openRawResource(R.raw.info);
        OutputStream mOutput = new FileOutputStream(DB_PATH + DB_NAME);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0)
            mOutput.write(mBuffer, 0, mLength);
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    public boolean openDataBase() throws SQLException {
        mDataBase = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        return mDataBase != null;
    }

    @Override
    public synchronized void close() {
        if (mDataBase != null)
            mDataBase.close();
        super.close();
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(BusStop.CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + BusStop.TABLE_NAME);

        // Create tables again
        onCreate(db);
        if (newVersion > oldVersion)
            mNeedUpdate = true;
    }

    public long insertBusStop(String name, String number, String officialName, String lat, String lng) {
        // get writable database as we want to write data


        SQLiteDatabase dbread = this.getReadableDatabase();
        Cursor cursor = dbread.query(TABLE_SOFIA_NAME,
                new String[]{"STAT_NAME", "STAT_LATITUDE", "STAT_LONGITUDE"},
                "STAT_NUMBER" + "=?",
                new String[]{String.valueOf(Integer.parseInt(number))}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        officialName = cursor.getString(cursor.getColumnIndex("STAT_NAME"));
        lat = cursor.getString(cursor.getColumnIndex("STAT_LATITUDE"));
        lng = cursor.getString(cursor.getColumnIndex("STAT_LONGITUDE"));



        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        dbread.close();
        ContentValues values = new ContentValues();
        SQLiteDatabase db = this.getWritableDatabase();
        values.put(BusStop.COLUMN_NAME, name);
        values.put(BusStop.COLUMN_NUMBER, number);
        values.put(BusStop.COLUMN_OFFNAME, officialName);
        values.put(BusStop.COLUMN_LAT, lat);
        values.put(BusStop.COLUMN_LNG, lng);

        // insert row
        long id = db.insert(BusStop.TABLE_NAME, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }

    public BusStop getBusStopById(long id) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(BusStop.TABLE_NAME,
                new String[]{BusStop.COLUMN_ID, BusStop.COLUMN_NAME, BusStop.COLUMN_NUMBER, BusStop.COLUMN_OFFNAME, BusStop.COLUMN_LAT, BusStop.COLUMN_LNG, BusStop.COLUMN_TIMESTAMP},
                BusStop.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        BusStop busStop = new BusStop(
                cursor.getInt(cursor.getColumnIndex(BusStop.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(BusStop.COLUMN_NAME)),
                cursor.getString(cursor.getColumnIndex(BusStop.COLUMN_NUMBER)),
                cursor.getString(cursor.getColumnIndex(BusStop.COLUMN_OFFNAME)),
                cursor.getString(cursor.getColumnIndex(BusStop.COLUMN_LAT)),
                cursor.getString(cursor.getColumnIndex(BusStop.COLUMN_LNG)),
                cursor.getString(cursor.getColumnIndex(BusStop.COLUMN_TIMESTAMP)));

        // close the db connection
        cursor.close();

        return busStop;
    }

    public BusStop getBusStopByNumber(String number) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(BusStop.TABLE_NAME,
                new String[]{BusStop.COLUMN_ID, BusStop.COLUMN_NAME, BusStop.COLUMN_NUMBER, BusStop.COLUMN_OFFNAME, BusStop.COLUMN_LAT, BusStop.COLUMN_LNG, BusStop.COLUMN_TIMESTAMP},
                BusStop.COLUMN_NUMBER + "=?",
                new String[]{String.valueOf(number)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        BusStop busStop = new BusStop(
                cursor.getInt(cursor.getColumnIndex(BusStop.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(BusStop.COLUMN_NAME)),
                cursor.getString(cursor.getColumnIndex(BusStop.COLUMN_NUMBER)),
                cursor.getString(cursor.getColumnIndex(BusStop.COLUMN_OFFNAME)),
                cursor.getString(cursor.getColumnIndex(BusStop.COLUMN_LAT)),
                cursor.getString(cursor.getColumnIndex(BusStop.COLUMN_LNG)),
                cursor.getString(cursor.getColumnIndex(BusStop.COLUMN_TIMESTAMP)));

        // close the db connection
        cursor.close();

        return busStop;
    }

    public List<BusStop> getAllBusStops() {
        List<BusStop> busStops = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + BusStop.TABLE_NAME + " ORDER BY " +
                BusStop.COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                BusStop busStop = new BusStop();
                busStop.setId(cursor.getInt(cursor.getColumnIndex(BusStop.COLUMN_ID)));
                busStop.setName(cursor.getString(cursor.getColumnIndex(BusStop.COLUMN_NAME)));
                busStop.setNumber(cursor.getString(cursor.getColumnIndex(BusStop.COLUMN_NUMBER)));
                busStop.setOfficialName(cursor.getString(cursor.getColumnIndex(BusStop.COLUMN_OFFNAME)));
                busStop.setLat(cursor.getString(cursor.getColumnIndex(BusStop.COLUMN_LAT)));
                busStop.setLng(cursor.getString(cursor.getColumnIndex(BusStop.COLUMN_LNG)));
                busStop.setTimestamp(cursor.getString(cursor.getColumnIndex(BusStop.COLUMN_TIMESTAMP)));
                busStops.add(busStop);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        return busStops;
    }

    public int getBusStopsCount() {
        String countQuery = "SELECT  * FROM " + BusStop.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();


        // return count
        return count;
    }

    public int updateBusStop(BusStop busStop) {


        SQLiteDatabase dbread = this.getReadableDatabase();
        Cursor cursor = dbread.query(TABLE_SOFIA_NAME,
                new String[]{"STAT_NAME", "STAT_LATITUDE", "STAT_LONGITUDE"},
                "STAT_NUMBER" + "=?",
                new String[]{String.valueOf(Integer.parseInt(busStop.getNumber()))}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

       String officialName = cursor.getString(cursor.getColumnIndex("STAT_NAME"));
        String lat = cursor.getString(cursor.getColumnIndex("STAT_LATITUDE"));
        String lng = cursor.getString(cursor.getColumnIndex("STAT_LONGITUDE"));



        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        dbread.close();

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(BusStop.COLUMN_NAME, busStop.getName());
        values.put(BusStop.COLUMN_NUMBER, busStop.getNumber());
        values.put(BusStop.COLUMN_OFFNAME, officialName);
        values.put(BusStop.COLUMN_LAT, lat);
        values.put(BusStop.COLUMN_LNG, lng);
        // updating row
        return db.update(BusStop.TABLE_NAME, values, BusStop.COLUMN_ID + " = ?",
                new String[]{String.valueOf(busStop.getId())});
    }

    public void deleteBusStop(BusStop busStop) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(BusStop.TABLE_NAME, BusStop.COLUMN_ID + " = ?",
                new String[]{String.valueOf(busStop.getId())});
        db.close();
    }
}
