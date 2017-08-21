package com.openandid.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Iterator;
import java.util.UUID;

public class LaunchActivity extends Activity {


    Button fire_btn;
    private final String TAG = "LaunchActivity--VT";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupScreen();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "Dispatcher Has Activity Result");
        Log.d(TAG, String.format("Result Ok: %s", resultCode == RESULT_OK));
        if (resultCode == RESULT_OK) {
            try {
                Bundle b = data.getExtras();
                Iterator<String> keys = b.keySet().iterator();
                String ignore = "odk_intent_bundle";
                while (keys.hasNext()) {
                    String key = keys.next();
                    try {
                        if (key.equals(ignore)) {
                            throw new IllegalArgumentException("Can't read bundle");
                        }
                        Log.d(TAG, String.format("%s | %s", key, b.getString(key)));
                    } catch (Exception e2) {
                        Log.d(TAG, String.format("%s | not readable", key));
                    }
                }
            } catch (Exception e) {
                Log.i(TAG, "No output from activity");
                Log.i(TAG, e.toString());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setupScreen() {
        Log.d(TAG, "Setting contentview");
        setContentView(R.layout.activity_main);
        //TODO This needs to be its own little screen
        fire_btn = (Button) findViewById(R.id.main_fire_btn);
        enable_fire();

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

    private void fire_other_intent() {
        Log.d(TAG, "Demo for .SCAN");
        Intent i = new Intent();
        //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setAction("com.openandid.core.SCAN");
        i.putExtra("sessionID", UUID.randomUUID().toString());
        i.putExtra("prompt", ".SCAN Test");
        i.putExtra("easy_skip", "true");
        i.putExtra("left_finger_assignment", "left_index");
        i.putExtra("right_finger_assignment", "right_middle");
        startActivityForResult(i, 102);
    }

}
