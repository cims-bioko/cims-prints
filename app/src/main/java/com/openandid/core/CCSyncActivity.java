package com.openandid.core;


import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import logic.Finger;

public class CCSyncActivity extends Activity {

    /**
     * Called when the activity is first created.
     */

    private static final String TAG = "CCSyncActivity";

    private Spinner caseSpinner;
    private Map<String, Spinner> fingerSpinners;
    private boolean spinnersSet = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        caseSpinner = (Spinner) this.findViewById(R.id.sync_spin_case);

        Set<String> types = new HashSet<>();
        final Map<String, String> typeMap = new HashMap<>();

        Cursor c = this.managedQuery(Uri.parse("content://org.commcare.dalvik.case/casedb/case"), null, null, null, null);

        try {
            c.moveToFirst();
        } catch (NullPointerException e) {
            Log.i(TAG, "CommCare not running");
            Toast.makeText(this, "Requires CommCare Signin", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        int caseTypeIdx = c.getColumnIndex("case_type");
        int count = c.getCount();
        if (count == 0) {
            Toast.makeText(this, "No Saved Cases in CommCare Database", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Log.i(TAG, "position of case_type: " + Integer.toString(caseTypeIdx) + " | case count :" + Integer.toString(count));
        do {
            if (!types.contains(c.getString(caseTypeIdx))) {
                types.add(c.getString(caseTypeIdx));
                typeMap.put(c.getString(caseTypeIdx), c.getString(c.getColumnIndex("case_id")));
            }
        } while (c.moveToNext());


        String[] typeArray = types.toArray(new String[types.size()]);
        ArrayAdapter<String> sca = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, typeArray);
        sca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        caseSpinner.setAdapter(sca);

        fingerSpinners = new HashMap<String, Spinner>() {{
            put(Finger.left_index.name(), (Spinner) findViewById(R.id.sync_spin_li));
            put(Finger.right_index.name(), (Spinner) findViewById(R.id.sync_spin_ri));
            put(Finger.left_thumb.name(), (Spinner) findViewById(R.id.sync_spin_lt));
            put(Finger.right_thumb.name(), (Spinner) findViewById(R.id.sync_spin_rt));
        }};

        caseSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnersSet = false;
                String caseId = typeMap.get(parent.getItemAtPosition(position));
                String caseUrl = "content://org.commcare.dalvik.case/casedb/data/" + caseId;
                Cursor c2 = getCursor(caseUrl);
                if (c2 == null) {
                    return;
                }
                c2.moveToFirst();
                List<String> keys = new ArrayList<String>() {{
                    add("None");
                }};
                int index = 0;
                do {
                    int datimIdIdx = c2.getColumnIndex("datum_id");
                    String datumId = c2.getString(datimIdIdx);
                    keys.add(datumId);
                    Log.i(TAG, "get datum_id iteration | " + Integer.toString(index) + " | datum: " + datumId + " | pos: " + Integer.toString(datimIdIdx));
                    index += 1;
                } while (c2.moveToNext());
                String[] columns = keys.toArray(new String[keys.size()]);

                ArrayAdapter<String> aa = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_spinner_item, columns);
                aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                for (Spinner s : fingerSpinners.values()) {
                    s.setAdapter(aa);
                }
                spinnersSet = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        Button b = (Button) findViewById(R.id.sync_accept_settings);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (registerDatum(caseSpinner, fingerSpinners)) {
                    Toast.makeText(getBaseContext(), "Fields Registered", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(getBaseContext(), "Error Registering Data", Toast.LENGTH_LONG).show();
                }
            }
        });

        if (Controller.prefsMgr.hasPreferences()) {
            Remember runner = new Remember();
            runner.execute();
        }
    }

    private Cursor getCursor(String url) {
        Cursor c = this.managedQuery(Uri.parse(url), null, null, null, null);
        if (c == null) {
            Toast.makeText(getBaseContext(), "null cursor: " + url, Toast.LENGTH_SHORT).show();
            return null;
        }
        return c;
    }

    private boolean registerDatum(Spinner case_spinner, Map<String, Spinner> template_spinners) {
        try {
            Map<String, String> syncPrefs = new HashMap<>();
            String caseType = (String) case_spinner.getSelectedItem();
            for (String key : template_spinners.keySet()) {
                Spinner s = template_spinners.get(key);
                String f = (String) s.getSelectedItem();
                if (!f.equals("None")) {
                    syncPrefs.put(f, key);
                }
            }
            Controller.prefsMgr.putTemplateFields(caseType, syncPrefs);
            syncPrefs.put("case_type", caseType);
            Controller.syncCommCare(syncPrefs);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    private class Remember extends AsyncTask<Void, Void, Void> {

        private String caseType;
        private Map<String, String> templateFields;

        @Override
        @SuppressWarnings("unchecked")
        protected void onPreExecute() {
            try {
                templateFields = Controller.prefsMgr.getTemplateFields();
                caseType = Controller.prefsMgr.getCaseType();
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) caseSpinner.getAdapter();
                caseSpinner.setSelection(adapter.getPosition(caseType));
            } catch (Exception e) {
                Log.i(TAG, "Error setting case_type");
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                while (!spinnersSet) {
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void onPostExecute(Void res) {
            for (String key : templateFields.keySet()) {
                try {
                    Spinner spinner = fingerSpinners.get(key);
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
                    spinner.setSelection(adapter.getPosition(key));
                } catch (Exception e2) {
                    Log.e(TAG, "No couldn't set default for: " + key, e2);
                }
            }
        }
    }
}