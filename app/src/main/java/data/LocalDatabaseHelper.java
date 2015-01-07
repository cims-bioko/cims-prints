package data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LocalDatabaseHelper extends SQLiteOpenHelper {

	private static final String LOG_ID = "LocalDatabaseHelper";
    private static final String DATABASE_NAME = "VTDEMODB";
    private static final int DATABASE_VERSION = 2;
	
    private static final String TABLE_CREATE_USERS = "create table Users (userid text primary key, userobject text);";

	public LocalDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		Log.i(LOG_ID,"Creating new Tables");
		database.execSQL(TABLE_CREATE_USERS);
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		Log.i(LOG_ID, "SQLLite Attempted to Upgrade...");
	}

}
