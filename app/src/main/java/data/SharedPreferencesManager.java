package data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SharedPreferencesManager {

    private static final String TAG = "SharedPrefManager";

    private static SharedPreferences preferences = null;

    public SharedPreferencesManager(Context mContext) {
        preferences = mContext.getSharedPreferences(mContext.getApplicationContext().getPackageName(), Context.MODE_PRIVATE);
    }

    public boolean hasPreferences() {
        return preferences.getBoolean("saved_preferences", false);
    }

    public String getCaseType() {
        return preferences.getString("case_type", null);
    }

    public Map<String, String> getTemplateFields() {
        Map<String, String> fields = new HashMap<>();
        Set<String> keys = preferences.getStringSet("template_keys", new HashSet<String>());
        for (String key : keys) {
            fields.put(key, preferences.getString(key, null));
        }
        return fields;
    }

    public void putTemplateFields(String case_type, Map<String, String> fields) {
        Set<String> keys = fields.keySet();
        for (String key : keys) {
            preferences.edit().putString(key, fields.get(key)).apply();
        }
        preferences.edit().putString("case_type", case_type).apply();
        preferences.edit().putStringSet("template_keys", keys).apply();
        preferences.edit().putBoolean("saved_preferences", true).apply();
    }

    public void notify_false_start() {
        Log.i(TAG, "Setting hard_restart flag -> True");
        preferences.edit().putBoolean("hard_restart", true).apply();
    }
}
