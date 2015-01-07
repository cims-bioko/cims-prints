package com.biometrac.core;

import java.util.Map;
import java.util.Random;

import data.CommCareContentHandler;
import data.LocalDatabaseHandler;
import data.SharedPreferencesManager;
import logic.HostUsbManager;
import logic.Scanner;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

public class Controller extends Application {

	private static final String TAG = "ApplicationController";
	public static Scanner mScanner = null;
	public static UsbDevice mDevice;
	public static UsbManager mUsbManager;
	public static HostUsbManager mHostUsbManager;
	
	
	public static Engine mEngine = null;
	public static Random mRandom = new Random();
	public static LocalDatabaseHandler db_handle = null;
	public static CommCareContentHandler commcare_handler = null;
	public static SharedPreferencesManager preference_manager = null;
	
	private static Context context;

    public void onCreate(){
        super.onCreate();
        Controller.context = getApplicationContext();
        db_handle = new LocalDatabaseHandler(context);
        mEngine = new Engine(Controller.context, 27.0f);
        preference_manager = new SharedPreferencesManager(context);
        //Start foreground service
        Intent i = new Intent(context, PersistenceService.class);
        context.startService(i);
        sync_commcare_default();
        
        //db_help = new LocalDatabaseHelper(context);
        
    }

    public static Context getAppContext() {
        return Controller.context;
    }

    public static void sync_commcare_default(){
    	if(preference_manager.has_preferences()){
        	Map<String,String> data= preference_manager.get_template_fields();
        	data.put("case_type", preference_manager.get_case_type());
        	sync_commcare(data);
        }else{
        	Toast.makeText(context, "CommCare Setting are NOT SET. Check BMTCore.", Toast.LENGTH_LONG);
        }
    }
    
    public static void sync_commcare(Map<String, String> output){
    	if (CommCareSyncService.is_ready){
    		Log.i(TAG, "CC Sync Start");
    		commcare_handler = new CommCareContentHandler(output);
        	Intent i = new Intent(context, CommCareSyncService.class);
        	context.startService(i);	
    	}else{
    		Log.i(TAG, "Delayed sync queued");
    		CommCareSyncService.re_sync = true;
    	}
    	
	}
    
    public void test(){
    	Log.i(TAG, "Received Message");
    }
    
    public static void kill_all(){
    	Log.i(TAG, "SHUT IT DOWN!");
    	preference_manager.notify_false_start();
    	context.stopService(new Intent(context, PersistenceService.class));
    	context.stopService(new Intent(context, NotificationReceiver.class));
    	try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	//android.os.Process.killProcess(android.os.Process.myPid());
    	System.exit(0);

    }
    
}
