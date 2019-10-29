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

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;

public class LoaderDialog extends DialogFragment
{

	public static LoaderDialog newInstance(int messageId)
	{
		LoaderDialog dialog = new LoaderDialog();
		Bundle bundle = new Bundle(1);
		bundle.putInt("message", messageId);
		dialog.setArguments(bundle);
		return dialog;
	}

	private int messageId;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		messageId = getArguments().getInt("message");
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		String message = getString(messageId);
		ProgressDialog startupDialog = ProgressDialog.show(getActivity(), "",
				message, true);
		return startupDialog;
	}
}
