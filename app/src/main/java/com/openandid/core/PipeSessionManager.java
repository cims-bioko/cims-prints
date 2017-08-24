package com.openandid.core;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.LinkedHashMap;

import static com.openandid.core.Constants.ENROLL;
import static com.openandid.core.Constants.IDENTIFY;
import static com.openandid.core.Constants.INTERNAL_ENROLL_ACTION;
import static com.openandid.core.Constants.INTERNAL_IDENTIFY_ACTION;
import static com.openandid.core.Constants.PIPE_ACTION;
import static com.openandid.core.Constants.SCAN_ACTION;


class PipeSessionManager {

    private static final String TAG = "PipeSessionManager";

    private static LinkedHashMap<String, Boolean> sessions;
    private static LinkedHashMap<Integer, Intent> currentSession;
    private static LinkedHashMap<Integer, Bundle> currentSessionResults;
    private static int currentSessionPosition;

    static void init() {
        sessions = new LinkedHashMap<>();
        clearSession();
    }

    private static void clearSession() {
        currentSession = new LinkedHashMap<>();
        currentSessionResults = new LinkedHashMap<>();
        currentSessionPosition = 0;
    }

    static boolean registerSession(String id, String action, Bundle info) {
        if (isNewSession(id)) {
            startSession(id, action, info);
            return true;
        } else {
            return false;
        }
    }

    private static void startSession(String id, String action, Bundle info) {
        sessions.put(id, false);
        digestSession(action, info);
        currentSessionPosition = 0;
    }

    static void endSession(String id) {
        Log.d(TAG, "Closing Session: " + id);
        sessions.put(id, true);
        clearSession();
    }

    private static void digestSession(String action, Bundle info) {
        if (PIPE_ACTION.equals(action)) {
            digestPipe(info);
        } else if (SCAN_ACTION.equals(action)) {
            importScans(info, 0);
        } else {
            processSingle(action, info);
        }
    }

    private static void processSingle(String action, Bundle info) {
        Log.i(TAG, String.format("Stacking | %s @ # %s ", action, 0));
        Intent i = new Intent();
        i.putExtras(info);
        i.setAction(rectifyAction(action));
        currentSession.put(0, i);
    }

    private static void digestPipe(Bundle bundle) {
        int x = 0, actionCounter = 0;
        String action;
        do {
            action = bundle.getString("action_" + Integer.toString(actionCounter));
            if (action != null) {
                if (SCAN_ACTION.equals(action)) {
                    x += importScans(bundle, x);
                    actionCounter += 1;
                } else {
                    Log.i(TAG, String.format("Stacking | %s @ # %s ", action, Integer.toString(x)));
                    Intent i = new Intent();
                    i.setAction(rectifyAction(action));
                    currentSession.put(x, i);
                    actionCounter += 1;
                    x += 1;
                }
            }
        } while (action != null);
        Log.d(TAG, String.format("Finished processing with %s intents", x));
    }

    private static Integer importScans(Bundle bundle, int starting) {
        int iter = ScanningActivity.getScanCount(bundle);
        for (int x = 0; x < iter; x++) {
            Intent i = ScanningActivity.getNextScan(bundle, x);
            if (i != null) {
                Log.i(TAG, String.format("Stacking | .SCAN @ # %s ", Integer.toString(starting + x)));
                currentSession.put(starting + x, i);
            } else {
                Log.i(TAG, "Scan Bundle Null");
            }
        }
        return iter;
    }

    static boolean isNewSession(String id) {
        return !sessions.containsKey(id);
    }

    static boolean registerResult(int intentId, Bundle result) {
        if (!currentSessionResults.containsKey(intentId)) {
            currentSessionResults.put(intentId, result);
            loadNextIntent();
            Log.d(TAG, "Result registered for intent #" + Integer.toString(intentId));
            return true;
        } else {
            Log.e(TAG, "Already registered result for intent #" + Integer.toString(intentId));
            return false;
        }
    }

    private static void loadNextIntent() {
        if (hasNextIntent()) {
            currentSessionPosition += 1;
            Log.d(TAG, String.format("IntentId %s is loaded", currentSessionPosition));
        } else {
            currentSessionPosition += 1;
            Log.d(TAG, "Session has no more intents");
        }
    }

    private static boolean hasNextIntent() {
        int nextIntentId = currentSessionPosition + 1;
        return hasIntent(nextIntentId);
    }

    private static boolean hasIntent(int id) {
        try {
            return currentSession.containsKey(id);
        } catch (Exception e) {
            Log.e(TAG, "hasIntent failed, returning false: " + e.toString());
            return false;
        }
    }

    public static Intent getIntent() {
        if (hasIntent(currentSessionPosition)) {
            Intent outIntent = currentSession.get(currentSessionPosition);
            outIntent.putExtras(aggregateResults());
            return outIntent;
        }
        return null;
    }

    static int getIntentId() {
        return currentSessionPosition;
    }

    private static void combineBundles(Bundle bundle, Bundle input) {
        bundle.putAll(input);
    }

    private static Bundle aggregateResults() {
        Bundle bindle = new Bundle();
        for (Integer key : currentSessionResults.keySet()) {
            Bundle current = currentSessionResults.get(key);
            if (current != null) {
                combineBundles(bindle, current);
            } else {
                Log.e(TAG, String.format("Result #%s was null and will not be combined", key));
            }
        }
        Bundle output = new Bundle();
        output.putAll(bindle);
        output.putBundle(Controller.ODK_SENTINEL, bindle);
        return output;
    }

    public static Bundle getResults() {
        if (!hasNextIntent()) {
            return aggregateResults();
        } else {
            Log.e(TAG, "getResult called before session was complete. Returning null");
            return null;
        }
    }

    private static String rectifyAction(String action) {
        if (action.contains(IDENTIFY)) {
            return INTERNAL_IDENTIFY_ACTION;
        } else if (action.contains(ENROLL)) {
            return INTERNAL_ENROLL_ACTION;
        } else {
            return action;
        }
    }
}
