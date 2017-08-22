package com.openandid.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.Set;

public class IntentFilterTest extends Activity {

    private final String TAG = "IntentFilterTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent i = new Intent("com.openandid.core.PIPE");
        i.putExtra("action_0", "com.openandid.core.SCAN");
        i.putExtra("action_1", "com.openandid.core.IDENTIFY");

        i.putExtra("prompt_0", "poop");
        i.putExtra("left_finger_assignment_0", "left_index");
        i.putExtra("right_finger_assignment_0", "right_index");
        i.putExtra("easy_skip_0", "true");

        i.putExtra("left_finger_assignment_1", "left_thumb");
        i.putExtra("right_finger_assignment_1", "right_thumb");
        i.putExtra("easy_skip_1", "true");

        startActivityForResult(i, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String txt = "";
        Bundle b = data.getExtras();
        if (resultCode != RESULT_CANCELED) {
            try {
                Set<String> keys = b.keySet();
                for (String k : keys) {
                    txt += k + " : " + b.get(k) + "\n";
                }
                Toast.makeText(this, txt, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.i(TAG, "No result returned");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
