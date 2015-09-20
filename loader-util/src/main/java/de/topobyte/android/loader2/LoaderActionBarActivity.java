// Copyright 2015 Sebastian Kuerten
//
// This file is part of android-loader-util.
//
// android-loader-util is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// android-loader-util is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with android-loader-util. If not, see <http://www.gnu.org/licenses/>.

package de.topobyte.android.loader2;

import java.lang.ref.WeakReference;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

public abstract class LoaderActionBarActivity extends ActionBarActivity
		implements LoaderCallbacks<Result>
{
	private static final int MSG_SUCCESS = 1;
	private static final int MSG_FAILURE = 2;

	private final int messageId;

	private LoaderDialog loaderDialog;

	protected abstract boolean performInitialization();

	public abstract void initializationSuccess();

	public abstract void initializationFailure();

	public LoaderActionBarActivity(int messageId)
	{
		this.messageId = messageId;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Log.i("loader", "starting loader");

		loaderDialog = (LoaderDialog) getSupportFragmentManager()
				.findFragmentByTag("dialog");
		if (loaderDialog == null) {
			showLoadDialog();
		}

		LoaderManager loaderManager = getSupportLoaderManager();
		loaderManager.initLoader(0, savedInstanceState, this);
	}

	private void showLoadDialog()
	{
		loaderDialog = LoaderDialog.newInstance(messageId);
		loaderDialog.setCancelable(false);
		loaderDialog.show(getSupportFragmentManager(), "dialog");
	}

	protected void dismissLoadDialog()
	{
		loaderDialog.dismiss();
	}

	@Override
	public Loader<Result> onCreateLoader(int id, Bundle args)
	{
		Log.i("loader", "creating loader instance");
		return new InitializationLoader(this) {

			@Override
			protected boolean performInitialization()
			{
				return LoaderActionBarActivity.this.performInitialization();
			}
		};
	}

	private final Handler handler = new FinishHandler(this);

	static class FinishHandler extends Handler
	{
		private final WeakReference<LoaderActionBarActivity> activityRef;

		FinishHandler(LoaderActionBarActivity activity)
		{
			activityRef = new WeakReference<LoaderActionBarActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			Log.i("loader", "handleMessage()");
			LoaderActionBarActivity activity = activityRef.get();
			if (msg.what == MSG_SUCCESS) {
				if (activity != null) {
					activity.dismissLoadDialog();
					activity.initializationSuccess();
				}
			} else if (msg.what == MSG_FAILURE) {
				if (activity != null) {
					activity.dismissLoadDialog();
					activity.initializationFailure();
				}
			}
		}

	}

	@Override
	public void onLoadFinished(Loader<Result> loader, final Result result)
	{
		Log.i("loader", "onLoadFinished()");
		runOnUiThread(new Runnable() {

			@Override
			public void run()
			{
				if (result == Result.SUCESS) {
					handler.sendEmptyMessage(MSG_SUCCESS);
				} else if (result == Result.FAILURE) {
					handler.sendEmptyMessage(MSG_FAILURE);
				}
			}
		});
	}

	@Override
	public void onLoaderReset(Loader<Result> loader)
	{
		// ?
	}
}
