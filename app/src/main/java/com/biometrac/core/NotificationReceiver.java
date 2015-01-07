package com.biometrac.core;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;


public class NotificationReceiver extends Service {

	public static final String TAG = "NotificationReceiver";
	
	public NotificationReceiver() {
		
	}
	
	@Override
	public void onCreate() {
		
		/*
		Intent i = new Intent(this, CleanupActivity.class);
		Log.i(TAG, "Starting CleanUp");
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(i);
		CleanUp c = new CleanUp(getApplicationContext());
		c.execute();
		*/
		kill_all();
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void kill_all(){
		Log.i(TAG, "Got Kill Switch");
		Controller.kill_all();
		stopSelf();
		return;
	}
	
	class CleanUp extends AsyncTask<Void, Void, Void> {
		private Context oldContext;
		
		public CleanUp(Context context){
			super();
			oldContext = context;
		}
		protected void onPreExecute() {
			//Toast.makeText(oldContext, "Cleaning Up", Toast.LENGTH_LONG).show();
		}
		@Override
		protected Void doInBackground(Void... params) {
	    	ActivityManager am = (ActivityManager) oldContext.getSystemService(Context.ACTIVITY_SERVICE);

	    	for (RunningAppProcessInfo pid : am.getRunningAppProcesses()) {
	    	    am.killBackgroundProcesses(pid.processName);
	    	}
			return null;
	    }
		protected void onPostExecute(Void res) {
			//Toast.makeText(oldContext, "Cleaning Finished...", Toast.LENGTH_LONG).show();
			kill_all();
		}

	}
	
}
