package com.pfe.rollingbridge.lejosBrick;

import java.io.IOException;
import java.util.ArrayList;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.pfe.rollingbridge.bluetooth.BTHandler;
import com.pfe.rollingbridge.bluetooth.BTService;
import com.pfe.rollingbridge.io.ReadTask;
import com.pfe.rollingbridge.io.WriteTask;
import com.pfe.rollingbridge.service.IBluetoothService;

abstract class BrickNXT implements Runnable {

	// 5 minutes en milliseconds ( 5 minutes = 300 secondes = 300 000 milliseconds) 
	private final static int CHECK_CONNECTION_DELAY = 60 * 1000;
	
	private int BrickNumber;
	private Handler UIHandler;
	private BTHandler btHandler;
	private NXTComm socket;
	private NXTInfo infoSocket;
	private ReadTask read;
	private WriteTask write;
	private boolean isConnected;
	
	public BrickNXT(int BrickId, BTService service) {
		
		BrickNumber = BrickId;
		
		UIHandler = service.getUIHandler();
		btHandler = service.getBTHandler();
		
		read = null;
		write = null;
		socket = null;
		isConnected = false;
	}
	
	public void sendMessageToUIThread(int what, int xVal, int yVal) {
		Message msg = new Message();
		Bundle b = new Bundle();
		
		b.putInt("BrickNumber", BrickNumber);
		b.putInt("xVal", xVal);
		b.putInt("yVal", yVal);
		
		msg.what = what;
		msg.setData(b);
		
		UIHandler.sendMessage(msg);
	}
	
	public void sendMessageToUIThread(int what, int arg1) {

		Message msg = new Message();
		Bundle b = new Bundle();
			
		b.putInt("Integer", arg1);
			
		msg.what = what;
		msg.setData(b);
			
		UIHandler.sendMessage(msg);
	}
	
	public void connect(String macAddr) {
		try {
			
			infoSocket = new NXTInfo(NXTCommFactory.BLUETOOTH, "NXT",  macAddr);
			socket = NXTCommFactory.createNXTComm(infoSocket.protocol);
			
			new Thread(this).start();
			
		} catch (NXTCommException e) {
			e.printStackTrace();
			sendMessageToUIThread(IBluetoothService.BLUETOOTH_FAIL_CONNECT, BrickNumber);
		}
	}
	
	@Override
	public void run() {
		//On contrôle que le bluetooth est bien actif
		if(!btHandler.initBluetooth()) 
			sendMessageToUIThread(IBluetoothService.BLUETOOTH_FAIL_CONNECT, BrickNumber);
		
		try {
			//on lance la connexion
			if(socket.open(infoSocket)) {
				//on ouvre les flux de data
				read = new ReadTask(socket.getInputStream());
				write = new WriteTask(socket.getOutputStream());
				
				//Nous sommes maintenant connecté
				isConnected = true;
				
				//on identifie la brick à laquelle on se connecte
				int device = identifyBrick();
				
				if(device != BrickNumber) {
					isConnected = false;
					
					read.close();
					write.close();
					socket.close();
					
					sendMessageToUIThread(IBluetoothService.BLUETOOTH_FAIL_CONNECT, BrickNumber);
				}
				else {
					
					//On fait des controles régulier sur la connexion
					mHandler.sendEmptyMessage(BrickNumber);
					
					//Si on arrive ici, tout c'est bien passé, la connexion est OK
					sendMessageToUIThread(IBluetoothService.BLUETOOTH_CONNECTED, BrickNumber);
				}
			}
			else {
				Thread.sleep(1000);
				sendMessageToUIThread(IBluetoothService.BLUETOOTH_FAIL_CONNECT, BrickNumber);
			}
		} catch (Exception e) {
			e.printStackTrace();
			sendMessageToUIThread(IBluetoothService.BLUETOOTH_FAIL_CONNECT, BrickNumber);
		}
	}
	
	private int identifyBrick() {
		
		System.out.println("Identification de la Brick");
		
		return sendCommandWithReturn(0);
	}
	
