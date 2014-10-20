package com.pfe.rollingbridge.lejosBrick;

import java.io.IOException;
import java.util.ArrayList;

import com.pfe.rollingbridge.bluetooth.BTService;
import com.pfe.rollingbridge.enumeration.ArmState;
import com.pfe.rollingbridge.enumeration.PinceState;

public class LeftBrick extends BrickNXT  {

	
	protected BrickNXT mInstance;

	public LeftBrick(int BrickId, BTService service) {
		super(BrickId, service);
		
		mInstance = this;
	}
	
	/**
	 * Fonction qui demande au NXT qui contrôle le moteur permettant les mouvements sur l'axe Z de lever la pince
	 * @throws IOException
	 */
	public void up() {
		System.out.println( "up" );
			
		new Thread(new Runnable() {
			@Override
			public void run() {
				mInstance.sendCommand(1);
			}
		}).start();
	}
	
	public void resetPosition() {
		if(isConnected()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					System.out.println("Reset");
					
					mInstance.sendCommand(41);
				}
			}).start();
		}
	}
	
	/**
	 * Fonction qui demande au NXT qui contrôle le moteur permettant les mouvements sur l'axe Z de descendre la pince
	 * @throws IOException
	 */
	public void down() {
		System.out.println( "down" );
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				mInstance.sendCommand(2);
			}
		}).start();
	}
		
	/**
	 * Fonction qui demande au NXT qui contrôle le moteur permettant de contrôler la pince de l'ouvrir
	 * @throws IOException
	 */
	public void open() {		
		System.out.println( "open" );
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				mInstance.sendCommand(3);
			}
		}).start();
	}
	
	/**
	 * Fonction qui demande au NXT qui contrôle le moteur permettant de contrôler la pince de la fermer
	 * @throws IOException
	 */
	public void close() {
		System.out.println( "close" );
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				mInstance.sendCommand(4);
			}
		}).start();
	}
	
	public void move(final int x, final int y) {

		new Thread(new Runnable() {
			@Override
			public void run() {
				ArrayList<Integer> cmdOnBrick = new ArrayList<Integer>();
				
				System.out.println( "Left Brick Move => x:"+x+", y:"+y );
					
				cmdOnBrick.add(17);
				cmdOnBrick.add(x);
				cmdOnBrick.add(y);
				
				mInstance.sendCommand(cmdOnBrick);
			}
		}).start();
	}
	
	public ArmState getArmState() {
		if(isConnected()) {
			
			System.out.println( "getArmState" );
			
			int angle = sendCommandWithReturn(6);

			if(angle > 0)
				return ArmState.DOWN;
			else
				return ArmState.UP;
			
		}
		return null;
	}
	
	public PinceState getPinceState() {
		if(isConnected()) {
			
			System.out.println( "getPinceState" );
				
			int angle = sendCommandWithReturn(5);
				
			if(angle > 0)
				return PinceState.OPEN;
			else
				return PinceState.CLOSE;
		}
		return null;
	}
}
