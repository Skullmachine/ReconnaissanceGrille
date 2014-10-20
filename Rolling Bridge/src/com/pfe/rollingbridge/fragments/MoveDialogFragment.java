package com.pfe.rollingbridge.fragments;

import com.pfe.rollingbridge.R;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;


public class MoveDialogFragment extends DialogFragment {

	private static MoveDialogFragment mInstance = null;
	
	public static MoveDialogFragment newInstance(String title, String message) {
		
		mInstance = new MoveDialogFragment();
		
		Bundle args = new Bundle();
		
		args.putString("title", title);
		args.putString("msg", message);
		
		mInstance.setArguments(args);
		mInstance.setRetainInstance(true);
		mInstance.setCancelable(false);
		
		return mInstance;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		return dialog;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		Bundle args = getArguments();
		View v = inflater.inflate(R.layout.move_dialog, container);
		
		((TextView)v.findViewById(R.id.Dialog_title)).setText(args.getString("title", "Work in Progress"));
		((TextView)v.findViewById(R.id.Dialog_msg)).setText(args.getString("msg", "Please Be Patient ..."));
		
		return v;
	}
}
