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

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class LoaderDialog extends DialogFragment
{

	private Activity activity;
	private int messageId;

	public LoaderDialog(Activity activity, int messageId)
	{
		this.activity = activity;
		this.messageId = messageId;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		String message = getString(messageId);
		ProgressDialog startupDialog = ProgressDialog.show(activity, "",
				message, true);
		return startupDialog;
	}
}
