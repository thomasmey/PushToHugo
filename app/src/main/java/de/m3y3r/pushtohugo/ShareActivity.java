package de.m3y3r.pushtohugo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;

import de.m3y3r.pushtohugo.git.GitAsyncTask;
import de.m3y3r.pushtohugo.git.GitUtil;

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
            if ("text/plain".equals(type)) {
                String title = intent.getStringExtra(Intent.EXTRA_SUBJECT);
                String url = intent.getStringExtra(Intent.EXTRA_TEXT);
                new GitAsyncTask().execute(title, url);
            }
        }
        finish();
    }
}
