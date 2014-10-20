package com.pfe.rollingbridge.service;

import com.pfe.rollingbridge.bluetooth.BTHandler;
import com.pfe.rollingbridge.lejosBrick.LeftBrick;
import com.pfe.rollingbridge.lejosBrick.RightBrick;

import android.os.Handler;

public interface IBluetoothService {
	
	//Brick Number Id
	public static final int RIGHT_DEVICE = 1;
	public static final int LEFT_DEVICE = 2;
	
	//Bluetooth Error
	public static final int DEVICE_NOT_SUPPORTED = 5;
	public static final int SAME_ADDR_MAC_FOR_BRICK = 6;
	public static final int DEVICE_DETECTED = 7;
	public static final int LOST_CONNECTION = 8;
	
	//Operation failed
	public static final int COMMAND_FAILED = 12;
	public static final int BLUETOOTH_FAIL_CONNECT = 14;
	public static final int LOW_BATTERY = 16;
	
	//Operation Succeed
	public static final int COMMAND_SUCCESS = 11;
	public static final int BLUETOOTH_CONNECTED = 13;
	public static final int MOVE_COMPLETE = 15;
	public static final int BATTERY_LEVEL = 17;
	
	//Function to register the UI Handler
	public void registerHandler(Handler UIHandler);
	
	//Function to get instance of BTHandler
	public BTHandler getBTHandler();
	public LeftBrick getLeftBrick();
	public RightBrick getRightBrick();
	public Handler getUIHandler();
}
