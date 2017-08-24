package data;

import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.openandid.core.CommCareSyncService;
import com.openandid.core.Controller;
import com.openandid.core.Engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CommCareContentHandler {

    private static final String TAG = "CCContentHandler";
    private static final String CASE_LISTING = "content://org.commcare.dalvik.case/casedb/case";
    private static final String CASE_DATA = "content://org.commcare.dalvik.case/casedb/data/";

    private static boolean inSync = false;

    private Map<String, String> instructions;
    private Set<String> caseIds;

    public CommCareContentHandler(Map<String, String> instructions) {
        this.instructions = instructions;
        caseIds = new HashSet<>();
    }

    public void sync(CommCareSyncService mService) {
        inSync = true;
        loadRelevantCaseIds(mService);
        Map<String, Map<String, String>> caseMap = loadCases(mService);
        translateTemplates(mService, caseMap);
        inSync = false;
    }

    private void translateTemplates(CommCareSyncService mService, Map<String, Map<String, String>> caseMap) {
        mService.showNotification("Translating Templates");
        Controller.mEngine.cacheCandidates(caseMap);
        if (!Engine.ready) {
            Log.i(TAG, "engine is busy, waiting.");
        }
        while (!Engine.ready) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Log.d(TAG, "interrupted during sleep");
            }
        }
        mService.showNotification("Service Ready.");
    }

    private Map<String, Map<String, String>> loadCases(CommCareSyncService mService) {
        mService.showNotification("Loading " + Integer.toString(caseIds.size()) + " cases from Commcare");
        Set<String> values = new HashSet<>(instructions.keySet());
        values.remove("case_type");
        Map<String, Map<String, String>> caseMap = new HashMap<>();
        Cursor c;
        for (String caseId : caseIds) {
            Map<String, String> templates = null;
            c = mService.getContentResolver().query(Uri.parse(CASE_DATA + caseId), null, null, null, null);
            if (c != null) {
                try {
                    c.moveToFirst();
                    int datum_pos = c.getColumnIndex("datum_id");
                    int val_pos = c.getColumnIndex("value");
                    String key = "";
                    do {
                        key = c.getString(datum_pos);
                        if (values.contains(key)) {
                            if (templates == null) {
                                templates = new HashMap<>();
                            }
                            String value = c.getString(val_pos);
                            templates.put(key, value);
                            Log.i(TAG, "key:" + key + " | v: " + value);
                        }
                    } while (c.moveToNext());
                    if (templates != null) {
                        caseMap.put(caseId, templates);
                    }
                } finally {
                    c.close();
                }
            }
        }
        return caseMap;
    }

    private void loadRelevantCaseIds(CommCareSyncService mService) {
        Cursor c = mService.getContentResolver().query(Uri.parse(CASE_LISTING), null, null, null, null);
        if (c != null) {
            try {
                c.moveToFirst();
                int caseTypeIdx = c.getColumnIndex("case_type");
                int caseIdIdx = c.getColumnIndex("case_id");
                String caseType = instructions.get("case_type");
                do {
                    if (c.getString(caseTypeIdx).equals(caseType)) {
                        caseIds.add(c.getString(caseIdIdx));
                        Log.i(TAG, "Adding Case: " + c.getString(caseIdIdx));
                    } else {
                        Log.i(TAG, "Ignoring Case with type: " + c.getString(caseTypeIdx));
                    }
                } while (c.moveToNext());
            } finally {
                c.close();
            }
        }
    }

    public Map<String, String> getInstructions() {
        return instructions;
    }

    public static boolean isInSync() {
        return inSync;
    }

    public static void setInSync(boolean inSync) {
        CommCareContentHandler.inSync = inSync;
    }
}
