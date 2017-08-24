package com.openandid.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.UUID;

public class LaunchActivity extends Activity {

    private static final String TAG = "LaunchActivity--CC";

    public static final String SCAN_ACTION = "com.openandid.core.SCAN";

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
                    if (Controller.ODK_SENTINEL.equals(key)) {
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
        i.putExtra("sessionID", UUID.randomUUID().toString());
        i.putExtra("prompt_0", ".PIPE Test 1");
        i.putExtra("easy_skip_0", "true");
        i.putExtra("prompt_1", ".PIPE Test 2");
        i.putExtra("easy_skip_1", "true");
        i.putExtra("left_finger_assignment_0", "left_index");
        i.putExtra("right_finger_assignment_0", "right_middle");
        i.putExtra("left_finger_assignment_1", "right_thumb");
        i.putExtra("right_finger_assignment_1", "left_middle");
        startActivityForResult(i, 101);
    }

    private void launchScan() {
        Intent i = new Intent();
        i.setAction(SCAN_ACTION);
        i.putExtra("sessionID", UUID.randomUUID().toString());
        i.putExtra("prompt", ".SCAN Test");
        i.putExtra("easy_skip", "true");
        i.putExtra("left_finger_assignment", "left_index");
        i.putExtra("right_finger_assignment", "right_middle");
        startActivityForResult(i, 101);
    }
}
