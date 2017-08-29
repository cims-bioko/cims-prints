package com.openandid.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.UUID;

import logic.Finger;

import static com.openandid.core.Constants.EASY_SKIP_KEY;
import static com.openandid.core.Constants.LEFT_FINGER_ASSIGNMENT_KEY;
import static com.openandid.core.Constants.ODK_INTENT_BUNDLE_KEY;
import static com.openandid.core.Constants.PROMPT_KEY;
import static com.openandid.core.Constants.RIGHT_FINGER_ASSIGNMENT_KEY;
import static com.openandid.core.Constants.SCAN_ACTION;
import static com.openandid.core.Constants.SESSION_ID_KEY;

public class LaunchActivity extends Activity {

    private static final String TAG = "LaunchActivity--VT";

    private static final int PIPE_REQUEST = 101, SCAN_REQUEST = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button launchButton = (Button) findViewById(R.id.launch_scan_button);
        LaunchHandler launchHandler = new LaunchHandler();
        launchButton.setOnClickListener(launchHandler);
        launchButton.setOnLongClickListener(launchHandler);

        Button settingsButton = (Button) findViewById(R.id.advanced_settings_button);
        settingsButton.setOnClickListener(new SettingsHandler());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            try {
                Bundle extras = data.getExtras();
                for (String key : extras.keySet()) {
                    try {
                        if (ODK_INTENT_BUNDLE_KEY.equals(key)) {
                            throw new IllegalArgumentException("Can't read bundle");
                        }
                        Log.d(TAG, String.format("%s | %s", key, extras.getString(key)));
                    } catch (Exception e2) {
                        Log.d(TAG, String.format("%s | not readable", key));
                    }
                }
            } catch (Exception e) {
                Log.i(TAG, "No output from activity", e);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class LaunchHandler implements View.OnClickListener, View.OnLongClickListener {

        @Override
        public void onClick(View v) {
            launchPipeDemo();
        }

        @Override
        public boolean onLongClick(View view) {
            launchScanDemo();
            return true;
        }

        private void launchPipeDemo() {

            Intent i = new Intent();
            i.setAction(SCAN_ACTION);

            i.putExtra(SESSION_ID_KEY, UUID.randomUUID().toString());

            i.putExtra(PROMPT_KEY + "_0", ".PIPE Test 1");
            i.putExtra(EASY_SKIP_KEY + "_0", "true");
            i.putExtra(LEFT_FINGER_ASSIGNMENT_KEY + "_0", Finger.left_index.name());
            i.putExtra(RIGHT_FINGER_ASSIGNMENT_KEY + "_0", Finger.right_middle.name());

            i.putExtra(PROMPT_KEY + "_1", ".PIPE Test 2");
            i.putExtra(EASY_SKIP_KEY + "_1", "true");
            i.putExtra(LEFT_FINGER_ASSIGNMENT_KEY + "_1", Finger.right_thumb.name());
            i.putExtra(RIGHT_FINGER_ASSIGNMENT_KEY + "_1", Finger.left_middle.name());

            startActivityForResult(i, PIPE_REQUEST);
        }

        private void launchScanDemo() {

            Intent i = new Intent();
            i.setAction(SCAN_ACTION);

            i.putExtra(SESSION_ID_KEY, UUID.randomUUID().toString());

            i.putExtra(PROMPT_KEY, ".SCAN Test");
            i.putExtra(EASY_SKIP_KEY, "true");
            i.putExtra(LEFT_FINGER_ASSIGNMENT_KEY, Finger.left_index.name());
            i.putExtra(RIGHT_FINGER_ASSIGNMENT_KEY, Finger.right_middle.name());

            startActivityForResult(i, SCAN_REQUEST);
        }
    }


    private class SettingsHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent i = new Intent(getBaseContext(), AdvancedPreferences.class);
            startActivity(i);
        }
    }

}
