package data;

import java.util.HashMap;
import java.util.Map;

import com.openandid.core.Engine;

import bmtafis.simple.Person;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class LocalDatabaseHandler {

	private Context mContext;
	private SQLiteDatabase mDB;
	private LocalDatabaseHelper mDBHelper;
	private static final String TAG = "LocalDatabaseHandler";
	
	//---------------DB Definition-----------------------
	private static final String USER_TABLE = "Users";
	private static final HashMap<String, String> USER_MAP = new HashMap<String,String>(){{
		put("USER_ID","userid");
		put("USER_OBJECT","userobject");
	}};
	
	public LocalDatabaseHandler(Context context){
		mContext = context;
		mDBHelper = new LocalDatabaseHelper(mContext);  
	    mDB = mDBHelper.getWritableDatabase();
	}
	
	public boolean save_user_to_db(String userid ,String obj){
		ContentValues cv = new ContentValues();
		cv.put("userid", userid);
		cv.put("userobject", obj);
		try{
			mDB.insertOrThrow(USER_TABLE, null, cv);
		}catch(Exception e){
			Log.i(TAG, "Couldn't insert asset into table, update required?");
			try{
				//Updating
				mDB.replace(USER_TABLE, null, cv);
					return true; //Updated to new revision
			}catch(Exception e2){
				Log.i(TAG, "Could not save or update");
				e2.printStackTrace();
				return false;
			}
		}
		return true; //First time insert
	}
	
	public Map<String, Person> get_all_users(){
		Cursor c = select_all_users();
		Map output = new HashMap<String, Person>(); 
		int count = c.getCount();
		if (count ==0){
			Log.i(TAG,"No users in DB");
			return new HashMap<String, Person>(); 
		}
		else{
			Log.i(TAG,Integer.toString(count) +" users in DB");
		}
		int id_pos = c.getColumnIndex("userid");
		int user_pos = c.getColumnIndex("userobject");
		c.moveToFirst();
		do{
			output.put(c.getString(id_pos), Engine.json_string_to_person(c.getString(id_pos),c.getString(user_pos)));
		}while(c.moveToNext()==true);
		return output;
		
	}
	
	
	public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
	
//PRIVATE CURSORS
	
	private Cursor select_user_by_id(String userid){
		String[] to_pull = USER_MAP.values().toArray(new String[0]);
		String[] parse_on = {userid};
		Cursor cursor = mDB.query(USER_TABLE, to_pull, "userid = ?", parse_on, null, null, null);
		return cursor;
	}
	private Cursor select_all_users(){
		String[] queue_pull = USER_MAP.values().toArray(new String[0]);
		Cursor cursor = mDB.query(USER_TABLE, queue_pull, null, null, null, null, null);
		return cursor;
	}

}
