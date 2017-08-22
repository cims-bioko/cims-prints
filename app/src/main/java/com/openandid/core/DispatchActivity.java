package com.openandid.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class DispatchActivity extends Activity {

    public static final String TAG = "DispatchActivity";
    private static boolean isCanceled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isCanceled = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isCanceled) {
            finishCancel();
            return;
        }
        try {
            doNextIntent();
            return;
        } catch (Exception e) {
            Log.e(TAG, "PipeSession Had no intent to dispatch", e);
        }
        try {
            String sessionID = getIntent().getExtras().getString("sessionID");
            if (PipeSessionManager.isNewSession(sessionID)) {
                Log.d(TAG, "New Session Found with ID " + sessionID);
                PipeSessionManager.registerSession(sessionID, getIntent().getAction(), getIntent().getExtras());
                doNextIntent();
                return;
            } else {
                Log.d(TAG, "Session Exists with ID " + sessionID);
                finishSession();
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "Couldn't check sessionID | " + e.toString());
        }


    }

    private void finishCancel() {
        PipeSessionManager.endSession(getIntent().getExtras().getString("sessionID"));
        setResult(RESULT_CANCELED);
        finish();
    }

    protected void doNextIntent() {
        Intent i = PipeSessionManager.getIntent();
        if (i == null) {
            throw new NullPointerException("No more intents");
        }
        int requestCode = PipeSessionManager.getIntentId();
        Log.d(TAG, String.format("Starting %s with requestCode %s", i.getAction(), requestCode));
        startActivityForResult(i, requestCode);
    }

    protected void finishSession() {
        Bundle bindle = PipeSessionManager.getResults();
        PipeSessionManager.endSession(getIntent().getExtras().getString("sessionID"));
        Intent output = new Intent();
        output.putExtras(bindle);
        setResult(RESULT_OK, output);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, String.format("onResult| request: %s | result ok: %s", requestCode, resultCode == RESULT_OK));
        if (resultCode == RESULT_OK && data != null) {
            boolean succeeded = PipeSessionManager.registerResult(requestCode, data.getExtras());
        } else if (data == null) {
            Log.e(TAG, String.format("No intent data returned from requestCode %s", requestCode));
        } else {
            Log.e(TAG, String.format("requestCode %s was canceled", requestCode));
            isCanceled = true;
        }
    }
}
