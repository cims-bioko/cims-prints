package data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SharedPreferencesManager {

	private static final String TAG = "SharedPrefManager";
	private Context mContext;
	public static SharedPreferences preferences = null;
	
	public SharedPreferencesManager(Context mContext) {
		this.mContext = mContext;
		preferences = mContext.getSharedPreferences("com.openandid.core", Context.MODE_PRIVATE);
	}
	
	public boolean has_preferences(){
		return preferences.getBoolean("saved_preferences", false);
		
	}
	
	public String get_case_type(){
		return preferences.getString("case_type", null);
	}
	
	public Map<String,String> get_template_fields(){
		
		Map<String,String>fields = new HashMap<String, String>();
		Set<String> keys = preferences.getStringSet("template_keys", new HashSet<String>());
		for(String key: keys){
			fields.put(key, preferences.getString(key, null));
		}
		return fields;
	}
	
	public void put_template_fields(String case_type, Map<String,String>fields){
		Set<String> keys = fields.keySet();
		for(String key: keys){
			preferences.edit().putString(key, fields.get(key)).apply();
		}
		preferences.edit().putString("case_type", case_type).apply();
		preferences.edit().putStringSet("template_keys", keys).apply();
		preferences.edit().putBoolean("saved_preferences", true).apply();
		
	}

	public boolean is_false_start() {
		Boolean was_reset = preferences.getBoolean("hard_restart", false);
		if (was_reset){
			preferences.edit().putBoolean("hard_restart", false).apply();
			return true;
		}
		return false;
	}
	
	public void notify_false_start(){
		Log.i(TAG, "Setting hard_restart flag -> True");
		preferences.edit().putBoolean("hard_restart", true).apply();
	}
	
	private void acknowledge_false_start(){
		preferences.edit().putBoolean("restart_acknowledge", true).apply();
	}
	
}
