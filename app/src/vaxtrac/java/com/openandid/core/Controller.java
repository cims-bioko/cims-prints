package com.openandid.core;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;

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
import data.LocalDatabaseHandler;
import data.SharedPreferencesManager;
import logic.HostUsbManager;
import logic.Scanner;


@ReportsCrashes(
        httpMethod = Method.PUT,
        reportType = Type.JSON,
        formUri = "http://dev.biometrac.com:5984/acra-vaxtrac/_design/acra-storage/_update/report",
        formUriBasicAuthLogin = "acralog",
        formUriBasicAuthPassword = "acralogging",
        mode = ReportingInteractionMode.SILENT,
        resNotifTitle = R.string.app_name,
        resNotifText = R.string.crash_body,
        logcatArguments = {"-t", "10000", "-v", "time"}
)
public class Controller extends Application {

    public static final String ODK_SENTINEL = "odk_intent_bundle";

    private static final String TAG = "ApplicationController";
    public static Scanner mScanner = null;
    public static UsbDevice mDevice;
    public static UsbManager mUsbManager;
    public static HostUsbManager mHostUsbManager;
    public static PipeSessionManager pipeSession;

    private static List<Intent> stack = null;
    private static int stackPosition = 0;
    private static boolean pipeFinished = false;
    private static Bundle lastStackOutput = null;

    public static Engine mEngine = null;
    public static LocalDatabaseHandler db_handle = null;
    public static CommCareContentHandler commcare_handler = null;
    public static SharedPreferencesManager preference_manager = null;

    private static Context context;

    public void onCreate() {
        super.onCreate();

        //init the logging function
        ACRA.init(this);

        Controller.context = getApplicationContext();
        preference_manager = new SharedPreferencesManager(context);
        PipeSessionManager.init();
        //Start foreground service
        Intent i = new Intent(context, PersistenceService.class);
        context.startService(i);

    }

    public static Context getAppContext() {
        return Controller.context;
    }

    public static void sync_commcare_default() {
    }

    public static void sync_commcare(Map<String, String> output) {

    }

    public void test() {
        Log.i(TAG, "Received Message");
    }

    public static void kill_all() {
        Log.i(TAG, "SHUT IT DOWN!");
        preference_manager.notify_false_start();
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
        Log.d(TAG, "Enabling crash dialog");
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
        Log.d(TAG, "Disabling crash dialog");
        ACRAConfiguration config = ACRA.getConfig();
        try {
            config.setMode(ReportingInteractionMode.SILENT);
        } catch (ACRAConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static void crash() {
        Log.i(TAG, "Inducing Crash!");
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
        Log.i(TAG, "Setting PipeStack");
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
        Log.i(TAG, "ResetStack.");
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
