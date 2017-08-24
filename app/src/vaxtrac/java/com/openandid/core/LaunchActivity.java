package com.openandid.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.UUID;

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
                        if (Controller.ODK_SENTINEL.equals(key)) {
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
            i.setAction("com.openandid.core.SCAN");
            i.putExtra("sessionID", UUID.randomUUID().toString());
            i.putExtra("prompt_0", ".PIPE Test 1");
            i.putExtra("easy_skip_0", "true");
            i.putExtra("prompt_1", ".PIPE Test 2");
            i.putExtra("easy_skip_1", "true");
            i.putExtra("left_finger_assignment_0", "left_index");
            i.putExtra("right_finger_assignment_0", "right_middle");
            i.putExtra("left_finger_assignment_1", "right_thumb");
            i.putExtra("right_finger_assignment_1", "left_middle");
            startActivityForResult(i, PIPE_REQUEST);
        }

        private void launchScanDemo() {
            Intent i = new Intent();
            i.setAction("com.openandid.core.SCAN");
            i.putExtra("sessionID", UUID.randomUUID().toString());
            i.putExtra("prompt", ".SCAN Test");
            i.putExtra("easy_skip", "true");
            i.putExtra("left_finger_assignment", "left_index");
            i.putExtra("right_finger_assignment", "right_middle");
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
