package org.nism.tellthetime;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ShowSettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedState) {
    	super.onCreate(savedState);
    	getFragmentManager().beginTransaction().replace(android.R.id.content,
                new MyPreferenceFragment()).commit();
    }
}
