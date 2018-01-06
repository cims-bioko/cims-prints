package com.openandid.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.github.cimsbioko.cimsprints.R;

import data.CommCareContentHandler;

import static com.openandid.core.Constants.ENROLL_ACTION;
import static com.openandid.core.Constants.IDENTIFY_ACTION;
import static com.openandid.core.Constants.ODK_INTENT_BUNDLE_KEY;
import static com.openandid.core.Constants.PIPE_ACTION;
import static com.openandid.core.Constants.SCAN_ACTION;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity--VT";

    private static final int REQUEST_CODE = 1;

    private static boolean gotResult = false;

    @Override
    protected void onResume() {
        super.onResume();
        if (!gotResult) {
            dispatchIntent(getIntent());
        } else {
            gotResult = false;
            finish();
        }
    }

    private void finishCancel() {
        Intent resultIntent = new Intent();
        Bundle bundle = new Bundle();
        resultIntent.putExtra(ODK_INTENT_BUNDLE_KEY, bundle);
        setResult(RESULT_CANCELED, resultIntent);
    }

    private void dispatchIntent(Intent incoming) {

        String action = incoming.getAction();

        switch (action) {
            case SCAN_ACTION:
                if (ScanningActivity.getScanCount(incoming.getExtras()) > 1) {
                    incoming.setClass(this, PipeActivity.class);
                } else {
                    incoming.setClass(this, ScanningActivity.class);
                }
                break;
            case ENROLL_ACTION:
                incoming.setClass(this, EnrollActivity.class);
                break;
            case IDENTIFY_ACTION:
                if (Controller.commCareHandler != null) {
                    if (CommCareContentHandler.isInSync()) {
                        SyncTask sync = new SyncTask(this, incoming);
                        sync.execute();
                        return;
                    }
                }
                incoming.setClass(this, IdentifyActivity.class);
                break;
            case PIPE_ACTION:
                incoming.setClass(this, PipeActivity.class);
                break;
        }

        startActivityForResult(incoming, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        gotResult = true;
        if (resultCode == RESULT_CANCELED) {
            finishCancel();
        } else {
            try {
                Bundle extras = data.getExtras();
                data.putExtra(ODK_INTENT_BUNDLE_KEY, extras);
            } catch (Exception e) {
                Log.i(TAG, "No output from activity", e);
            }
            setResult(resultCode, data);
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private class SyncTask extends AsyncTask<Void, Void, Void> {

        private Context oldContext;
        private Intent incoming;

        SyncTask(Context context, Intent incoming) {
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
            while (CommCareContentHandler.isInSync()) {
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
            dispatchIntent(incoming);
        }
    }
}


