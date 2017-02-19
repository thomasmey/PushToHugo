package de.m3y3r.pushtohugo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.eclipse.jgit.util.HttpSupport;

import de.m3y3r.pushtohugo.git.GitAsyncTask;

/**
 * Created by thomas on 12.02.2017.
 */
public class ShareActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get intent, action and MIME type
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		if (Intent.ACTION_SEND.equals(action)) {
			if (HttpSupport.TEXT_PLAIN.equals(type)) {
				String title = intent.getStringExtra(Intent.EXTRA_SUBJECT);
				String url = intent.getStringExtra(Intent.EXTRA_TEXT);
				GitAsyncTask t = new GitAsyncTask(getApplicationContext());
				t.execute(title, url);
			}
		}
		finish();
	}
}
