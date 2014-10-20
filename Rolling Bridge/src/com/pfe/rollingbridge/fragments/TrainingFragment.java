package com.pfe.rollingbridge.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Switch;

import com.pfe.rollingbridge.MainActivity;
import com.pfe.rollingbridge.R;
import com.pfe.rollingbridge.enumeration.ArmState;
import com.pfe.rollingbridge.enumeration.MessageType;
import com.pfe.rollingbridge.enumeration.PinceState;
import com.pfe.rollingbridge.service.IBluetoothService;
import com.pfe.rollingbridge.views.GameBoard;

public class TrainingFragment extends Fragment implements OnTouchListener, OnClickListener {

	private MainActivity mInstance;
	private GameBoard mGameBoard;
	private Switch mOpenOrClose, mPositionOfArms;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.trainingfragment, container, false);
	
		mGameBoard = (GameBoard)v.findViewById(R.id.trainingGameBoard);
		mOpenOrClose = (Switch) v.findViewById(R.id.pinceState);
		mPositionOfArms = (Switch) v.findViewById(R.id.armsState);
		
		v.findViewById(R.id.reset).setOnClickListener(this);
		mGameBoard.setOnTouchListener(this);
		mOpenOrClose.setOnClickListener(this);
		mPositionOfArms.setOnClickListener(this);
		
		
		return v;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		if(!mInstance.getService().getLeftBrick().isConnected() || !mInstance.getService().getRightBrick().isConnected())
			mInstance.popup("Attention, vous êtes hors ligne !", MessageType.INFO);
		
		//On mets √† jour l'ui en fonction des moteurs
		updateMotorState(mInstance.getService().getLeftBrick().getArmState(), mInstance.getService().getLeftBrick().getPinceState());
		
		//On mets √† jour la position du robot
		updateRobotPosition(mInstance.getService().getRightBrick().getRobotPositionX(), mInstance.getService().getRightBrick().getRobotPositionY());
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		mInstance = (MainActivity) activity;
	}

	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		switch(v.getId()) {
		
			case R.id.trainingGameBoard:
				
				if(event.getAction() == MotionEvent.ACTION_UP) {
					
					if(mInstance.getService().getLeftBrick().isConnected() && mInstance.getService().getRightBrick().isConnected()) {
						//on récupère les coordonn√©es de la flêche pour envoyer au robot
						Point[] coordinate = mGameBoard.getLastArrowCoordinate();
						Point robotPosition = mGameBoard.getRobotPosition();
						
						//On deplace le robot seulement si nous avons des coordonn√©es valides
						if( (robotPosition.x == coordinate[0].x) && (robotPosition.y == coordinate[0].y) && 
							(!coordinate[0].equals(coordinate[1].x, coordinate[1].y))) {
	
								MoveDialogFragment.newInstance("Contrôle du Robot", "Déplacement en Cours").show(mInstance.getFragmentManager(), MainActivity.DIALOG_TAG);
								
								System.out.println("Sending Coordinate : ("+coordinate[1].x+", "+coordinate[1].y+")");
								
								//On demande au robot de bouger 
								mInstance.getService().getLeftBrick().move(coordinate[1].x, coordinate[1].y);
								mInstance.getService().getRightBrick().move(coordinate[1].x, coordinate[1].y);
						}
					}
					return false;
				}
				
			return false;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
			
		switch(v.getId()) {
			case R.id.pinceState:
				if(mInstance.getService().getLeftBrick().isConnected()) {
					System.out.println("Pince State : isChecked = "+mOpenOrClose.isChecked()+" --- "+mOpenOrClose.getText());
					
					MoveDialogFragment.newInstance("Contrôle du Robot", "Déplacement en Cours").show(mInstance.getFragmentManager(), MainActivity.DIALOG_TAG);
					
					//on lance la commande au robot via le service
					if(mOpenOrClose.isChecked()) 
						mInstance.getService().getLeftBrick().open();
					else
						mInstance.getService().getLeftBrick().close();
				}
			break;
			
			case R.id.armsState:
				if(mInstance.getService().getLeftBrick().isConnected()) {
					System.out.println("arm State : isChecked = "+mPositionOfArms.isChecked()+" --- "+mPositionOfArms.getText());
					
					MoveDialogFragment.newInstance("Contrôle du Robot", "Déplacement en Cours").show(mInstance.getFragmentManager(), MainActivity.DIALOG_TAG);
					
					//on lance la commande au robot via le service
					if(mPositionOfArms.isChecked())
						mInstance.getService().getLeftBrick().down();
					else
						mInstance.getService().getLeftBrick().up();
				}
			break;
			
			case R.id.reset:
				if(mInstance.getService().getLeftBrick().isConnected() && mInstance.getService().getRightBrick().isConnected())
					ResetDialogFragment.newInstance(this, "Réinitialisation du Robot", "Confirmez vous vouloir remettre le robot en position initiale ? Attention, lors de la remise"
																						+ " à zéro, le robot va se déplacer.").show(mInstance.getFragmentManager(), MainActivity.DIALOG_TAG);
				else
					mInstance.popup("Merci de vous connecter aux bricks avant de demander une réinitialisation.", MessageType.INFO);
			break;
		}
	}
	 
	public void updateMotorState(ArmState arm, PinceState pince) {
		if(arm == ArmState.DOWN)
			mPositionOfArms.setChecked(true);
		else
			mPositionOfArms.setChecked(false);
		
		if(pince == PinceState.OPEN)
			mOpenOrClose.setChecked(true);
		else
			mOpenOrClose.setChecked(false);
	}
	
	public void updateRobotPosition(int xVal, int yVal) {
			mGameBoard.changeRobotPosition(xVal, yVal);
	}
	
	public void resetRobot() {
		//si le bras est en bas, on le met en haut
		if(!mPositionOfArms.isChecked())
			mInstance.getService().getLeftBrick().up();
		
		//si la pince est ouverte, on la ferme
		if(!mOpenOrClose.isChecked())
			mInstance.getService().getLeftBrick().close();
		
		//on dit au brick de se réinitialiser
		mInstance.getService().getLeftBrick().resetPosition();
		mInstance.getService().getRightBrick().resetPosition();
		
		updateRobotPosition(0, 0);
	}
	
	@SuppressLint("HandlerLeak")
	public Handler mTrainingHandler = new Handler() {
		boolean leftComplete = false, rightComplete = false;
		
		@Override
		public void handleMessage(Message msg) {
			
			switch(msg.what) {
				case IBluetoothService.MOVE_COMPLETE:
					
					if(msg.getData().getInt("BrickNumber") == IBluetoothService.LEFT_DEVICE)
						leftComplete = true;
					else if(msg.getData().getInt("BrickNumber") == IBluetoothService.RIGHT_DEVICE)
						rightComplete = true;
					else {
						leftComplete = false; rightComplete = false;
					}
						
					if(leftComplete && rightComplete) {
						System.out.println("Deplacement Done !");
						
						updateRobotPosition(msg.getData().getInt("xVal"), msg.getData().getInt("yVal"));
						
						//on enleve le message permettant de patienter
						if(mInstance.getFragmentManager().findFragmentByTag(MainActivity.DIALOG_TAG) != null)
							((MoveDialogFragment) mInstance.getFragmentManager().findFragmentByTag(MainActivity.DIALOG_TAG)).dismiss();
						
						leftComplete = false; rightComplete = false;
					}
				break;
			}
		}
	};
}
