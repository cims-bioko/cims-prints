package com.openandid.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class LaunchActivity extends Activity {


    Button fire_btn;
    private final String TAG = "LaunchActivity--CC";


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
                    Log.d(TAG, String.format("%s | %s", key, extras.getString(key)));
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
        Log.d(TAG, "Setting contentview");
        setContentView(R.layout.activity_main);
        fire_btn = (Button) findViewById(R.id.main_fire_btn);
        enable_fire();
        Button sync_button = (Button) findViewById(R.id.main_sync_btn);
        if (!Controller.preference_manager.has_preferences()) {
            sync_button.setVisibility(View.GONE);
        } else {
            sync_button.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!Controller.commcare_handler.isWorking()) {
                        Controller.sync_commcare_default();
                        Toast.makeText(getBaseContext(), "Sync Started.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getBaseContext(), "Sync is Already Running.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        Button sync_set = (Button) findViewById(R.id.main_sync_settings_btn);
        sync_set.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!Controller.commcare_handler.isWorking()) {
                    Intent i = new Intent(getBaseContext(), CCSyncActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(getBaseContext(), "Please wait for Sync to Complete...", Toast.LENGTH_LONG).show();
                }

            }
        });
        Button advanced = (Button) findViewById(R.id.advanced_settings_btn);
        advanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getBaseContext(), AdvancedPreferences.class);
                startActivity(i);

            }
        });

    }

    public void enable_fire() {
        fire_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fire_intent();
            }
        });
        fire_btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                fire_other_intent();
                return true;
            }
        });
    }

    private void fire_intent() {
        Intent i = new Intent();
        Log.d(TAG, "Demo for .PIPE");
        //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setAction("com.openandid.core.SCAN");
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

    private void fire_other_intent() {
        Log.d(TAG, "Demo for .SCAN");
        Intent i = new Intent();
        //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setAction("com.openandid.core.SCAN");
        i.putExtra("prompt", ".SCAN Test");
        i.putExtra("easy_skip", "true");
        i.putExtra("left_finger_assignment", "left_index");
        i.putExtra("right_finger_assignment", "right_middle");
        startActivityForResult(i, 101);
    }

}
