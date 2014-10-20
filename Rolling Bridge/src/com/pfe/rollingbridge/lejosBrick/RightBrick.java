package com.pfe.rollingbridge.lejosBrick;

import java.util.ArrayList;

import com.pfe.rollingbridge.bluetooth.BTService;

public class RightBrick extends BrickNXT {

	private RightBrick mInstance;
	
	public RightBrick(int BrickId, BTService service) {
		super(BrickId, service);
		
		mInstance = this;
	}
	
	public void move(final int x, final int y) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				ArrayList<Integer> cmdOnBrick = new ArrayList<Integer>();
				
				System.out.println( "Right Brick Move => x:"+x+", y:"+y );
					
				cmdOnBrick.add(16);
				cmdOnBrick.add(x);
				cmdOnBrick.add(y);
				
				mInstance.sendCommand(cmdOnBrick);
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
	
	public int getRobotPositionX() {
		if(isConnected()) {
			System.out.println( "getRobotPosition X" );
			
			return sendCommandWithReturn(5);
		}
		return 0;
	}
	
	public int getRobotPositionY() {
		if(isConnected()) {
			System.out.println( "getRobotPosition Y" );
				
			return sendCommandWithReturn(7);
		}
		return 0;
	}
}
