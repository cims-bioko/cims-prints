package data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.biometrac.core.CommCareSyncService;
import com.biometrac.core.Controller;
import com.biometrac.core.Engine;

public class CommCareContentHandler {

	private static final String TAG = "CCContentHandler";
	String CASE_LISTING = "content://org.commcare.dalvik.case/casedb/case";
	String CASE_DATA = "content://org.commcare.dalvik.case/casedb/data/";
	Map<String,String> instructions;
	Set<String> cases;
	private static boolean in_sync = false;
	
	public CommCareContentHandler(Map<String, String> instructions) {
		this.instructions = instructions;
		cases = new HashSet<String>();
	}
	
	public void sync(CommCareSyncService mService){
		in_sync = true;
		Log.i(TAG, "Starting Sync");
		Cursor c = mService.getContentResolver().query(Uri.parse(CASE_LISTING), null, null, null, null);
        c.moveToFirst();
        int case_type_pos = c.getColumnIndex("case_type");
        int case_id_pos = c.getColumnIndex("case_id");
        String req_case_type = instructions.get("case_type");
        do{
        	if (c.getString(case_type_pos).equals(req_case_type)){
        		cases.add(c.getString(case_id_pos));
        		Log.i(TAG, "Adding Case: " + c.getString(case_id_pos)); 
        	}else{
        		Log.i(TAG, "Ignoring Case with type: " + c.getString(case_type_pos));
        	}
        }while(c.moveToNext());
        c.close();
        mService.show_message("Loading " + Integer.toString(cases.size()) + " cases from Commcare");
        Set<String> values = new HashSet<String>(instructions.keySet());
        values.remove("case_type");
        
        Map<String,Map<String,String>> case_map = new HashMap<String, Map<String,String>>();
        
        for (String case_id: cases){
        	Log.i(TAG, case_id);
        	Map<String,String> templates = null;
        	c = mService.getContentResolver().query(Uri.parse(CASE_DATA+case_id), null, null, null, null);
            c.moveToFirst();
            int datum_pos = c.getColumnIndex("datum_id");
            int val_pos = c.getColumnIndex("value");
            String key = "";
            do{
            	key = c.getString(datum_pos);
            	if (values.contains(key)){
            			if (templates == null){
            				templates = new HashMap<String, String>();
            			}
            			String value = c.getString(val_pos);
            			templates.put(key, value);
            			Log.i(TAG, "key:" + key + " | v: " + value) ;	
        		}
            }while(c.moveToNext());
            if (templates != null){
            	case_map.put(case_id, templates);
            }
            c.close();

        }
        
        mService.show_message("Translating Templates");
        Controller.mEngine.cache_candidates(case_map);
        if (!Engine.is_ready){
        	Log.i(TAG, "Engine is busy, waiting.");
        }
        while(!Engine.is_ready){
        	try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        Log.i(TAG, "Finished Sync");
        mService.show_message("Service Ready.");
		in_sync = false;
	}

    public Map<String,String> getInstructions(){
        if (this.instructions!= null){
            return this.instructions;
        }
        return null;
    }

	public static boolean isWorking() {
		return in_sync;
	}

	public static void died(){
		in_sync = false;
	}
}
