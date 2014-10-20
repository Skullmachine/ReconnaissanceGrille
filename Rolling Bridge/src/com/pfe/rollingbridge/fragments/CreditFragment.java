package com.pfe.rollingbridge.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pfe.rollingbridge.MainActivity;
import com.pfe.rollingbridge.R;
import com.pfe.rollingbridge.enumeration.MessageType;

public class CreditFragment extends Fragment {

	private MainActivity mInstance;
	private Handler mHandler;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.creditfragment, container, false);
		
		mHandler = new Handler();
		mHandler.postDelayed(mRunnable, 5000);
		return v;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		mInstance = (MainActivity) activity;
	}
	
	@Override
	public void onDestroy() {
		mHandler.removeCallbacks(mRunnable);
		
		super.onDestroy();
	}
	
	private Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			mInstance.popup("Appuies sur Back pour revenir au Menu", MessageType.INFO);
		}
	};
}
