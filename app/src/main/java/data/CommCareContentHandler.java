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

    private Map<String, String> syncSpec;
    private Set<String> caseIds;

    public CommCareContentHandler(Map<String, String> syncSpec) {
        this.syncSpec = syncSpec;
        caseIds = new HashSet<>();
    }

    public void sync(CommCareSyncService mService) {
        inSync = true;
        loadRelevantCaseIds(mService);
        Map<String, Map<String, String>> caseMap = loadCaseTemplates(mService);
        translateTemplates(mService, caseMap);
        inSync = false;
    }

    private void translateTemplates(CommCareSyncService mService, Map<String, Map<String, String>> caseMap) {
        mService.showNotification("Translating Templates");
        Controller.mEngine.populateCache(caseMap);
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

    private Map<String, Map<String, String>> loadCaseTemplates(CommCareSyncService mService) {

        mService.showNotification("Loading " + Integer.toString(caseIds.size()) + " cases from Commcare");

        Set<String> templateKeys = new HashSet<>(syncSpec.keySet());
        templateKeys.remove("case_type"); // dirty, dirty hack - this approach will sting someone later

        Map<String, Map<String, String>> caseTemplates = new HashMap<>();
        for (String caseId : caseIds) {

            Map<String, String> templates = null; // only allocate map when there's data for the case
            Cursor c = mService.getContentResolver().query(Uri.parse(CASE_DATA + caseId), null, null, null, null);

            if (c != null) {
                try {
                    c.moveToFirst();
                    int datumKeyIdx = c.getColumnIndex("datum_id"), valueIdx = c.getColumnIndex("value");
                    do {
                        String datumKey = c.getString(datumKeyIdx);
                        if (templateKeys.contains(datumKey)) {
                            // allocate the map now, if it doesn't already exist
                            if (templates == null) {
                                templates = new HashMap<>();
                            }
                            templates.put(datumKey, c.getString(valueIdx));
                        }
                    } while (c.moveToNext());
                } finally {
                    c.close();
                }
            }

            if (templates != null) {
                caseTemplates.put(caseId, templates);
            }
        }

        return caseTemplates;
    }

    private void loadRelevantCaseIds(CommCareSyncService mService) {
        Cursor c = mService.getContentResolver().query(Uri.parse(CASE_LISTING), null, null, null, null);
        if (c != null) {
            try {
                c.moveToFirst();
                int caseTypeIdx = c.getColumnIndex("case_type");
                int caseIdIdx = c.getColumnIndex("case_id");
                String caseType = syncSpec.get("case_type");
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

    public Map<String, String> getSyncSpec() {
        return syncSpec;
    }

    public static boolean isInSync() {
        return inSync;
    }

    public static void setInSync(boolean inSync) {
        CommCareContentHandler.inSync = inSync;
    }
}
