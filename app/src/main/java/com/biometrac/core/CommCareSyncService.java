package com.biometrac.core;

import data.CommCareContentHandler;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.Notification.Builder;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class CommCareSyncService extends Service {

	private Context mContext;
	private int NOTE_ID;
	private final String TAG = "CCSyncService";
	
	public static boolean is_ready = true;
	public static boolean re_sync = false;
	
	public CommCareSyncService() {
		// TODO Auto-generated constructor stub
	}

	
	@Override
	public void onCreate(){
		NOTE_ID = PersistenceService.NOTE_ID;
		mContext = this;
		show_message("Staring CommCare Load");
		sync(); 
	}
	
	private Runnable sync_thread(){
		return new Runnable(){
			public void run(){
				while(true){
					is_ready = false;
					CommCareContentHandler handler = Controller.commcare_handler;
					try{
						handler.sync((CommCareSyncService) mContext);
					}catch (Exception e){
						Log.i(TAG, "Couldn't Sync to CC");
						show_message("Couldn't sync with Commcare.");
                        Log.e(TAG,e.getMessage());
                        e.printStackTrace();
						handler.died();
						Log.i(TAG, "Sync Failed.");
						stopSelf();
					}
					
					int c = 0;
					while(handler.isWorking()){
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					//show_message("CommCare Sync Complete");
					if (!re_sync){
						Log.i(TAG, "Finished Sync");
						is_ready = true;
						stopSelf();	
						break;
					}else{
						Log.i(TAG, "New Data ping since start of sync. ReSyncing.");
					}
					
				}
				
			}
		};
	}
	
	private void commcare_error(){
		Toast.makeText(mContext, "Couldn't sync to CommCare", Toast.LENGTH_SHORT).show();
	}
	
	public void sync(){
		Thread mythread = new Thread(sync_thread());
		mythread.start();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public void show_message(String message){
		Notification n = get_notification(message);
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(NOTE_ID, n);
	}
	
	private Notification get_notification(String message){
		Builder builder = new Builder(getApplicationContext());
		builder.setContentTitle("BiometracCore");
		builder.setContentText(message);
		builder.setSmallIcon(R.drawable.bmt_icon);
		Intent notificationIntent = new Intent(this, PersistenceService.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		builder.setContentIntent(pendingIntent);
		Notification n = builder.build();
		return n;
	}

}