	/**
	 * Fonction permettant de vérifier la validité de la connexion
	 * 
	 * @param le numero de la brick
	 */
	private void isConnectionAvailable() {
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				int batteryLife = sendCommandWithReturn(42);
				
				if(batteryLife != -1) {
					//6 Piles de 1500mV chacune
					int pourcentBattery = (batteryLife * 100) / (6 * 1500);
					
					if(pourcentBattery < 5) {
						//On a perdu la connexion suite à un probleme de battery
						closeConnection();
						//On le notifie à l'activity
						sendMessageToUIThread(IBluetoothService.LOW_BATTERY, BrickNumber);
					}
					else {
						Message msg = new Message();
						
						msg.what = IBluetoothService.BATTERY_LEVEL;
						msg.arg1 = BrickNumber;
						msg.arg2 = pourcentBattery;
						
						UIHandler.sendMessage(msg);
						mHandler.sendEmptyMessageDelayed(BrickNumber, CHECK_CONNECTION_DELAY);
					}
				}
				else {
					//On a perdu la connexion
					closeConnection();
					//On dit à l'activity qu'on a perdu la connexion :/
					sendMessageToUIThread(IBluetoothService.LOST_CONNECTION, BrickNumber);
				}
			}
		}).start();
	}
	
	public void closeConnection() {
		try {
			if(read != null) read.close();
			if(write != null) write.close();
			if(socket != null) socket.close();
	
			isConnected = false;
			mHandler.removeMessages(BrickNumber);

		} catch(IOException e) { }
	}
	
	public void sendCommand(ArrayList<Integer> command) {
		if(isConnected) {
			try {
				write.writeInFLux(command);
				
				//On d�finit le timeout selon si c'est la p�riode d'identification ou non
				if(command.get(0) == 0 || command.get(0) == 42)
					read.setTimeout(5000);
				else
					read.setTimeout(-1);
				
				//On lit la r�ponse dans le flux
				if( read.readInFlux() == command.get(0) )
					sendMessageToUIThread(IBluetoothService.MOVE_COMPLETE, command.get(1), command.get(2));
			}
			catch(Exception e) {
				sendMessageToUIThread(IBluetoothService.COMMAND_FAILED, -1);
				closeConnection();
			}
		}
	}
	
	public boolean sendCommand(int command) {
		if(isConnected) {
			try {
				write.writeInFLux(command);
				
				//On d�finit le timeout selon si c'est la p�riode d'identification ou non
				if(command == 0 || command == 42)
					read.setTimeout(5000);
				else
					read.setTimeout(-1);
				
				//On lit la r�ponse dans le flux
				if( read.readInFlux() == command ) {
					if(command != 42)
						sendMessageToUIThread(IBluetoothService.COMMAND_SUCCESS, -1);
					return true;
				}
			}
			catch(Exception e) {
				sendMessageToUIThread(IBluetoothService.COMMAND_FAILED, -1);
				closeConnection();
			}
		}
		return false;
	}
	
	public int sendCommandWithReturn(int command) {
		//On test si la connexion est bien réalisé
		if(isConnected) {	
			try {
				write.writeInFLux(command);
				
				//On d�finit le timeout selon si c'est la p�riode d'identification ou non
				if(command == 0 || command == 42)
					read.setTimeout(5000);
				else
					read.setTimeout(-1);
				
				//On lit la r�ponse dans le flux
				int response = read.readInFlux();
				
				if(read.readInFlux() == command)
					return response;
				
			} catch (Exception e) {
				sendMessageToUIThread(IBluetoothService.COMMAND_FAILED, -1);
				closeConnection();
			}
		}
		return -1;
	}
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == BrickNumber) {
				
					//On supprime l'ancien message pour éviter d'empiler la queue
					mHandler.removeMessages(BrickNumber);

					//On verifie la connexion
					isConnectionAvailable();
			}
		}
	};
	
	public boolean isConnected() {
		return isConnected;
	}
}
