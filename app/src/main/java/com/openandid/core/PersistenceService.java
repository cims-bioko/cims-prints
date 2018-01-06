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

    @Override
    public void onCreate() {
        startForeground(NOTIFICATION_ID, buildNotification(getString(R.string.service_running)));
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public Notification buildNotification(String message) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        boolean allowKill = sharedPref.getBoolean("bmt.allowkill", false);

        Builder builder = new Builder(getApplicationContext());
        builder.setContentTitle(getText(R.string.app_name));
        builder.setContentText(message);
        builder.setSmallIcon(R.drawable.bmt_icon);

        if (allowKill) {
            Intent killIntent = new Intent(this, NotificationReceiver.class);
            killIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            killIntent.setAction("KILL");
            builder.addAction(android.R.drawable.ic_input_delete, getText(R.string.stop_service),
                    PendingIntent.getService(this, 0, killIntent, 0));
        }

        Intent persistIntent = new Intent(this, PersistenceService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, persistIntent, 0);
        builder.setContentIntent(pendingIntent);

        return builder.build();
    }

}
