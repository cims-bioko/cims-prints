package com.openandid.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CommcareReceiver extends BroadcastReceiver {

    private static final String TAG = "CommcareReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "received CommCare broadcast, ignoring");
    }
}
