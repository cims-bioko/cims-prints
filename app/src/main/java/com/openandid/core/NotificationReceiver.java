package com.openandid.core;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class NotificationReceiver extends Service {

    public static final String TAG = "NotificationReceiver";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        switch (action) {
            case "KILL":
                killAll();
                break;
            case "CRASH":
                crash();
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void killAll() {
        Controller.killAll();
        stopSelf();
    }

    private void crash() {
        Controller.crash();
        stopSelf();
    }
}
