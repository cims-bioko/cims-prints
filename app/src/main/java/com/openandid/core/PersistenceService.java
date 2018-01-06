package com.openandid.core;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.github.cimsbioko.cimsprints.R;

public class PersistenceService extends Service {

    public static final int NOTIFICATION_ID = 70503;

    public boolean foreground = false;

    @Override
    public void onCreate() {
        showForeground();
    }

    private void showForeground() {
        startForeground(NOTIFICATION_ID, buildNotification("Service Started."));
    }

    @Override
    public void onDestroy() {
        stop();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public Notification buildNotification(String message) {

        foreground = true;

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

        if (sharedPref.getBoolean("acra.verbose", false)) {
            Intent crashIntent = new Intent(this, NotificationReceiver.class);
            crashIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            crashIntent.setAction("CRASH");
            builder.addAction(android.R.drawable.ic_delete, "Send Error Report",
                    PendingIntent.getService(this, 0, crashIntent, 0));
        }

        Intent persistIntent = new Intent(this, PersistenceService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, persistIntent, 0);
        builder.setContentIntent(pendingIntent);

        return builder.build();
    }

    private void stop() {
        if (foreground) {
            foreground = false;
            stopForeground(true);
        }
    }
}
