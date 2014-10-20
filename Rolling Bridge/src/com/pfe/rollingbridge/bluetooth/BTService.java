package com.pfe.rollingbridge.bluetooth;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.pfe.rollingbridge.lejosBrick.LeftBrick;
import com.pfe.rollingbridge.lejosBrick.RightBrick;
import com.pfe.rollingbridge.service.BluetoothServiceBinder;
import com.pfe.rollingbridge.service.IBluetoothService;

public class BTService extends Service implements IBluetoothService {
	/** 
	 * Down / Up  --> Brick Gauche - Motor C (oS2 - iS2)
	 * Close / Open --> Brick Gauche - Motor B (oS2 - iS2)
	 * Move en Y roue gauche --> Brick Gauche Motor A (os2 - iS2)
	 * 
	 * Move en Y roue droite --> Brick Droite Motor A (oS1 - iS1)
	 * Move en X roue ficelle --> Brick Droite Motor B (oS1 - iS1)
	 */

	private Handler mUIHandler;
	private BluetoothServiceBinder binder;
	private BTHandler mBTHandler;
	private LeftBrick left;
	private RightBrick right;


	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.d("RollingBridgeService", "Service created !");
		
		binder = new BluetoothServiceBinder(this);
		mBTHandler = new BTHandler(this);
		
		//initialisation du Bluetooth
		mBTHandler.initBluetooth();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		left.closeConnection();
		right.closeConnection();

		mBTHandler.stopSearchOfDevices();
	}
	
	/**
	 * Connexion d'un client au service
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
						
	/**
	 * Définir le Messager du mainUI
	 * 
	 */
	@Override
	public void registerHandler(Handler UIHandler) {
		mUIHandler = UIHandler;
		
		//On initialise les valeurs uniquement si nous disposons du thread UI
		left = new LeftBrick(IBluetoothService.LEFT_DEVICE, this);
		right = new RightBrick(IBluetoothService.RIGHT_DEVICE, this);
	}
	
	/**
	 * Get le manager du Bluetooth
	 */
	@Override
	public BTHandler getBTHandler() {
		return mBTHandler;
	}
	
	@Override 
	public Handler getUIHandler() {
		return mUIHandler;
	}

	/**
	 * Permet de récupérer l'instance de la brick gauche
	 */
	@Override
	public LeftBrick getLeftBrick() {
		return left;
	}

	@Override
	public RightBrick getRightBrick() {
		return right;
	}
}
