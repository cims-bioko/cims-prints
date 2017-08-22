package com.openandid.core;


import android.app.Activity;
import android.content.Context;
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

public class CCSyncActivity extends Activity {

    public static final int KEY_REQUEST_CODE = 1;

    /**
     * Called when the activity is first created.
     */

    private static final String TAG = "CCSyncActivity";
    private Map<String, String> template_datum_map;
    Spinner case_spinner;
    Map<String, Spinner> spinners;
    boolean spinners_set = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        case_spinner = (Spinner) this.findViewById(R.id.sync_spin_case);

        template_datum_map = new HashMap<String, String>();

        Set<String> types = new HashSet<String>();
        final Map<String, String> type_map = new HashMap<String, String>();


        Cursor c = this.managedQuery(Uri.parse("content://org.commcare.dalvik.case/casedb/case"), null, null, null, null);

        try {
            c.moveToFirst();
        } catch (NullPointerException e) {
            Log.i(TAG, "CommCare not running");
            Toast.makeText(this, "Requires CommCare Signin", Toast.LENGTH_LONG).show();
            this.finish();
            return;
        }


        int case_type_pos = c.getColumnIndex("case_type");
        int count = c.getCount();
        if (count == 0) {
            Toast.makeText(this, "No Saved Cases in CommCare Database", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Log.i(TAG, "position of case_type: " + Integer.toString(case_type_pos) + " | case count :" + Integer.toString(count));
        do {
            if (!types.contains(c.getString(case_type_pos))) {
                types.add(c.getString(case_type_pos));
                type_map.put(c.getString(case_type_pos), c.getString(c.getColumnIndex("case_id")));
            }
        } while (c.moveToNext());


        String[] type_array = types.toArray(new String[types.size()]);
        ArrayAdapter<String> sca = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, type_array);
        sca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        case_spinner.setAdapter(sca);
        /*
        final SimpleCursorAdapter sca = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, c, new String[] {"case_type"}, new int[] { android.R.id.text1});
        sca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        case_id.setAdapter(sca);
        */

        final Map<String, Integer> template_positions = new HashMap<String, Integer>();
        final Map<String, Integer> position_map = new HashMap<String, Integer>();
        spinners = new HashMap<String, Spinner>() {{
            put("left_index", (Spinner) findViewById(R.id.sync_spin_li));
            put("right_index", (Spinner) findViewById(R.id.sync_spin_ri));
            put("left_thumb", (Spinner) findViewById(R.id.sync_spin_lt));
            put("right_thumb", (Spinner) findViewById(R.id.sync_spin_rt));
        }};

        case_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinners_set = false;
                String case_id = type_map.get(parent.getItemAtPosition(position));
                String case_url = "content://org.commcare.dalvik.case/casedb/data/" + case_id;
                Cursor c2 = get_cursor(case_url);
                if (c2 == null) {
                    return;
                }
                c2.moveToFirst();
                List<String> keys = new ArrayList<String>() {{
                    add("None");
                }};
                int index = 0;
                do {
                    int datum_pos = c2.getColumnIndex("datum_id");
                    String datum = c2.getString(datum_pos);
                    position_map.put(datum, index);
                    keys.add(datum);
                    Log.i(TAG, "get datum_id iteration | " + Integer.toString(index) + " | datum: " + datum + " | pos: " + Integer.toString(datum_pos));
                    index += 1;


                } while (c2.moveToNext());
                String[] columns = keys.toArray(new String[keys.size()]);

                ArrayAdapter<String> aa = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, columns);
                aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                for (Spinner s : spinners.values()) {
                    s.setAdapter(aa);
                }
                spinners_set = true;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        Button b = (Button) findViewById(R.id.sync_accept_settings);
        b.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (register_datum(case_spinner, spinners)) {
                    Toast.makeText(getBaseContext(), "Fields Registered", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(getBaseContext(), "Error Registering Data", Toast.LENGTH_LONG).show();
                }

            }
        });

        if (Controller.preference_manager.has_preferences()) {
            Remember runner = new Remember(this);
            runner.execute();
        }

    }

    private Cursor get_cursor(String url) {
        Cursor c = this.managedQuery(Uri.parse(url), null, null, null, null);
        if (c == null) {
            Toast.makeText(getBaseContext(), "null cursor: " + url, Toast.LENGTH_SHORT).show();
            return null;
        }
        return c;

    }

    private boolean register_datum(Spinner case_spinner, Map<String, Spinner> template_spinners) {
        try {
            Map<String, String> output = new HashMap<String, String>();
            String case_type = (String) case_spinner.getSelectedItem();
            for (String key : template_spinners.keySet()) {
                Spinner s = template_spinners.get(key);
                String f = (String) s.getSelectedItem();
                if (!f.equals("None")) {
                    output.put(f, key);
                }
            }
            Controller.preference_manager.put_template_fields(case_type, output);
            output.put("case_type", case_type);
            for (String s : output.keySet()) {
                Log.i(TAG, "k: " + s + "| v: " + output.get(s));
            }
            Controller.sync_commcare(output);
            return true;
        } catch (Exception e) {
            return false;
        }

    }


    class Remember extends AsyncTask<Void, Void, Void> {
        private Context mContext;
        String case_type;
        Map<String, String> data;

        public Remember(Context context) {
            super();
            mContext = context;

        }

        @Override
        protected void onPreExecute() {
            try {
                data = Controller.preference_manager.get_template_fields();
                case_type = Controller.preference_manager.get_case_type();
                ArrayAdapter<String> myAdap = (ArrayAdapter<String>) case_spinner.getAdapter(); //cast to an ArrayAdapter
                int case_pos = myAdap.getPosition(case_type);
                case_spinner.setSelection(case_pos);
            } catch (Exception e) {
                Log.i(TAG, "Error setting case_type");
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                while (!spinners_set) {
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void res) {
            for (String key : data.keySet()) {
                try {
                    Spinner spinner = spinners.get(key);
                    ArrayAdapter<String> myAdap = (ArrayAdapter<String>) spinner.getAdapter(); //cast to an ArrayAdapter
                    int case_pos = myAdap.getPosition(key);
                    spinner.setSelection(case_pos);
                } catch (Exception e2) {
                    Log.i(TAG, "No couldn't set default for: " + key);
                    e2.printStackTrace();
                }
            }
        }

    }


}