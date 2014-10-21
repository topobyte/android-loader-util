// Copyright 2014 Sebastian Kuerten
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

package de.topobyte.android.loader;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public abstract class Loader<T extends FragmentActivity & LoaderActivity> {
	private static final String LOG_TAG = "loader";

	private static final String STATE_DONE = "state_done";
	private static final String STATE_SUCCESS = "state_sucess";

	public static final String PREF_LAST_VERSION = "lastSeenVersion";

	private T activity;
	private Initializer initializer;
	private int messageId;

	private boolean workerRunning = false;
	private boolean done = false;
	private boolean resumed = false;
	private boolean beforeSaveInstanceState = true;

	private SharedPreferences preferences;
	private int storedVersion;
	private int currentVersion;
	private boolean versionUpdate = false;
	private boolean postInitializationDone = false;

	private LoaderDialog loaderDialog;

	public interface Initializer {
		public void initialize();
	}

	public Loader(T activity, Bundle bundle, Initializer initializer,
			int messageId) {
		this.activity = activity;
		this.initializer = initializer;
		this.messageId = messageId;

		if (bundle != null) {
			done = bundle.getBoolean(STATE_DONE);
			initializationSucceeded = bundle.getBoolean(STATE_SUCCESS);
			if (done) {
				postInitialization();
				return;
			}
		}

		preferences = PreferenceManager.getDefaultSharedPreferences(activity);

		storedVersion = preferences.getInt(PREF_LAST_VERSION, 0);

		currentVersion = 0;
		try {
			PackageInfo pinfo = activity.getPackageManager().getPackageInfo(
					activity.getPackageName(), 0);
			currentVersion = pinfo.versionCode;
			versionUpdate = currentVersion != storedVersion;
		} catch (NameNotFoundException e) {
			Log.e(LOG_TAG, "unable to get version info");
		}

		Log.i(LOG_TAG, "stored version: " + storedVersion);
		Log.i(LOG_TAG, "current version: " + currentVersion);

		if (versionUpdate) {
			Log.i(LOG_TAG, "version update");
		} else {
			Log.i(LOG_TAG, "version did not change");
		}
	}

	protected void postInitialization() {
		Log.i(LOG_TAG, "Loader.postInitialization. done? "
				+ postInitializationDone);
		if (postInitializationDone) {
			return;
		}
		postInitializationDone = true;
		if (loaderDialog != null) {
			loaderDialog.dismiss();
		}
		if (initializationSucceeded) {
			initializer.initialize();
			if (resumeInternal) {
				dispatchOnResume();
			}
		} else {
			Dialog errorDialog = activity.createErrorDialog();
			DialogWrapper wrapper = new DialogWrapper(errorDialog);
			wrapper.setCancelable(false);
			wrapper.show(activity.getSupportFragmentManager(), "dialog");
		}
	}

	private boolean resumeInternal = false;

	protected void onResume() {
		Log.i(LOG_TAG, "Loader.onResume() done? " + done);
		resumeInternal = true;
		beforeSaveInstanceState = true;
		if (done) {
			postInitialization();
			if (initializationSucceeded) {
				dispatchOnResume();
			}
			return;
		}
		if (!workerRunning) {
			workerRunning = true;
			loaderDialog = new LoaderDialog(activity, messageId);
			loaderDialog.setCancelable(false);
			loaderDialog.show(activity.getSupportFragmentManager(), "dialog");
			startWorkerThread();
		}
	}

	protected void onPause() {
		resumeInternal = false;
		if (initializationSucceeded) {
			dispatchOnPause();
		}
	}

	private void dispatchOnResume() {
		if (!resumed) {
			activity.myResume();
			resumed = true;
		}
	}

	private void dispatchOnPause() {
		if (resumed) {
			activity.myPause();
			resumed = false;
		}
	}

	public void onSaveInstanceState(Bundle outState) {
		beforeSaveInstanceState = false;
		outState.putBoolean(STATE_DONE, done);
		outState.putBoolean(STATE_SUCCESS, initializationSucceeded);
	}

	private void startWorkerThread() {
		Log.d(LOG_TAG, "starting worker thread");
		LoadWorker worker = new LoadWorker();
		Thread thread = new Thread(worker);
		thread.start();
	}

	private boolean initializationSucceeded = false;

	private class LoadWorker implements Runnable {

		@Override
		public void run() {
			InitializationInfo info = new InitializationInfo(versionUpdate,
					storedVersion, currentVersion);
			initializationSucceeded = performInitialization(info);

			if (initializationSucceeded && versionUpdate) {
				Editor edit = preferences.edit();
				edit.putInt(PREF_LAST_VERSION, currentVersion);
				edit.commit();
			}

			activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (beforeSaveInstanceState) {
						postInitialization();
					}
					done = true;
				}
			});
		}

	}

	/*
	 * run from LoadWorker thread
	 */
	protected abstract boolean performInitialization(InitializationInfo info);

}
