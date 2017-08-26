package com.openandid.core;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.acra.ACRA;
import org.acra.ACRAConfiguration;
import org.acra.ACRAConfigurationException;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender.Method;
import org.acra.sender.HttpSender.Type;

import java.util.List;
import java.util.Map;

import data.CommCareContentHandler;
import data.SharedPreferencesManager;
import logic.HostUsbManager;
import logic.Scanner;


@ReportsCrashes(
        httpMethod = Method.PUT,
        reportType = Type.JSON,
        formUri = "http://dev.biometrac.com:5984/acra-bmt/_design/acra-storage/_update/report",
        formUriBasicAuthLogin = "acralog",
        formUriBasicAuthPassword = "acralogging",
        mode = ReportingInteractionMode.SILENT,
        resNotifTitle = R.string.app_name,
        resNotifText = R.string.crash_body,
        logcatArguments = {"-t", "10000", "-v", "time"}
)
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

        //init the logging function
        //check for preference?
        ACRA.init(this);

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

    public static void enableCrashDialog() {
        ACRAConfiguration config = ACRA.getConfig();
        config.setLogcatArguments(new String[]{"-t", "20000", "-v", "time"});
        try {
            config.setResNotifTitle(R.string.app_name);
            config.setResNotifText(R.string.crash_body);
            config.setResToastText(R.string.crash_toast);
            config.setResDialogText(R.string.crash_body);
            config.setResDialogCommentPrompt(R.string.crash_prompt);
            config.setMode(ReportingInteractionMode.DIALOG);
        } catch (ACRAConfigurationException e) {
            Log.e(TAG, "Couldn't set Acra dialog options");
            e.printStackTrace();
        }
    }

    public static void disableCrashDialog() {
        ACRAConfiguration config = ACRA.getConfig();
        try {
            config.setMode(ReportingInteractionMode.SILENT);
        } catch (ACRAConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static void crash() {
        enableCrashDialog();
        ACRA.getErrorReporter().handleException(new Exception("Induced Crash"));
        disableCrashDialog();
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
