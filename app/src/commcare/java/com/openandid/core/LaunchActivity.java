package com.openandid.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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

    private static final String TAG = "LaunchActivity--CC";

    public static final int REQUEST_CODE = 101;

    Button launchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupScreen();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            Bundle extras = data.getExtras();
            for (String key : extras.keySet()) {
                try {
                    if (ODK_INTENT_BUNDLE_KEY.equals(key)) {
                        throw new IllegalArgumentException("Can't read bundle");
                    }
                } catch (Exception e2) {
                    Log.d(TAG, String.format("%s | not readable", key));
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "No output from activity", e);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setupScreen() {

        setContentView(R.layout.activity_main);

        launchButton = (Button) findViewById(R.id.main_fire_btn);
        addLaunchHandlers();

        Button syncButton = (Button) findViewById(R.id.main_sync_btn);
        if (!Controller.prefsMgr.hasPreferences()) {
            syncButton.setVisibility(View.GONE);
        } else {
            syncButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!Controller.commCareHandler.isInSync()) {
                        Controller.syncCommCareDefault();
                        Toast.makeText(getBaseContext(), "Sync Started.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getBaseContext(), "Sync is Already Running.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        Button syncSettingsButton = (Button) findViewById(R.id.main_sync_settings_btn);
        syncSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Controller.commCareHandler.isInSync()) {
                    Intent i = new Intent(getBaseContext(), CCSyncActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(getBaseContext(), "Please wait for Sync to Complete...", Toast.LENGTH_LONG).show();
                }
            }
        });

        Button advancedSettingsButton = (Button) findViewById(R.id.advanced_settings_btn);
        advancedSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getBaseContext(), AdvancedPreferences.class);
                startActivity(i);
            }
        });
    }

    public void addLaunchHandlers() {
        launchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchPipe();
            }
        });
        launchButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                launchScan();
                return true;
            }
        });
    }

    private void launchPipe() {

        Intent i = new Intent();
        i.setAction(SCAN_ACTION);

        i.putExtra(SESSION_ID_KEY, UUID.randomUUID().toString());

        // first scan setup
        i.putExtra(PROMPT_KEY + "_0", ".PIPE Test 1");
        i.putExtra(EASY_SKIP_KEY + "_0", "true");
        i.putExtra(LEFT_FINGER_ASSIGNMENT_KEY + "_0", Finger.left_index.name());
        i.putExtra(RIGHT_FINGER_ASSIGNMENT_KEY + "_0", Finger.right_middle.name());

        // second scan setup
        i.putExtra(PROMPT_KEY + "_1", ".PIPE Test 2");
        i.putExtra(EASY_SKIP_KEY + "_1", "true");
        i.putExtra(LEFT_FINGER_ASSIGNMENT_KEY + "_1", Finger.right_thumb.name());
        i.putExtra(RIGHT_FINGER_ASSIGNMENT_KEY + "_1", Finger.left_middle.name());

        startActivityForResult(i, REQUEST_CODE);
    }

    private void launchScan() {

        Intent i = new Intent();
        i.setAction(SCAN_ACTION);

        i.putExtra(SESSION_ID_KEY, UUID.randomUUID().toString());

        i.putExtra(PROMPT_KEY, ".SCAN Test");
        i.putExtra(EASY_SKIP_KEY, "true");
        i.putExtra(LEFT_FINGER_ASSIGNMENT_KEY, Finger.left_index.name());
        i.putExtra(RIGHT_FINGER_ASSIGNMENT_KEY, Finger.right_middle.name());

        startActivityForResult(i, REQUEST_CODE);
    }
}
