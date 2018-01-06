package com.openandid.core;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.github.cimsbioko.cimsprints.R;


public class AdvancedPreferences extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
