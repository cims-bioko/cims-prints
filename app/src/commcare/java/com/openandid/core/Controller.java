package com.openandid.core;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

import data.CommCareContentHandler;
import data.SharedPreferencesManager;
import logic.HostUsbManager;
import logic.Scanner;

public class Controller extends Application {

    private static final String TAG = "ApplicationController";
    public static Scanner mScanner = null;
    public static UsbDevice mDevice;
    public static UsbManager mUsbManager;
    public static HostUsbManager mHostUsbManager;

    private static List<Intent> stack = null;
    private static int stackPosition = 0;
    private static boolean pipeFinished = false;
    private static Bundle lastStackOutput = null;

    public static Engine mEngine = null;
    public static CommCareContentHandler commCareHandler = null;
    public static SharedPreferencesManager prefsMgr = null;

    private static Context context;

    public void onCreate() {
        super.onCreate();

        Controller.context = getApplicationContext();
        mEngine = new Engine(Controller.context, 27.0f);
        prefsMgr = new SharedPreferencesManager(context);
        PipeSessionManager.init();

        //Start foreground service
        Intent i = new Intent(context, PersistenceService.class);
        context.startService(i);
        syncCommCareDefault();
    }

    public static Context getAppContext() {
        return Controller.context;
    }

    public static void syncCommCareDefault() {
        if (prefsMgr.hasPreferences()) {
            Map<String, String> data = prefsMgr.getTemplateFields();
            data.put("case_type", prefsMgr.getCaseType());
            syncCommCare(data);
        } else {
            Toast.makeText(context, "CommCare Setting are NOT SET. Check BMTCore.", Toast.LENGTH_LONG).show();
        }
    }

    public static void syncCommCare(Map<String, String> syncSpec) {
        if (CommCareSyncService.isReady()) {
            commCareHandler = new CommCareContentHandler(syncSpec);
            Intent i = new Intent(context, CommCareSyncService.class);
            context.startService(i);
        } else {
            Log.i(TAG, "Delayed sync queued");
            CommCareSyncService.setResync(true);
        }
    }

    public static void killAll() {
        prefsMgr.notifyFalseStart();
        context.stopService(new Intent(context, PersistenceService.class));
        context.stopService(new Intent(context, NotificationReceiver.class));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Log.d(TAG, "interrupted during sleep");
        }
        System.exit(0);
    }

    public static List<Intent> getPipeStack() {
        return stack;
    }

    public static int getPipeStackPosition() {
        return stackPosition;
    }

    public static void setPipeStack(List<Intent> pipeStack, int pipePosition) {
        setPipeStack(pipeStack);
        setPipePosition(pipePosition);
    }

    public static void setPipeStack(List<Intent> pipeStack) {
        stack = pipeStack;
        pipeFinished = false;
    }

    public static void setPipePosition(int pipePosition) {
        stackPosition = pipePosition;
    }

    public static void nullPipeStack() {
        setPipeStack(null, 0);
    }

    public static boolean isStackFinished() {
        return pipeFinished;
    }

    public static void resetStack() {
        pipeFinished = false;
        nullPipeStack();
        lastStackOutput = null;
    }

    public static void setStackFinished() {
        pipeFinished = true;
    }

    public static Bundle getLastStackOutput() {
        return lastStackOutput;
    }

    public static void setLastStackOutput(Bundle bundle) {
        lastStackOutput = bundle;
    }

}
