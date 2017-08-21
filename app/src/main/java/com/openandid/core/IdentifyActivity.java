package com.openandid.core;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdentifyActivity extends Activity {

    final String TAG = "IdentifyActivity";
    boolean enrolled = false;
    Bundle output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Log.i(TAG, "ID Started");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.spinner_view);
        Bundle extras = getIntent().getExtras();
        IdentifyBG bg = new IdentifyBG(extras);
        bg.execute();
        //show spinner
        //run asynch with extras
        //finish with code
    }

    @Override
    public void onBackPressed() {
        finish_error();
    }

    private void finish_ok() {
        //send all clear and id
        Intent i = new Intent();
        i.putExtras(output);
        setResult(RESULT_OK, i);
        this.finish();
    }

    private void finish_error() {
        //send error code
        Intent i = new Intent();
        setResult(RESULT_CANCELED, i);
        this.finish();
    }

    private class IdentifyBG extends AsyncTask<Void, Void, Void> {

        final List<String> fingers = new ArrayList<String>() {{
            add("left_thumb");
            add("right_thumb");
            add("left_index");
            add("right_index");
        }};
        Bundle extras;

        private IdentifyBG(Bundle mExtras) {
            super();
            extras = mExtras;

        }

        @Override
        protected Void doInBackground(Void... params) {


            Map<String, String> templates = new HashMap<String, String>();
            for (String f : fingers) {
                String temp = extras.getString(f);
                if (temp != null) {
                    templates.put(f, temp);
                    Log.i(TAG, "Found input template for finger: " + f);
                } else {
                    Log.i(TAG, "null template for finger: " + f);
                }
            }

            List<Engine.Match> matches = Controller.mEngine.get_best_matches(templates);


            output = new Bundle();
            int max_matches = 10;
            try {
                int max = extras.getInt("max_matches");
                if (max != 0) {
                    max_matches = max;
                }
            } catch (Exception e) {

            }
            if (matches.isEmpty() == true) {
                output.putString("matches_found", "0");
            } else {
                output.putString("matches_found", Integer.toString(matches.size()));
                int c = 0;
                for (Engine.Match m : matches) {
                    String c_s = Integer.toString(c);
                    output.putString(("match_id_" + c_s), m.uuid);
                    output.putString(("match_score_" + c_s), Double.toString(m.score));
                    if (c >= max_matches) {
                        break;
                    }
                    c += 1;
                }
            }
            return null;
        }

        protected void onPostExecute(Void res) {
            finish_ok();

        }
    }

}
