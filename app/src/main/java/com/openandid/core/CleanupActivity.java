package com.openandid.core;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class CleanupActivity extends Activity {

    private static final String TAG = "CleanUp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "CleanUp Started");
        setContentView(R.layout.spinner_view);
        CleanUpRunner clean = new CleanUpRunner(this);
        clean.execute();
        super.onCreate(savedInstanceState);
    }

    private void kill_all() {
        Log.i(TAG, "Got Kill Switch");
        Controller.kill_all();
        finish();
        return;
    }

    class CleanUpRunner extends AsyncTask<Void, Void, Void> {
        private Context oldContext;

        public CleanUpRunner(Context context) {
            super();
            oldContext = context;
        }

        protected void onPreExecute() {
            Toast.makeText(oldContext, "Cleaning Up", Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            ActivityManager am = (ActivityManager) oldContext.getSystemService(Context.ACTIVITY_SERVICE);

            for (RunningAppProcessInfo pid : am.getRunningAppProcesses()) {
                am.killBackgroundProcesses(pid.processName);
            }
            Intent i = new Intent();
            i.setAction("com.openandid.core.KILL");
            sendBroadcast(i);

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Log.d(TAG, "interrupted during sleep");
            }
            return null;
        }

        protected void onPostExecute(Void res) {
            Toast.makeText(oldContext, "Cleaning Finished...", Toast.LENGTH_LONG).show();
            kill_all();
        }

    }


}
