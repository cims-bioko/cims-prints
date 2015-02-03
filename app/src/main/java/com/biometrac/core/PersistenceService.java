package com.biometrac.core;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class PersistenceService extends Service {

	private boolean is_foreground =false;
	public static int NOTE_ID = 70503;
	
	public PersistenceService() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(){
		show_foreground();
	}
	
	private void show_foreground(){
		startForeground(NOTE_ID, get_notification("Service Started."));
	}
	
	@Override
	  public void onDestroy() {
	    stop();
	  }
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void show_message(String message){
		Notification n = get_notification(message);
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(NOTE_ID, n);
	}
	
	private Notification get_notification(String message){
		is_foreground=true;
		
		Intent intent = new Intent(this, NotificationReceiver.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	    PendingIntent kill_intent = PendingIntent.getService(this, 0, intent, 0);
	    Builder builder = new Builder(getApplicationContext());
		builder.setContentTitle("BiometracCore");
		builder.setContentText(message);
		builder.setSmallIcon(R.drawable.bmt_icon);
		//builder.setContentIntent(kill_intent);
		
		//builder.addAction(R.drawable.error_icon, "Stop Service", kill_intent);
		builder.addAction(android.R.drawable.ic_input_delete, "Stop Service", kill_intent);
		//builder.addAction(R.drawable.error_icon, "Kill BMT", kill_intent);
		Intent notificationIntent = new Intent(this, PersistenceService.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		builder.setContentIntent(pendingIntent);
		Notification n = builder.build();
		return n;
	}
	
	private void stop() {
	    if (is_foreground) {
	      is_foreground=false;
	      stopForeground(true);
	    }
	  }

}
