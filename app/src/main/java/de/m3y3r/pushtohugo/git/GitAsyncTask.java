package de.m3y3r.pushtohugo.git;

import android.content.Context;
import android.os.AsyncTask;

import java.util.HashMap;

/**
 * Created by thomas on 12.02.2017.
 */

public class GitAsyncTask extends AsyncTask<String, Void, Void> {
	private Context ctx;

	public GitAsyncTask(Context ctx) {
		this.ctx = ctx;
	}

	@Override
	protected Void doInBackground(String[] params) {
		new GitUtil(ctx).createRepoAndAddPost(params[0], params[1], new HashMap<String, String>());
		return null;
	}
}
