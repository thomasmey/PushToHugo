package de.m3y3r.pushtohugo;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import de.m3y3r.pushtohugo.git.Constants;

public class SettingsFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getPreferenceManager().setSharedPreferencesName(Constants.GIT_PREFS);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.pref_git);
	}
}
