package com.openandid.core;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Map;

import data.CommCareContentHandler;

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
    public void onCreate() {
        NOTE_ID = PersistenceService.NOTE_ID;
        mContext = this;
        show_message("Staring CommCare Load");
        sync();
    }

    private Runnable sync_thread() {
        return new Runnable() {
            public void run() {
                while (true) {
                    is_ready = false;
                    CommCareContentHandler handler = Controller.commcare_handler;
                    try {
                        handler.sync((CommCareSyncService) mContext);
                    } catch (Exception e) {
                        Log.i(TAG, "Couldn't Sync to CC");
                        show_message("Couldn't sync with Commcare.");
                        Log.e(TAG, String.format("%s", e.getMessage()));
                        try {
                            if (handler != null) {
                                Map<String, String> cc_instructions = handler.getInstructions();
                                Log.i(TAG, "Commcare Instructions");
                                if (cc_instructions != null) {
                                    for (String key : cc_instructions.keySet()) {
                                        Log.i(TAG, String.format("%s | %s", key, cc_instructions.get(key)));
                                    }
                                }
                            }
                        } catch (Exception e1) {
                            Log.e(TAG, "Couldn't report CC Instruction set");
                        }
                        if (e != null) {
                            e.printStackTrace();
                        }
                        CommCareContentHandler.died();
                        Log.i(TAG, "Sync Failed.");
                        stopSelf();
                    }

                    int c = 0;
                    while (CommCareContentHandler.isWorking()) {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    //show_message("CommCare Sync Complete");
                    if (!re_sync) {
                        Log.i(TAG, "Finished Sync");
                        is_ready = true;
                        stopSelf();
                        break;
                    } else {
                        Log.i(TAG, "New Data ping since start of sync. ReSyncing.");
                    }

                }

            }
        };
    }

    private void commcare_error() {
        Toast.makeText(mContext, "Couldn't sync to CommCare", Toast.LENGTH_SHORT).show();
    }

    public void sync() {
        Thread mythread = new Thread(sync_thread());
        mythread.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }


    public void show_message(String message) {
        Notification n = get_notification(message);
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTE_ID, n);
    }

    private Notification get_notification(String message) {
        Intent kill = new Intent(this, NotificationReceiver.class);
        kill.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        kill.setAction("KILL");
        Intent crash = new Intent(this, NotificationReceiver.class);
        crash.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        crash.setAction("CRASH");
        PendingIntent kill_intent = PendingIntent.getService(this, 0, kill, 0);
        PendingIntent crash_intent = PendingIntent.getService(this, 0, crash, 0);
        Builder builder = new Builder(getApplicationContext());
        builder.setContentTitle("OpenANDIDCore");
        builder.setContentText(message);
        builder.setSmallIcon(R.drawable.bmt_icon);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPref.getBoolean("bmt.allowkill", false)) {
            builder.addAction(android.R.drawable.ic_input_delete, "Stop Service", kill_intent);
        }
        if (sharedPref.getBoolean("acra.verbose", false)) {
            builder.addAction(android.R.drawable.ic_delete, "Send Error Report", crash_intent);
        }
        //builder.addAction(R.drawable.error_icon, "Kill BMT", kill_intent);
        Intent notificationIntent = new Intent(this, PersistenceService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        builder.setContentIntent(pendingIntent);
        Notification n = builder.build();
        return n;
    }

}
