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

import com.github.cimsbioko.cimsprints.R;

import data.CommCareContentHandler;

public class CommCareSyncService extends Service {

    private static final String TAG = "CCSyncService";
    private static final int NOTIFICATION_ID = 70503;

    private static boolean resync = false;
    private static boolean ready = true;

    private Context mContext;

    @Override
    public void onCreate() {
        mContext = this;
        showNotification("Staring CommCare Load");
        sync();
    }

    public void sync() {
        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    ready = false;
                    CommCareContentHandler handler = Controller.commCareHandler;
                    try {
                        handler.sync((CommCareSyncService) mContext);
                    } catch (Exception e) {
                        Log.e(TAG, "Couldn't Sync to CC", e);
                        showNotification("Couldn't sync with Commcare.");
                        CommCareContentHandler.setInSync(false);
                        stopSelf();
                    }

                    while (CommCareContentHandler.isInSync()) {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            Log.d(TAG, "interrupted during sleep");
                        }
                    }

                    if (!resync) {
                        Log.i(TAG, "Finished Sync");
                        ready = true;
                        stopSelf();
                        break;
                    } else {
                        Log.i(TAG, "New Data ping since start of sync. ReSyncing.");
                    }
                }
            }
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void showNotification(String message) {
        Notification n = buildNotification(message);
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID, n);
    }

    private Notification buildNotification(String message) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        Builder builder = new Builder(getApplicationContext());
        builder.setContentTitle("OpenANDIDCore");
        builder.setContentText(message);
        builder.setSmallIcon(R.drawable.bmt_icon);

        if (sharedPref.getBoolean("bmt.allowkill", false)) {
            Intent killIntent = new Intent(this, NotificationReceiver.class);
            killIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            killIntent.setAction("KILL");
            builder.addAction(android.R.drawable.ic_input_delete, "Stop Service",
                    PendingIntent.getService(this, 0, killIntent, 0));
        }

        return builder.build();
    }

    public static boolean isReady() {
        return ready;
    }

    public static void setResync(boolean resync) {
        CommCareSyncService.resync = resync;
    }
}
