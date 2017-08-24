package com.openandid.core;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdentifyActivity extends Activity {

    private static final String TAG = "IdentifyActivity";

    private Bundle output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "ID Started");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.spinner_view);
        IdentifyTask bg = new IdentifyTask(getIntent().getExtras());
        bg.execute();
    }

    @Override
    public void onBackPressed() {
        finishCancel();
    }

    private void finishOk() {
        Intent i = new Intent();
        i.putExtras(output);
        setResult(RESULT_OK, i);
        finish();
    }

    private void finishCancel() {
        Intent i = new Intent();
        setResult(RESULT_CANCELED, i);
        finish();
    }

    private class IdentifyTask extends AsyncTask<Void, Void, Void> {

        private Bundle extras;

        private IdentifyTask(Bundle mExtras) {
            super();
            extras = mExtras;
        }

        @Override
        protected Void doInBackground(Void... params) {

            Map<String, String> templates = new HashMap<>();

            for (SupportedFinger f : SupportedFinger.values()) {
                String temp = extras.getString(f.name());
                if (temp != null) {
                    templates.put(f.name(), temp);
                }
            }

            List<Engine.Match> matches = Controller.mEngine.getBestMatches(templates);

            output = new Bundle();
            int max_matches = 10;
            try {
                int max = extras.getInt("max_matches");
                if (max != 0) {
                    max_matches = max;
                }
            } catch (Exception e) {
                Log.e(TAG, "failed to get max matches");
            }
            if (matches.isEmpty()) {
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
            finishOk();
        }
    }

}
