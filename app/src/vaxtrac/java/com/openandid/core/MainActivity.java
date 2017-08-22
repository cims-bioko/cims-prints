package com.openandid.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import data.CommCareContentHandler;

public class MainActivity extends Activity {

    private static boolean gotResult = false;
    private final String TAG = "MainActivity--VT";
    int REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Resume");
        if (!gotResult) {
            Log.i(TAG, "No Result");
            dispatch_intent(getIntent());
            return;
        } else {
            gotResult = false;
            Log.i(TAG, "Has Result, finished.");
            finish();
        }
    }

    private void finish_cancel() {
        Log.i(TAG, "Finishing as Canceled.");
        Intent i = new Intent();
        Bundle b = new Bundle();
        i.putExtra(Controller.ODK_SENTINEL, b);
        setResult(RESULT_CANCELED, i);
    }


    private void dispatch_intent(Intent incoming) {
        String action = incoming.getAction();
        Log.i(TAG, "Started via... " + action);
        if (action.equals("com.openandid.core.SCAN")) {
            if (ScanningActivity.get_total_scans_from_bundle(incoming.getExtras()) > 1) {
                incoming.setClass(this, PipeActivity.class);
                Log.i(TAG, "Starting PipeActivity");
                startActivityForResult(incoming, REQUEST_CODE);
            } else {
                incoming.setClass(this, ScanningActivity.class);
                Log.i(TAG, "Starting ScanningActivity");
                startActivityForResult(incoming, REQUEST_CODE);
            }

        } else if (action.equals("com.openandid.core.ENROLL")) {
            incoming.setClass(this, EnrollActivity.class);
            startActivityForResult(incoming, REQUEST_CODE);
        } else if (action.equals("com.openandid.core.VERIFY")) {

        } else if (action.equals("com.openandid.core.IDENTIFY")) {
            if (Controller.commcare_handler != null) {
                if (CommCareContentHandler.isWorking()) {
                    //Wait for sync to finish w/ prompt
                    Syncronizing sync = new Syncronizing(this, incoming);
                    sync.execute();
                    return;
                }
            }
            incoming.setClass(this, IdentifyActivity.class);
            startActivityForResult(incoming, REQUEST_CODE);
        } else if (action.equals("com.openandid.core.LOAD")) {

        } else if (action.equals("com.openandid.core.PIPE")) {
            incoming.setClass(this, PipeActivity.class);
            startActivityForResult(incoming, REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "Dispatcher Has Activity Result");
        gotResult = true;
        if (resultCode == RESULT_CANCELED) {
            Log.d(TAG, "Got Cancelled flag");
            finish_cancel();
            return;
        } else {
            Log.d(TAG, "Previous Activity Finished");
            try {
                Bundle b = data.getExtras();
                //IF CCODK
                data.putExtra(Controller.ODK_SENTINEL, b);

                Log.i(TAG, "Output from openandidCore");
                for (String k : b.keySet()) {
                    Log.i(TAG, k + ": " + b.getString(k));
                }
            } catch (Exception e) {
                Log.i(TAG, "No output from activity");
                Log.i(TAG, e.toString());
            }
            setResult(resultCode, data);
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //Sets up the NDK file system and utilities on the apps first run
    class Syncronizing extends AsyncTask<Void, Void, Void> {
        private Context oldContext;
        private Intent incoming;

        public Syncronizing(Context context, Intent incoming) {
            super();
            oldContext = context;
            this.incoming = incoming;
        }

        protected void onPreExecute() {
            Toast.makeText(oldContext, "CommCare Sync in Progress...\nPlease Wait", Toast.LENGTH_LONG).show();
            setContentView(R.layout.activity_main_wait);
            TextView t = (TextView) findViewById(R.id.main_blank_txt);
            t.setText("CommCare Sync in Progress...");
        }

        @Override
        protected Void doInBackground(Void... params) {
            while (CommCareContentHandler.isWorking()) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.i(TAG, "Sync Done Found");
            return null;
        }

        protected void onPostExecute(Void res) {
            Toast.makeText(oldContext, "Sync Finished...", Toast.LENGTH_LONG).show();
            dispatch_intent(incoming);
        }

    }
}


