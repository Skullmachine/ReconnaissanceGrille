package com.pfe.rollingbridge.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.pfe.rollingbridge.R;
import com.pfe.rollingbridge.views.HoughView;


public class ShowSolutionDialogFragment extends DialogFragment {

	private static ShowSolutionDialogFragment mInstance = null;
	
	public static ShowSolutionDialogFragment newInstance(Fragment f, SparseIntArray solution) {
		
		mInstance = new ShowSolutionDialogFragment();
		
		Bundle args = new Bundle();
		
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0 ; i < solution.size() ; i++) {
			int key = solution.keyAt(i);
			
			switch(key) {
				case HoughView.indiceRose: sb.append((i != solution.size()-1) ? "Rose -> " : "Rose"); break;
				case HoughView.indiceBleu: sb.append((i != solution.size()-1) ? "Bleu -> " : "Bleu"); break;
				case HoughView.indiceJaune: sb.append((i != solution.size()-1) ?"Jaune -> ": "Jaune"); break;
				case HoughView.indiceRouge: sb.append((i != solution.size()-1) ?"Rouge -> ":"Rouge"); break;
				case HoughView.indiceVert: sb.append((i != solution.size()-1) ?"Vert -> ":"Vert"); break;
			}
		}
		
		args.putString("solution", sb.toString());
		
		mInstance.setArguments(args);
		mInstance.setRetainInstance(true);
		mInstance.setCancelable(true);
		
		return mInstance;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		dialog.setCanceledOnTouchOutside(true);
		
		return dialog;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		Bundle args = getArguments();
		View v = inflater.inflate(R.layout.solution_dialog, container);
		
		((TextView)v.findViewById(R.id.Dialog_title)).setText(args.getString("title", "Solution du PVC"));
		
		((TextView)v.findViewById(R.id.Dialog_msg)).setText(args.getString("solution", "Solution ..."));
		
		return v;
	}
}
