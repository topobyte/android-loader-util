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

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import de.topobyte.android.loader.Loader.Initializer;

public abstract class LoaderActionBarActivity extends ActionBarActivity
		implements LoaderActivity {
	private int loadDialogMessageId;
	private Loader<LoaderActionBarActivity> loader;

	public LoaderActionBarActivity(int loadDialogMessageId) {
		this.loadDialogMessageId = loadDialogMessageId;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("navigation", "LoaderActionBarActivity.onCreate()");

		loader = new MyLoader(this, new Initializer() {

			@Override
			public void initialize() {
				postLoadCreate(savedInstanceState);
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		loader.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		loader.onPause();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		loader.onSaveInstanceState(outState);
	}

	protected abstract boolean performInitialization(InitializationInfo info);

	private class MyLoader extends Loader<LoaderActionBarActivity> {

		public MyLoader(LoaderActionBarActivity activity,
				Initializer initiliazer) {
			super(activity, initiliazer, loadDialogMessageId);
		}

		@Override
		protected boolean performInitialization(InitializationInfo info) {
			return LoaderActionBarActivity.this.performInitialization(info);
		}

	}
}
