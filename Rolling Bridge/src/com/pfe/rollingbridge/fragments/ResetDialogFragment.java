package com.pfe.rollingbridge.fragments;

import com.pfe.rollingbridge.R;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ResetDialogFragment extends DialogFragment {

	private static ResetDialogFragment mInstance = null;
	private static Fragment parent = null;
	private TextView dialog_msg;
	private LinearLayout yesOrNo;
	
	public static ResetDialogFragment newInstance(Fragment f, String title, String message) {
		
		mInstance = new ResetDialogFragment();
		
		Bundle args = new Bundle();
		
		args.putString("title", title);
		args.putString("msg", message);
		
		mInstance.setArguments(args);
		mInstance.setRetainInstance(true);
		mInstance.setCancelable(false);
		
		parent = f;
		
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
		View v = inflater.inflate(R.layout.reset_dialog, container);
		
		((TextView)v.findViewById(R.id.Dialog_title)).setText(args.getString("title", "Work in Progress"));
		
		yesOrNo = (LinearLayout) v.findViewById(R.id.yesOrNoQuestion);
		
		dialog_msg = (TextView)v.findViewById(R.id.Dialog_msg);
		dialog_msg.setText(args.getString("msg", "Please Be Patient ..."));
		
		v.findViewById(R.id.yes).setOnClickListener(mOnClick);
		v.findViewById(R.id.no).setOnClickListener(mOnClick);
		
		return v;
	}
	
	private OnClickListener mOnClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
				case R.id.yes:
					//dans le cas où c'est l'acquiessement
					if(yesOrNo.findViewById(R.id.no) == null) 
						mInstance.dismiss();
					else {
						if(parent instanceof TrainingFragment) {
							//On reset le robot
							((TrainingFragment) parent).resetRobot();
							
							dialog_msg.setText("Merci de Positionner le robot sur la case (0,0)");
							
							//On supprime le no, on demande juste à l'utilisateur d'acquiesser
							yesOrNo.removeView(yesOrNo.findViewById(R.id.no));
						}
					}
				break;
				
				case R.id.no:
					mInstance.dismiss();
				break;
			}
		}
	};
}
