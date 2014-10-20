package com.pfe.rollingbridge.bluetooth;

import java.util.ArrayList;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.pfe.rollingbridge.service.IBluetoothService;

public class BTHandler extends BroadcastReceiver {

	private static BluetoothAdapter mBluetooth;
	private BTService mService;
	
	public BTHandler(BTService service) {
		mService = service;
		mBluetooth = BluetoothAdapter.getDefaultAdapter();
	}
	
	public boolean initBluetooth() {
		if(mBluetooth == null)
			return false;
		
		//Si le bluetooth n'est pas pas utilisable
		if(!isAvailable())
			return mBluetooth.enable();  //On retourne l'activation du bluetooth (true si succès, false echec)
		
		return true;
	}
	
	public boolean isAvailable() {
		if(mBluetooth == null)
			return false;
		
		return mBluetooth.isEnabled();
	}
	
	public void sendMessageToService(int what, ArrayList<String> data) {
			Message msg = new Message();
			Bundle bundle = new Bundle();
			
			msg.what = what;
			
			bundle.putStringArrayList("ArrayList", data);
			
			msg.setData(bundle);
			
			mService.getUIHandler().sendMessage(msg);
	}
	
	public void searchDevices() {
		//Dans le cas où le bluetooth n'est pas dispo ou pas utilisable
		if(!initBluetooth())
			return;
		
		//Dans le cas où une recherche serait déjà en cours, on la coupe
		stopSearchOfDevices();
		
		//Creation de l'intent de recherche
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND); 
        
        //On enregistre le broadcast receiver et lance la detection
        mService.registerReceiver(this, filter); 
        mBluetooth.startDiscovery();
        
        Log.i("Service", "Recherche peripherique Bluetooth en cours ...");
	}

	public void stopSearchOfDevices() {
		
		if(mBluetooth.isDiscovering()) {
			mService.unregisterReceiver(this);
			mBluetooth.cancelDiscovery();
		}
			
		Log.i("Service", "Arret de la recherche des services ...");
	}
	
	public boolean isSearchIsInProgress() {
		return mBluetooth.isDiscovering();
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		//Si nous trouvons un périphérique
		if(BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {

			//Nous recuperons les infos du peripherique
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			
			//On envoit le message à l'UI Thread pour qu'il note le changement
			ArrayList<String> deviceInfo = new ArrayList<String>();
			deviceInfo.add(device.getName());
			deviceInfo.add(device.getAddress());
			
			sendMessageToService(IBluetoothService.DEVICE_DETECTED, deviceInfo);
			
			//Affichage Console
			Log.i("Service found BTDevices", device.getName()+" - "+device.getAddress());
		}
	}

}
