package com.pfe.rollingbridge.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.pfe.rollingbridge.MainActivity;
import com.pfe.rollingbridge.R;
import com.pfe.rollingbridge.enumeration.ArmState;
import com.pfe.rollingbridge.enumeration.MessageType;
import com.pfe.rollingbridge.enumeration.PinceState;
import com.pfe.rollingbridge.fragments.ConfirmDialogFragment.yesOrNoResponseDialog;
import com.pfe.rollingbridge.service.IBluetoothService;
import com.pfe.rollingbridge.views.GameBoard;

public class PlayFragment extends Fragment  implements OnTouchListener, OnClickListener, yesOrNoResponseDialog {
	
	private MainActivity mActivity;
	private TextView tv_coup;
	private GameBoard mGameBoard;
	private Switch mOpenOrClose, mPositionOfArms;
	
	private int currentNbCoup;
	private int nbCoutMax, coutTmp;
	private SparseIntArray mSolutionPVC = null;
	private SparseArray<SparseIntArray> departPiece, arriveePiece;
	private Point[] lastCoordinate;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.playfragment, container, false);
		
		currentNbCoup = 0;
		
		tv_coup = (TextView) v.findViewById(R.id.nbCoup);
		mGameBoard = (GameBoard)v.findViewById(R.id.trainingGameBoard);
		mOpenOrClose = (Switch) v.findViewById(R.id.pinceState);
		mPositionOfArms = (Switch) v.findViewById(R.id.armsState);
		
		v.findViewById(R.id.reset).setOnClickListener(this);
		v.findViewById(R.id.showSolution).setOnClickListener(this);
		
		mGameBoard.setOnTouchListener(this);
		mOpenOrClose.setOnClickListener(this);
		mPositionOfArms.setOnClickListener(this);
		
		departPiece = mActivity.getDepartPiece();
		arriveePiece = mActivity.getArriveePiece();

		if(mActivity.getScoreABattre() != -1) 
			setNbCoupABattre(mActivity.getScoreABattre());
		
		if(mActivity.getSolutionPVC() != null)
			mSolutionPVC = mActivity.getSolutionPVC();
		
		return v;
	}	
	
	@Override
	public void onStart() {
		super.onStart();
		
		//On mets Ã  jour l'ui en fonction des moteurs
		updateMotorState(mActivity.getService().getLeftBrick().getArmState(), mActivity.getService().getLeftBrick().getPinceState());
		
		//On mets Ã  jour la position du robot
		updateRobotPosition(mActivity.getService().getRightBrick().getRobotPositionX(), mActivity.getService().getRightBrick().getRobotPositionY());
	}
	
	private void reloadNbCout() {
		if( currentNbCoup > 1)
			if(nbCoutMax > 1)
				tv_coup.setText(currentNbCoup+" dŽplacements / "+nbCoutMax+ " dŽplacements");
			else
				tv_coup.setText(currentNbCoup+" dŽplacements / "+nbCoutMax+ " dŽplacement");
		else
			if(nbCoutMax > 1)
				tv_coup.setText(currentNbCoup+" dŽplacement / "+nbCoutMax+ " dŽplacements");
			else
				tv_coup.setText(currentNbCoup+" dŽplacement / "+nbCoutMax+ " dŽplacement");
	}
 	
	public void setNbCoupABattre(int nbCout) {
		nbCoutMax = nbCout;
		reloadNbCout();
	}
	
	public void setSolutionPVC(SparseIntArray solution) {
		mSolutionPVC = solution;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		mActivity = (MainActivity) activity;
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		switch(v.getId()) {
		
			case R.id.trainingGameBoard:
				
				if(event.getAction() == MotionEvent.ACTION_UP) {
					if(mActivity.getService().getLeftBrick().isConnected() && mActivity.getService().getRightBrick().isConnected()) {
						//on rŽcupre les coordonnÃ©es de la flche pour envoyer au robot
						lastCoordinate = mGameBoard.getLastArrowCoordinate();
						final Point robotPosition = mGameBoard.getRobotPosition();
						
						//On deplace le robot seulement si nous avons des coordonnÃ©es valides
						if( (robotPosition.x == lastCoordinate[0].x) && (robotPosition.y == lastCoordinate[0].y) && 
							(!lastCoordinate[0].equals(lastCoordinate[1].x, lastCoordinate[1].y))) {
							
								coutTmp = Math.abs(lastCoordinate[1].x - lastCoordinate[0].x) + Math.abs(lastCoordinate[1].y - lastCoordinate[0].y);
	
								final ConfirmDialogFragment dialog = ConfirmDialogFragment.newInstance(this, coutTmp);
								dialog.show(mActivity.getFragmentManager(), MainActivity.DIALOG_TAG);
						}
					}

					return false;
				}
				
			return false;
		}
		return false;
	}
	
	@Override
	public void updateResult(boolean value) {
		if(value) {
			System.out.println("Sending Coordinate : ("+lastCoordinate[1].x+", "+lastCoordinate[1].y+")");
			
			currentNbCoup += coutTmp;
			reloadNbCout();
			
			//On demande au robot de bouger 
			mActivity.getService().getLeftBrick().move(lastCoordinate[1].x, lastCoordinate[1].y);
			mActivity.getService().getRightBrick().move(lastCoordinate[1].x, lastCoordinate[1].y);
		}
	}

	@Override
	public void onClick(View v) {
			
		switch(v.getId()) {
			case R.id.pinceState:
				if(mActivity.getService().getLeftBrick().isConnected()) {
					System.out.println("Pince State : isChecked = "+mOpenOrClose.isChecked()+" --- "+mOpenOrClose.getText());
					
					MoveDialogFragment.newInstance("Contr™le du Robot", "DŽplacement en Cours").show(mActivity.getFragmentManager(), MainActivity.DIALOG_TAG);
					
					//on lance la commande au robot via le service
					if(mOpenOrClose.isChecked()) 
						mActivity.getService().getLeftBrick().open();
					else
						mActivity.getService().getLeftBrick().close();
				}
			break;
			
			case R.id.armsState:
				if(mActivity.getService().getLeftBrick().isConnected()) {
					System.out.println("arm State : isChecked = "+mPositionOfArms.isChecked()+" --- "+mPositionOfArms.getText());
					
					MoveDialogFragment.newInstance("Contr™le du Robot", "DŽplacement en Cours").show(mActivity.getFragmentManager(), MainActivity.DIALOG_TAG);
					
					//on lance la commande au robot via le service
					if(mPositionOfArms.isChecked())
						mActivity.getService().getLeftBrick().down();
					else
						mActivity.getService().getLeftBrick().up();
				}
			break;
			
			case R.id.reset:
				if(mActivity.getService().getLeftBrick().isConnected() && mActivity.getService().getRightBrick().isConnected())
					ResetDialogFragment.newInstance(this, "RŽinitialisation du Robot", "Confirmez vous vouloir remettre le robot en position initiale ? Attention, lors de la remise"
																						+ " ˆ zŽro, le robot va se dŽplacer.").show(mActivity.getFragmentManager(), MainActivity.DIALOG_TAG);
				else
					mActivity.popup("Merci de vous connecter aux bricks avant de demander une rŽinitialisation.", MessageType.INFO);
			break;
			
			case R.id.showSolution:
				if(mSolutionPVC != null) {
					ShowSolutionDialogFragment dialog = ShowSolutionDialogFragment.newInstance(this, mSolutionPVC);
					dialog.show(mActivity.getFragmentManager(), MainActivity.DIALOG_TAG);
				}
				else
					mActivity.popup("Merci de rŽessayer d'ici quelques secondes, nous n'avons pas encore rŽcupŽrŽ la solution.", MessageType.INFO);
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
			mActivity.getService().getLeftBrick().up();
		
		//si la pince est ouverte, on la ferme
		if(!mOpenOrClose.isChecked())
			mActivity.getService().getLeftBrick().close();
		
		//on dit au brick de se rŽinitialiser
		mActivity.getService().getLeftBrick().resetPosition();
		mActivity.getService().getRightBrick().resetPosition();
		
		updateRobotPosition(0, 0);
	}
	
	@SuppressLint("HandlerLeak")
	public Handler mPlayHandler = new Handler() {
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
						if(mActivity.getFragmentManager().findFragmentByTag(MainActivity.DIALOG_TAG) != null)
							((ConfirmDialogFragment) mActivity.getFragmentManager().findFragmentByTag(MainActivity.DIALOG_TAG)).dismiss();
						
						if(currentNbCoup > nbCoutMax)
							Toast.makeText(mActivity, "Vous avez Perdu ... Ce n'est pas si facile :s", Toast.LENGTH_LONG).show();
						
						leftComplete = false; rightComplete = false;
					}
				break;
			}
		}
	};
}
