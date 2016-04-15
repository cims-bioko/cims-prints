package com.openandid.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.openandid.core.Controller;

public class BootReceiver extends BroadcastReceiver {

	private final String TAG = "BootReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "Received Boot Broadcast");
		((Controller)context.getApplicationContext()).test();

	}

}
