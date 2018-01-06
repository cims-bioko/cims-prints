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

import com.github.cimsbioko.cimsprints.R;

import static com.openandid.core.Constants.KILL_ACTION;

public class CleanupActivity extends Activity {

    private static final String TAG = "CleanUp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spinner_view);
        new CleanupTask(this).execute();
    }

    private void killAll() {
        Controller.killAll();
        finish();
    }

    private class CleanupTask extends AsyncTask<Void, Void, Void> {

        private Context context;

        CleanupTask(Context context) {
            super();
            this.context = context;
        }

        protected void onPreExecute() {
            Toast.makeText(context, "Cleaning Up", Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (RunningAppProcessInfo pid : am.getRunningAppProcesses()) {
                am.killBackgroundProcesses(pid.processName);
            }

            Intent i = new Intent();
            i.setAction(KILL_ACTION);
            sendBroadcast(i);

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Log.d(TAG, "interrupted during sleep");
            }

            return null;
        }

        protected void onPostExecute(Void res) {
            Toast.makeText(context, "Cleaning Finished...", Toast.LENGTH_LONG).show();
            killAll();
        }
    }
}
