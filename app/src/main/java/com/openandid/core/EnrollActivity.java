package com.openandid.core;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnrollActivity extends Activity {

    private static final String TAG = "EnrollActivity";

    private static final List<String> FINGERS = new ArrayList<String>() {{
        add("left_thumb");
        add("right_thumb");
        add("left_index");
        add("right_index");
    }};

    private boolean enrolled = false;
    private String previousId = null;
    private String newId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.spinner_view);
        EnrollTask bg = new EnrollTask(getIntent().getExtras());
        bg.execute();
    }

    private void finishOk() {
        Intent i = new Intent();
        i.putExtra("new_id", newId);
        setResult(RESULT_OK, i);
        finish();
    }

    private void finishCancel() {
        Intent i = new Intent();
        i.putExtra("previous_id", previousId);
        setResult(RESULT_CANCELED, i);
        finish();
    }

    private class EnrollTask extends AsyncTask<Void, Void, Void> {

        Bundle extras;

        private EnrollTask(Bundle mExtras) {
            super();
            extras = mExtras;
        }

        @Override
        protected Void doInBackground(Void... params) {

            String uuid = extras.getString("uuid");

            Map<String, String> templates = new HashMap<>();
            for (String f : FINGERS) {
                String temp = extras.getString(f);
                if (temp != null) {
                    templates.put(f, temp);
                }
            }
            try {
                Controller.mEngine.addCandidateToCache(uuid, templates);
                enrolled = true;
            } catch (Exception e) {
                enrolled = false;
            }

            return null;
        }

        protected void onPostExecute(Void res) {
            if (enrolled) {
                finishOk();
            } else {
                finishCancel();
            }
        }
    }

}
