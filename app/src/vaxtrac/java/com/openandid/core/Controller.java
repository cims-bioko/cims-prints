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
        ACRA.init(this);
        Controller.context = getApplicationContext();
        prefsMgr = new SharedPreferencesManager(context);
        PipeSessionManager.init();
        Intent i = new Intent(context, PersistenceService.class);
        context.startService(i);
    }

    public static Context getAppContext() {
        return Controller.context;
    }

    public static void syncCommCare(Map<String, String> output) {
        // stub
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
            Log.e(TAG, "Couldn't set Acra dialog options", e);
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
