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

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * This Fragment manages a single background task and retains itself across
 * configuration changes.
 */
public abstract class TaskFragment extends Fragment
{

	public abstract boolean performInitialization();

	/**
	 * Callback interface through which the fragment will report the task's
	 * progress and results back to the Activity.
	 */
	interface TaskCallbacks
	{
		void onPreExecute();

		void onCancelled();

		void onPostExecute(boolean result);
	}

	private TaskCallbacks mCallbacks;
	private InitTask mTask;

	/**
	 * Hold a reference to the parent Activity so we can report the task's
	 * current progress and results. The Android framework will pass us a
	 * reference to the newly created Activity after each configuration change.
	 */
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		mCallbacks = (TaskCallbacks) activity;
	}

	/**
	 * This method will only be called once when the retained Fragment is first
	 * created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Retain this fragment across configuration changes.
		setRetainInstance(true);

		// Create and execute the background task.
		mTask = new InitTask();
		mTask.execute();
	}

	/**
	 * Set the callback to null so we don't accidentally leak the Activity
	 * instance.
	 */
	@Override
	public void onDetach()
	{
		super.onDetach();
		mCallbacks = null;
	}

	/**
	 * A dummy task that performs some (dumb) background work and proxies
	 * progress updates and results back to the Activity.
	 * 
	 * Note that we need to check if the callbacks are null in each method in
	 * case they are invoked after the Activity's and Fragment's onDestroy()
	 * method have been called.
	 */
	private class InitTask extends AsyncTask<Void, Void, Boolean>
	{

		@Override
		protected void onPreExecute()
		{
			if (mCallbacks != null) {
				mCallbacks.onPreExecute();
			}
		}

		/**
		 * Note that we do NOT call the callback object's methods directly from
		 * the background thread, as this could result in a race condition.
		 */
		@Override
		protected Boolean doInBackground(Void... ignore)
		{
			boolean success = performInitialization();
			return success;
		}

		@Override
		protected void onProgressUpdate(Void... ignore)
		{

		}

		@Override
		protected void onCancelled()
		{
			if (mCallbacks != null) {
				mCallbacks.onCancelled();
			}
		}

		@Override
		protected void onPostExecute(Boolean result)
		{
			TaskFragment.this.finished = true;
			TaskFragment.this.result = result;
			if (mCallbacks != null) {
				mCallbacks.onPostExecute(result);
			}
		}
	}

	private boolean finished = false;
	private boolean result = false;

	public boolean hasFinished()
	{
		return finished;
	}

	public boolean getResult()
	{
		return result;
	}

}
