package com.openandid.core;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by sarwar on 6/4/15.
 */
public class AdvancedPreferences extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
