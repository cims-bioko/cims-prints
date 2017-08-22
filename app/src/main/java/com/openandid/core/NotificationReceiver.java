package com.openandid.core;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class NotificationReceiver extends Service {

    public static final String TAG = "NotificationReceiver";

    public NotificationReceiver() {

    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        Log.d(TAG, "onStartCommand " + intent.getAction());
        if (action.equals("KILL")) {
            kill_all();
        } else if (action.equals("CRASH")) {
            crash();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public Intent getIntent(PendingIntent pendingIntent) throws IllegalStateException {
        try {
            Method getIntent = PendingIntent.class.getDeclaredMethod("getIntent");
            return (Intent) getIntent.invoke(pendingIntent);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private void kill_all() {
        Log.i(TAG, "Got Kill Switch");
        Controller.kill_all();
        stopSelf();
        return;
    }

    private void crash() {
        Log.i(TAG, "Got Crash Switch");
        Controller.crash();
        stopSelf();
        return;
    }

    class CleanUp extends AsyncTask<Void, Void, Void> {
        private Context oldContext;

        public CleanUp(Context context) {
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
