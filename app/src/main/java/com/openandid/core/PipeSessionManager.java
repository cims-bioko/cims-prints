package com.openandid.core;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by sarwar on 10/3/15.
 */
public class PipeSessionManager {

    private static final String TAG = "PipeSessionManager";
    private static LinkedHashMap<String, Boolean> sessions;
    private static LinkedHashMap<Integer, Intent> currentSession;
    private static LinkedHashMap<Integer, Bundle> currentSessionResults;
    private static int currentSessionPosition;

    public static void init(){

        sessions = new LinkedHashMap<>();
        clearSession();
    }

    private static void clearSession(){
        currentSession = new LinkedHashMap<>();
        currentSessionResults = new LinkedHashMap<>();
        currentSessionPosition = 0;
    }

    public static boolean registerSession(String id, String action, Bundle info){
        if (isNewSession(id)) {
            // new session created
            startSession(id, action, info);
            return true;
        }else {
            // new session could not be created
            return false;
        }
    }

    private static void startSession(String id, String action, Bundle info){
        sessions.put(id, false);
        digestSession(action, info);
        currentSessionPosition = 0;
    }

    public static void endSession(String id){
        Log.d(TAG, "Closing Session: " + id);
        sessions.put(id, true);
        clearSession();
    }

    private static void digestSession(String action, Bundle info){
        if (action.equals("com.openandid.core.PIPE")){
            process_pipe(info);
        }else if(action.equals("com.openandid.core.SCAN")){
            append_scans(info, 0);
        }else{
            process_single(action, info);
        }
    }

    private static void process_single(String action, Bundle info){
        Log.i(TAG, String.format("Stacking | %s @ # %s ", action, 0));
        Intent i = new Intent();
        i.putExtras(info);
        i.setAction(rectifyAction(action));
        currentSession.put(0, i);
    }

    private static void process_pipe(Bundle info){
        Log.d(TAG, "Processing Pipe");
        printBundle(info);
        List<String> actions = new ArrayList<String>();
        int x = 0;
        int actionCounter = 0;
        while(true){
            String a = info.getString("action_"+Integer.toString(actionCounter));
            Log.d(TAG, String.format("action #%s -> %s", actionCounter, a));
            if (a!= null){
                if (a.equals("com.openandid.core.SCAN")){
                    x+= append_scans(info, x);
                    actionCounter +=1;
                }else{
                    Log.i(TAG, String.format("Stacking | %s @ # %s ", a, Integer.toString(x)));
                    Intent i = new Intent();
                    i.setAction(rectifyAction(a));
                    currentSession.put(x, i);
                    actionCounter +=1;
                    x+=1;
                }

            }else{
                Log.d(TAG, String.format("Finished processing with %s intents",x));
                break;
            }
        }
    }

    private static void printBundle(Bundle bundle){
        Iterator<String> keys = bundle.keySet().iterator();
        while(keys.hasNext()){
            Log.d(TAG, String.format("BundleKeys | %s", keys.next()));
        }
    }

    private static Integer append_scans(Bundle b, int starting) {
        Log.i(TAG, "Append Scans");
        int iter = ScanningActivity.get_total_scans_from_bundle(b);
        Log.i(TAG, "Total Scans: " + Integer.toString(iter));
        for (int x = 0; x< iter; x++){
            Intent i = ScanningActivity.get_next_scanning_bundle(b, x);
            if (i!=null){
                Log.i(TAG, String.format("Stacking | .SCAN @ # %s ", Integer.toString(starting+x)));
                currentSession.put(starting+x, i);
            }else{
                Log.i(TAG, "Scan Bundle Null");
            }
        }
        return iter;
    }

    public static boolean isNewSession(String id){
        return !sessions.containsKey(id);
    }

    public static boolean isSessionClosed(String id){
        if (!sessions.containsKey(id)){
            Log.e(TAG, "Session doesn't exist at all!");
            return true;
        }
        return sessions.get(id);
    }

    public static boolean registerResult(int intentId, Bundle result){
        if(!currentSessionResults.containsKey(intentId)){
            currentSessionResults.put(intentId, result);
            loadNextIntent();
            Log.d(TAG, "Result registered for intent #" + Integer.toString(intentId));
            return true;
        }else{
            Log.e(TAG, "Already registered result for intent #" + Integer.toString(intentId));
            return false;
        }
    }

    private static void loadNextIntent(){
        if(hasNextIntent()){
            currentSessionPosition += 1;
            Log.d(TAG, String.format("IntentId %s is loaded", currentSessionPosition));
        }else{
            currentSessionPosition += 1;
            Log.d(TAG, "Session has no more intents");
        }
    }

    private static boolean hasNextIntent(){
        int nextIntentId = currentSessionPosition +1;
        return hasIntent(nextIntentId);
    }

    private static boolean hasIntent(int id){
        try{
            return currentSession.containsKey(id);
        }catch (Exception e){
            Log.e(TAG, "hasIntent failed, returning false: " + e.toString());
            return false;
        }
    }

    public static Intent getIntent(){
        if(hasIntent(currentSessionPosition)){
            Intent outIntent = currentSession.get(currentSessionPosition);
            outIntent.putExtras(aggregateResults());
            return outIntent;
        }
        return null;
    }

    public static int getIntentId(){
        return currentSessionPosition;
    }

    private static void combineBundles(Bundle bindle, Bundle input){
        bindle.putAll(input);
        return;
    }

    private static Bundle aggregateResults(){
        Bundle bindle = new Bundle();
        Iterator<Integer> keys = currentSessionResults.keySet().iterator();
        while (keys.hasNext()){
            int key = keys.next();
            Log.d(TAG, String.format("Combining bindle and %s", key));
            Bundle current = currentSessionResults.get(key);
            if (current != null){
                combineBundles(bindle, current);
            }else{
                Log.e(TAG, String.format("Result #%s was null and will not be combined", key));
            }
        }
        Bundle output = new Bundle();
        output.putAll(bindle);
        Log.d(TAG, "Adding CCODK bundle");
        output.putBundle("odk_intent_bundle", bindle);
        Log.d(TAG, "Aggregate bundle ready.");
        return output;
    }

    public static Bundle getResults(){
        if(!hasNextIntent()){
            Log.d(TAG, "Returning bundle to Dispatch");
            return aggregateResults();
        }else{
            Log.e(TAG, "getResult called before session was complete. Returning null");
            return null;
        }

    }

    public static String rectifyAction(String action){
        if (action.contains("IDENTIFY")){
            return "com.openandid.internal.IDENTIFY";
        }else if (action.contains("ENROLL")){
            return "com.openandid.internal.ENROLL";
        }else{
            return action;
        }
    }
}
