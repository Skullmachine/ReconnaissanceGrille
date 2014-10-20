package com.pfe.rollingbridge.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pfe.rollingbridge.MainActivity;
import com.pfe.rollingbridge.R;
import com.pfe.rollingbridge.RollingBridgeApplication;
import com.pfe.rollingbridge.enumeration.FragmentWindow;

public class HomeFragment extends Fragment implements OnClickListener {

	private MainActivity mInstance;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.homefragment, container, false);
		
		((TextView) v.findViewById(R.id.SchoolTitle)).setTypeface(RollingBridgeApplication.getTypeFace("roboto"));
		((TextView) v.findViewById(R.id.ProjectTitle)).setTypeface(RollingBridgeApplication.getTypeFace("roboto"));
		
		v.findViewById(R.id.playBTN).setOnClickListener(this);
		v.findViewById(R.id.creditsBTN).setOnClickListener(this);
		v.findViewById(R.id.trainingBTN).setOnClickListener(this);
		
		return v;
	}
	
	@Override
	public void onClick(View v) {
		
		switch(v.getId()) {
			case R.id.playBTN:
				mInstance.changeFragment(FragmentWindow.PHOTO);
			break;
			
			case R.id.trainingBTN:
				mInstance.changeFragment(FragmentWindow.TRAINING);
			break;
			
			case R.id.creditsBTN:
				mInstance.changeFragment(FragmentWindow.CREDITS);
			break;
		}	
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		mInstance = (MainActivity) activity;
	}
}
