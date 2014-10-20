package com.pfe.rollingbridge;

import java.util.HashMap;
import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;

public class RollingBridgeApplication extends Application {

	private static Context mContext;
	private static HashMap<String, Typeface> mAppTypeFace;
	
	public RollingBridgeApplication() {
		mContext = this;
		mAppTypeFace = new HashMap<String, Typeface>();
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		mAppTypeFace.put("roboto", Typeface.createFromAsset(getAssets(),"fonts/Roboto-LightItalic.ttf"));
	}
	
	public static Context getContext() {
		return mContext;
	}
	
	public static Typeface getTypeFace(String name) {
		if(mAppTypeFace.containsKey(name)) 
			return mAppTypeFace.get(name);
		else
			return null;
	}
}