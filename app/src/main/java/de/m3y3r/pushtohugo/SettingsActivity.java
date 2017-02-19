package de.m3y3r.pushtohugo;

import android.app.Activity;
import android.os.Bundle;

public class SettingsActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.settings);
		// Display the fragment as the main content
		getFragmentManager().beginTransaction()
			.replace(R.id.content, new SettingsFragment())
			.commit();
	}
}
