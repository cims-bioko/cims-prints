package com.openandid.core;

import android.os.Bundle;
import android.preference.PreferenceActivity;


public class AdvancedPreferences extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
