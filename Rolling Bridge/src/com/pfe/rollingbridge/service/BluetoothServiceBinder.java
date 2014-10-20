package com.pfe.rollingbridge.service;

import android.os.Binder;

public class BluetoothServiceBinder extends Binder {
	
	private IBluetoothService mService = null;
	
	public BluetoothServiceBinder(IBluetoothService bluetoothService) {
		super();
		
		mService = bluetoothService;
	}
	
	public IBluetoothService getService() {
		return mService;
	}
}
