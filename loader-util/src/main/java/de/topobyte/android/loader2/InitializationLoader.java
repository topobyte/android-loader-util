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

import android.content.Context;
import androidx.loader.content.AsyncTaskLoader;
import android.util.Log;

public abstract class InitializationLoader extends AsyncTaskLoader<Result>
{

	public InitializationLoader(Context context)
	{
		super(context);
	}

	private boolean started = false;
	private boolean done = false;
	private Result result = null;

	@Override
	public Result loadInBackground()
	{
		Log.i("loader", "loadInBackground()");
		started = true;
		boolean success = performInitialization();
		Log.i("loader", "loadInBackground returned " + success);
		result = success ? Result.SUCESS : Result.FAILURE;
		done = true;
		return result;
	}

	@Override
	protected void onStartLoading()
	{
		if (done) {
			deliverResult(result);
		}
		Log.i("loader", "onStartLoading()");

		if (!started) {
			Log.i("loader", "forceLoad()");
			forceLoad();
		}
	}

	@Override
	protected void onStopLoading()
	{
		Log.i("loader", "onStopLoading()");
	}

	protected abstract boolean performInitialization();

}
