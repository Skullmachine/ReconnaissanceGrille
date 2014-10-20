package com.pfe.rollingbridge.fragments;

import com.pfe.rollingbridge.R;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;



public class ConfirmDialogFragment extends DialogFragment {
	

	
	private static ConfirmDialogFragment mInstance = null;
	private PlayFragment mFragmentParent;
	
	public static ConfirmDialogFragment newInstance(PlayFragment play, int nbCoup) {
		
		mInstance = new ConfirmDialogFragment();
		
		mInstance.mFragmentParent = play;
		
		Bundle args = new Bundle();
		
		args.putInt("nbcoup", nbCoup);
		
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
		final View v = inflater.inflate(R.layout.confirm_dialog, container);
		
		v.findViewById(R.id.deplacementEnCours).setVisibility(View.GONE);
		v.findViewById(R.id.confirmationDeplacement).setVisibility(View.VISIBLE);
		((TextView) v.findViewById(R.id.confirmDeplacementText)).setText("Ce déplacement coûtera "+args.getInt("nbcoup")+" case(s).");
		
		v.findViewById(R.id.annulerDeplacement).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View buttonView) {
				mInstance.dismiss();
				mFragmentParent.updateResult(false);
			}
		});
		
		v.findViewById(R.id.deplacer).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View buttonView) {
				v.findViewById(R.id.deplacementEnCours).setVisibility(View.VISIBLE);
				v.findViewById(R.id.confirmationDeplacement).setVisibility(View.GONE);
				mFragmentParent.updateResult(true);
			}
		});
		
		return v;
	}
	
	public interface yesOrNoResponseDialog {
		public void updateResult(boolean value);
	}
}

