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

package de.topobyte.android.loader3;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import de.topobyte.android.loader2.LoaderDialog;
import de.topobyte.android.loader3.TaskFragment.TaskCallbacks;

public abstract class LoaderActionBarActivity extends AppCompatActivity
		implements TaskCallbacks
{

	private static final String TAG_LOAD_DIALOG = "load_dialog";
	private static final String TAG_TASK_FRAGMENT = "task_fragment";

	private TaskFragment taskFragment;

	private final int messageId;

	private LoaderDialog loaderDialog = null;

	private boolean initializationDone = false;
	private boolean initializationSucceeded = false;
	private boolean movedOnToMainUI = false;

	private boolean beforeOnSaveInstanceState = false;

	public LoaderActionBarActivity(int messageId)
	{
		this.messageId = messageId;
	}

	public abstract TaskFragment createTaskFragment();

	public abstract void initializationSuccess();

	public abstract void initializationFailure();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.i("loader", "onCreate");
		super.onCreate(savedInstanceState);

		FragmentManager fm = getSupportFragmentManager();

		Log.i("loader", "initializationDone? " + initializationDone);
		Log.i("loader", "initializationSucceeded? " + initializationSucceeded);

		taskFragment = (TaskFragment) fm
				.findFragmentByTag(TAG_TASK_FRAGMENT);
		if (taskFragment != null) {
			Log.i("loader", "task fragment is not null");
			initializationDone = taskFragment.hasFinished();
			initializationSucceeded = taskFragment.getResult();
		}

		if (initializationDone) {
			tryToMoveOnToMainUI();
			return;
		}

		if (!initializationDone) {
			loaderDialog = (LoaderDialog) fm.findFragmentByTag(TAG_LOAD_DIALOG);

			if (loaderDialog == null) {
				Log.i("loader", "dialog is null");
				showLoadDialog();
			}

			// If the Fragment is non-null, then it is currently being
			// retained across a configuration change.
			if (taskFragment == null) {
				Log.i("loader", "task fragment is null");
				taskFragment = createTaskFragment();
				fm.beginTransaction().add(taskFragment, TAG_TASK_FRAGMENT)
						.commit();
			}
		}
	}

	protected boolean resumed = false;

	@Override
	protected void onResume()
	{
		super.onResume();
		resumed = true;
		beforeOnSaveInstanceState = true;

		if (initializationDone && !movedOnToMainUI) {
			tryToMoveOnToMainUI();
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		resumed = false;
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		beforeOnSaveInstanceState = false;
		Log.i("loader", "onSaveInstanceState");
	}

	private void showLoadDialog()
	{
		loaderDialog = LoaderDialog.newInstance(messageId);
		loaderDialog.setCancelable(false);
		loaderDialog.show(getSupportFragmentManager(), TAG_LOAD_DIALOG);
	}

	@Override
	public void onPreExecute()
	{
		Log.i("loader", "onPreExecute");
	}

	@Override
	public void onCancelled()
	{
		Log.i("loader", "onCancelled");
	}

	@Override
	public void onPostExecute(boolean result)
	{
		Log.i("loader", "onPostExecute");
		initializationDone = true;
		initializationSucceeded = result;
		Log.i("loader", "beforeOnSaveInstance? " + beforeOnSaveInstanceState);
		if (beforeOnSaveInstanceState) {
			Log.i("loader", "dialog != null? " + (loaderDialog != null));
			tryToMoveOnToMainUI();
		}
	}

	private void tryToMoveOnToMainUI()
	{
		tryToDismissLoaderDialog();
		if (initializationSucceeded) {
			initializationSuccess();
		} else {
			initializationFailure();
		}
		movedOnToMainUI = true;
	}

	private void tryToDismissLoaderDialog()
	{
		if (loaderDialog != null) {
			loaderDialog.dismiss();
		}
	}

}
