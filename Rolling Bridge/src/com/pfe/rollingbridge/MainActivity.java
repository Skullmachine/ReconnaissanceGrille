package com.pfe.rollingbridge;

import java.util.ArrayList;
import java.util.Map.Entry;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pfe.rollingbridge.adapter.BluetoothListAdapter;
import com.pfe.rollingbridge.bluetooth.BTService;
import com.pfe.rollingbridge.enumeration.FragmentWindow;
import com.pfe.rollingbridge.enumeration.MessageType;
import com.pfe.rollingbridge.fragments.CreditFragment;
import com.pfe.rollingbridge.fragments.MoveDialogFragment;
import com.pfe.rollingbridge.fragments.HomeFragment;
import com.pfe.rollingbridge.fragments.PhotoFragment;
import com.pfe.rollingbridge.fragments.PlayFragment;
import com.pfe.rollingbridge.fragments.TrainingFragment;
import com.pfe.rollingbridge.service.BluetoothServiceBinder;
import com.pfe.rollingbridge.service.IBluetoothService;

public class MainActivity extends Activity {

	/* Global Variable */
	public static String DIALOG_TAG = "dialog";
	/* ********** */
	
	private MainActivity mInstance;
	private BluetoothListAdapter mListBluetoothDevice;
	private Entry<String, String> mSelectedDevice1, mSelectedDevice2;
	private ListView mListViewLeft, mListViewRight;
	private IBluetoothService mService;
	private boolean mIsBound, isHandset = true;
	
	private int mScoreABattre = -1;
	private SparseIntArray mSolutionPVC = null;
	private SparseArray<SparseIntArray> departPiece, arriveePiece;

	public void setPiecePosition( SparseArray<SparseIntArray> depart,  SparseArray<SparseIntArray> arrivee) {
		departPiece = depart;
		arriveePiece = arrivee;
	}
	
	public SparseArray<SparseIntArray> getDepartPiece() {
		return departPiece;
	}
	
	public SparseArray<SparseIntArray> getArriveePiece() {
		return arriveePiece;
	}
	
	public void setScoreABattre(int score) {
		mScoreABattre = score;
		
		if(getFragmentManager().findFragmentByTag("play") != null) 
			((PlayFragment) getFragmentManager().findFragmentByTag("play")).setNbCoupABattre(mScoreABattre);
	}
	
	public void setSolutionPVC(SparseIntArray solution) {
		mSolutionPVC = solution;
		
		if(getFragmentManager().findFragmentByTag("play") != null) 
			((PlayFragment) getFragmentManager().findFragmentByTag("play")).setSolutionPVC(mSolutionPVC);
	}
	
	public SparseIntArray getSolutionPVC() {
		return mSolutionPVC;
	}
	
	public int getScoreABattre() {
		return mScoreABattre;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mInstance = this;
		mIsBound = false;
		
    	//On démarre notre service
    	Intent service = new Intent(mInstance, BTService.class);
    	startService(service);
    	
    	//On se connecte au service
    	if(bindService(service, mServiceConnection, BIND_AUTO_CREATE)) mIsBound = true;
    	
    	//on load la vue principale de base
    	getFragmentManager().beginTransaction().add(R.id.fragmentContainer, new HomeFragment(), "home").commit();
    	
    	//on gere la fenetre gauche  
    	if(isHandset)
    		findViewById(R.id.leftBrickBTN).setOnClickListener(mOnClickListener);
    	((TextView) findViewById(R.id.BrickNameLeft)).setTypeface(RollingBridgeApplication.getTypeFace("roboto"));
    	
    	//on gere la fenetre droite    	
    	if(isHandset)
    		findViewById(R.id.RightBrickBTN).setOnClickListener(mOnClickListener);
    	((TextView) findViewById(R.id.BrickNameRight)).setTypeface(RollingBridgeApplication.getTypeFace("roboto"));

    	//On gère les listes bluetooth
    	mListViewLeft = (ListView) findViewById(R.id.BTlistLeft);
    	mListViewRight = (ListView) findViewById(R.id.BTlistRight);
    	
    	//on gere les click sur les items
    	mListViewLeft.setOnItemClickListener(mOnItemClickListener);
    	mListViewRight.setOnItemClickListener(mOnItemClickListener);
    	
    	//on definit l'adapter pour la listView
    	mListBluetoothDevice = new BluetoothListAdapter(mInstance, null);
    	mListViewLeft.setAdapter(mListBluetoothDevice);
    	mListViewRight.setAdapter(mListBluetoothDevice);
    	
    	//On gere les clicks permettant la gestion bluetooth
    	findViewById(R.id.BTconnectLeft).setOnClickListener(mOnClickListener);
    	findViewById(R.id.BTconnectRight).setOnClickListener(mOnClickListener);
    	findViewById(R.id.BtsearchLeft).setOnClickListener(mOnClickListener);
    	findViewById(R.id.BtsearchRight).setOnClickListener(mOnClickListener);
	}
	
	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		
		String tag = (String) findViewById(R.id.home).getTag();
		isHandset = (tag.equals("handset")) ? true : false;
	}
	
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if(mIsBound) {
			//On ferme les connexions vers les bricks
			mService.getRightBrick().closeConnection();
			mService.getLeftBrick().closeConnection();
			
			//On se deconnecte du service
			unbindService(mServiceConnection);
			mIsBound = false;
		}
	}
		
	/**
	 * Fonction permettant d'afficher le menu permettant de gérer les connexions bluetooth
	 * 
	 * @param idLayout id du menu à afficher
	 */
	private void showBrickMenu(final int idLayout) {
		final RelativeLayout rl = (RelativeLayout) findViewById(idLayout);
		final Button leftButton = (Button) findViewById(R.id.leftBrickBTN);
		final Button rightButton = (Button) findViewById(R.id.RightBrickBTN);
		
		//Création des animations
		ObjectAnimator layoutAnimation;
		ObjectAnimator buttonAnimation;

		if(idLayout == R.id.leftBrick) {
			layoutAnimation = ObjectAnimator.ofFloat(rl, View.TRANSLATION_X, 0, rl.getWidth()-2);
			buttonAnimation = ObjectAnimator.ofFloat(leftButton, View.TRANSLATION_X, 0, rl.getWidth()-7);
		}
		else {
			layoutAnimation = ObjectAnimator.ofFloat(rl, View.TRANSLATION_X, 0, -(rl.getWidth()-2));
			buttonAnimation = ObjectAnimator.ofFloat(rightButton, View.TRANSLATION_X, 0, -(rl.getWidth()-7));
		}
		
		//On demarre l'animation
		AnimatorSet setAnimation = new AnimatorSet();
		setAnimation.playTogether(layoutAnimation, buttonAnimation);
		setAnimation.setDuration(500);
		setAnimation.setInterpolator(new DecelerateInterpolator());
		setAnimation.start();
	}
	
	/**
	 * Fonction permettant de cacher le menu permettant de gérer les connexions bluetooth
	 * 
	 * @param idLayout id du menu à cacher
	 */
	private void hideBrickMenu(final int idLayout) {
		final RelativeLayout rl = (RelativeLayout) findViewById(idLayout);
		final Button leftButton = (Button) findViewById(R.id.leftBrickBTN);
		final Button rightButton = (Button) findViewById(R.id.RightBrickBTN);
		
		//Création des animations
		ObjectAnimator layoutAnimation;
		ObjectAnimator buttonAnimation;

		if(idLayout == R.id.leftBrick) { 
			layoutAnimation = ObjectAnimator.ofFloat(rl, View.TRANSLATION_X, rl.getWidth()-2, 0);
			buttonAnimation = ObjectAnimator.ofFloat(leftButton, View.TRANSLATION_X,  rl.getWidth()-7, 0);
		}
		else {
			layoutAnimation = ObjectAnimator.ofFloat(rl, View.TRANSLATION_X, -(rl.getWidth()-2), 0);
			buttonAnimation = ObjectAnimator.ofFloat(rightButton, View.TRANSLATION_X, -(rl.getWidth()-7), 0);
		}

		//On demarre l'animation
		AnimatorSet setAnimation = new AnimatorSet();
		setAnimation.playTogether(layoutAnimation, buttonAnimation);
		setAnimation.setDuration(500);
		setAnimation.setInterpolator(new AccelerateInterpolator());
		setAnimation.start();
	}
	
	/**
	 * Fonction permettant de se deconnecter d'un périphérique bluetooth distant
	 * 
	 * @param device s'il on veut se connecter au device gauche ou droit
	 */
	private void deconnexion(final int device) {

		if(device == IBluetoothService.LEFT_DEVICE) {
			//On se deconnecte de la brick NXT
			mService.getLeftBrick().closeConnection();
			
			((Button) findViewById(R.id.BTconnectLeft)).setText("Connexion");
			changeLayout(R.id.layoutBtConnectedLeft, R.id.layoutBtDeviceLeft);
		}
		else {
			//On se deconnecte de la brick NXT
			mService.getRightBrick().closeConnection();
			
			((Button) findViewById(R.id.BTconnectRight)).setText("Connexion");
			changeLayout(R.id.layoutBtConnectedRight, R.id.layoutBtDeviceRight);
		}
	}
	
	/**
	 * Fonction permettant de lancer la connexion vers un périphérique bluetooth distant
	 * 
	 * @param device s'il on veut se connecter au device gauche ou droit
	 */
	private void connexion(final int device) {
		
		Entry<String, String> deviceSelected = (device == IBluetoothService.LEFT_DEVICE) ? mSelectedDevice2 : mSelectedDevice1;
		
    	if(deviceSelected != null) {
    		
    		if(device == IBluetoothService.LEFT_DEVICE) {
    			//On met à jour l'ui
    			((TextView)findViewById(R.id.connectTextLeftAddr)).setText(deviceSelected.getValue());
    			changeLayout(R.id.layoutBtDeviceLeft, R.id.layoutBtConnectionLeft);
    			findViewById(R.id.BTconnectLeft).setEnabled(false);
    			
    			//On lance la connexion
    			mService.getLeftBrick().connect(deviceSelected.getValue());
    		}
    		else {
    			//On met à jour l'ui
    			((TextView)findViewById(R.id.connectTextRightAddr)).setText(deviceSelected.getValue());
    			changeLayout(R.id.layoutBtDeviceRight, R.id.layoutBtConnectionRight);
    			findViewById(R.id.BTconnectRight).setEnabled(false);
    			
    			//On lance la connexion
    			mService.getRightBrick().connect(deviceSelected.getValue());
    		}
    		
    	}
    	else
    		popup("Please Select a device", MessageType.INFO);
	}
	
	/**
	 * Permet de changer le fragment courant par un autre
	 * 
	 * @param fragment le fragment de destination @see {@link FragmentWindow}
	 * @param animate si l'on veut animer le changement ou non
	 */
	public void changeFragment(FragmentWindow fragment) {
		
		Fragment f = null;
		String fragmentName = null;
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		
		switch(fragment) {
			case HOME: 		f = new HomeFragment(); fragmentName = "home"; 		   break;
			case TRAINING:  f = new TrainingFragment(); fragmentName = "training"; break;
			case PLAY: 		f = new PlayFragment(); fragmentName = "play";         break;
			case CREDITS: 	f = new CreditFragment(); fragmentName = "credits";	   break;
			case PHOTO:		f = new PhotoFragment(); fragmentName = "photo";	   break;
		}
		
		if(f != null) {
			//On ajoute une animation
			if( f instanceof HomeFragment)
				fm.popBackStack();
			else if( f instanceof PlayFragment) {
				//Dans le cas ou on charge PlayFragment c'est qu'on vient de passer par photoFragment, on ne veut pas l'ajouter a la pile
				ft.setCustomAnimations(R.animator.card_flip_right_in, R.animator.card_flip_right_out, 
						   			   R.animator.card_flip_left_in, R.animator.card_flip_left_out)
				  .replace(R.id.fragmentContainer, f, fragmentName)
				  .commit();
			}
			else {
				ft.setCustomAnimations(R.animator.card_flip_right_in, R.animator.card_flip_right_out, 
									   R.animator.card_flip_left_in, R.animator.card_flip_left_out)
				  .replace(R.id.fragmentContainer, f, fragmentName)
				  .addToBackStack(null)
				  .commit();
			}
		}
	}
	
	/**
	 * Dans le cas où une connexion réussie, il faut appeler cette fonction afin de mettre à jour l'interface graphique
	 * (Bouton de Déconnexion, cacher le menu de connexion, informations de connexion)
	 * 
	 * @param device s'il on sait connecté sur le device gauche ou droit @see {@link IBluetoothService} 
	 */
	private void changeLayoutAfterSucceedConnection(int device) {
		if(device != IBluetoothService.RIGHT_DEVICE && device != IBluetoothService.LEFT_DEVICE)
			return;
		
		if(device == IBluetoothService.LEFT_DEVICE) {
			
			popup("Connexion Reussi sur "+mSelectedDevice2.getKey(), MessageType.SUCCESS);
			
			//On change l'UI pour notifier la connexion à l'utilisateur
			((TextView)findViewById(R.id.BTconnectLeft)).setText("Deconnexion");
			findViewById(R.id.BTconnectLeft).setEnabled(true);
			((TextView)findViewById(R.id.connectedLeftName)).setText("Connecté à "+mSelectedDevice2.getKey());
			((TextView)findViewById(R.id.connectedLeftAddr)).setText(mSelectedDevice2.getValue());
		
			//on change les layouts
			changeLayout(R.id.layoutBtConnectionLeft, R.id.layoutBtConnectedLeft);
			if(isHandset)
				hideBrickMenu(R.id.leftBrick);
			
		} else {
			popup("Connexion Reussi sur "+mSelectedDevice1.getKey(), MessageType.SUCCESS);
			
			//On change l'UI pour notifier la connexion à l'utilisateur
			((TextView)findViewById(R.id.BTconnectRight)).setText("Deconnexion");
			findViewById(R.id.BTconnectRight).setEnabled(true);
			((TextView)findViewById(R.id.connectedRightName)).setText("Connecté à "+mSelectedDevice1.getKey());
			((TextView)findViewById(R.id.connectedRightAddr)).setText(mSelectedDevice1.getValue());
			
			//On change les layouts
			changeLayout(R.id.layoutBtConnectionRight, R.id.layoutBtConnectedRight);
			if(isHandset)
				hideBrickMenu(R.id.RightBrick);
		}
	}
	
	private void changeLayoutAfterLostConnection(int device) {	
		if(device != IBluetoothService.RIGHT_DEVICE && device != IBluetoothService.LEFT_DEVICE)
			return;
		
		if(device == IBluetoothService.LEFT_DEVICE) {		
			mSelectedDevice2 = null;
			
			//On change l'UI pour notifier la connexion à l'utilisateur
			((TextView)findViewById(R.id.BTconnectLeft)).setText("Connexion");
			findViewById(R.id.BTconnectLeft).setEnabled(true);
			
			//on change les layouts
			changeLayout(R.id.layoutBtConnectedLeft, R.id.layoutBtDeviceLeft);	
		} else {
			mSelectedDevice1 = null;
			
			//On change l'UI pour notifier la connexion à l'utilisateur
			((TextView)findViewById(R.id.BTconnectRight)).setText("Connexion");
			findViewById(R.id.BTconnectRight).setEnabled(true);
			
			//On change les layouts
			changeLayout(R.id.layoutBtConnectedRight, R.id.layoutBtDeviceRight);
		}
	}
	
	/**
	 * Change les deux layouts de connexion
	 * @param FromLayout Le layout de départ 
	 * @param ToLayout le layout d'arrivée
	 * @throws IllegalArgumentException dans le cas où les deux layouts ne se trouvent pas dans la même zone
	 */
	private void changeLayout(int FromLayout, int ToLayout) throws IllegalArgumentException {
		
		if(FromLayout == ToLayout)
			return;
		
		final RelativeLayout from = (RelativeLayout) findViewById(FromLayout);
		final RelativeLayout to = (RelativeLayout) findViewById(ToLayout);
		
		//si le layout est deja voyant, on arrete la directement
		if(to.getVisibility() == View.VISIBLE)
			return;

		//On verifie que c'est un changement autorisé, sur la meme partie de l'écran
		if (  ((FromLayout == R.id.layoutBtDeviceLeft || FromLayout == R.id.layoutBtConnectionLeft || FromLayout == R.id.layoutBtConnectedLeft) &&
		      (ToLayout == R.id.layoutBtDeviceLeft || ToLayout == R.id.layoutBtConnectionLeft || ToLayout == R.id.layoutBtConnectedLeft)) 
		      || 
		      ((FromLayout == R.id.layoutBtDeviceRight || FromLayout == R.id.layoutBtConnectionRight || FromLayout == R.id.layoutBtConnectedRight) &&
		      (ToLayout == R.id.layoutBtDeviceRight || ToLayout == R.id.layoutBtConnectionRight || ToLayout == R.id.layoutBtConnectedRight)) 
		   ) {
			
			//On rend la premiere vue visible mais totalement transparente
			to.setAlpha(0f);
			to.setVisibility(View.VISIBLE);
			
			//On anime les vues actuelles pour l'interchangeage
			to.animate().alpha(1f).setDuration(700).setListener(null);
			
			from.animate().alpha(0f).setDuration(700).setListener(new AnimatorListener() {
				@Override
				public void onAnimationCancel(Animator arg0) { }
				@Override
				public void onAnimationEnd(Animator arg0) {
					from.setVisibility(View.GONE);
				}
				@Override
				public void onAnimationRepeat(Animator arg0) { }
				@Override
				public void onAnimationStart(Animator arg0) { }
			});
		}
		else
			throw new IllegalArgumentException("Les deux layouts doivent être dans la même zone");
	}
	
	/**
	 * 
	 * Objet permettant de gérer les click sur l'interface principale
	 * Les clicks dans le fragment container seront géré directement dans les Fragments
	 * 
	 */
	private OnClickListener mOnClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			    case R.id.leftBrickBTN:
			    	final RelativeLayout left = (RelativeLayout) findViewById(R.id.leftBrick);
			    	if(left.getTranslationX() > 0) 
			    		hideBrickMenu(R.id.leftBrick);
			    	else 
			    		showBrickMenu(R.id.leftBrick);
			    break;
			    
			    case R.id.RightBrickBTN:
			    	final RelativeLayout right = (RelativeLayout) findViewById(R.id.RightBrick);
			    	if(right.getTranslationX() < 0) 
			    		hideBrickMenu(R.id.RightBrick);
			    	else 
			    		showBrickMenu(R.id.RightBrick);
			    break;
			    
			    case R.id.BTconnectLeft:
			    	if(mService.getLeftBrick().isConnected())			    		
			    		deconnexion(IBluetoothService.LEFT_DEVICE);
			    	else
			    		connexion(IBluetoothService.LEFT_DEVICE);
			    break;
			    
			    case R.id.BTconnectRight:
			    	if(mService.getRightBrick().isConnected())
			    		deconnexion(IBluetoothService.RIGHT_DEVICE);
			    	else 
			    		connexion(IBluetoothService.RIGHT_DEVICE);
			    break;
			    
			    case R.id.BtsearchLeft:
			    case R.id.BtsearchRight:
			    	if(mService.getBTHandler().isSearchIsInProgress()) {
			    		//On stop la recherche
			    		mService.getBTHandler().stopSearchOfDevices();
			    		
			    		((Button) mInstance.findViewById(R.id.BtsearchLeft)).setText("Lancer la Recherche");
			    		((Button) mInstance.findViewById(R.id.BtsearchRight)).setText("Lancer la Recherche");
			    		
			    	} else {
			    		//On vide la liste actuelle
			    		mListBluetoothDevice.clearData();
			    		
			    		//On lance la recherche
			    		mService.getBTHandler().searchDevices();
			    		
			    		((Button) mInstance.findViewById(R.id.BtsearchLeft)).setText("Arrêter la Recherche");
			    		((Button) mInstance.findViewById(R.id.BtsearchRight)).setText("Arrêter la Recherche");
			    	}
			    break;
			}
		}
	};
	
	/**
	 * Objet d'événement permettant la gestion des Clicks sur les items des deux ListView
	 */
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
						
			Entry<String, String> res = mListBluetoothDevice.getItem(position);
			
			if(mSelectedDevice1 != null) {
				if(res.equals(mSelectedDevice1.getValue()))
					popup("Please don't select the same device", MessageType.INFO);
			}
			
			if(mSelectedDevice2 != null) {
				if(res.equals(mSelectedDevice2.getValue()))
					popup("Please don't select the same device", MessageType.INFO);
			}
			
			view.setSelected(true);
				
			if(parent.getId() == mListViewLeft.getId()) {	
				((Button) mInstance.findViewById(R.id.BTconnectLeft)).setText("Connexion à "+res.getKey());
				mSelectedDevice2 = res;
			} else { 
				((Button) mInstance.findViewById(R.id.BTconnectRight)).setText("Connexion à "+res.getKey());
				mSelectedDevice1 = res;
			}
		}
	};
	
	/**
	 * Objet permettant de gérer la connexion / deconnexion à un service android
	 * 
	 */
	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder service) {
			Log.i("BluetoothService", "NxtBrickService Connected !!");
			mService = ((BluetoothServiceBinder)service).getService();
			
			//On enregistre le receiver de message pour les modifications UI
			mService.registerHandler(mServiceHandler);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mIsBound = false;
		}
	};
	
	/**
	 * Objet permettant de récupérer les messages provenant du service sur le mainUI (mis à jour de l'interface)
	 * 
	 */
	@SuppressLint("HandlerLeak")
	private Handler mServiceHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int device = -1;
			Fragment dialog;
			
			switch(msg.what) {
				case IBluetoothService.DEVICE_NOT_SUPPORTED: popup("Device not supported !", MessageType.ERROR); break;	
				
				case IBluetoothService.BLUETOOTH_CONNECTED: 
					device = msg.getData().getInt("Integer");
					changeLayoutAfterSucceedConnection(device);
					
					if(getFragmentManager().findFragmentByTag("training") != null) {
						((TrainingFragment) getFragmentManager().findFragmentByTag("training")).updateMotorState(mService.getLeftBrick().getArmState(), mService.getLeftBrick().getPinceState());
						((TrainingFragment) getFragmentManager().findFragmentByTag("training")).updateRobotPosition(mService.getRightBrick().getRobotPositionX(), mService.getRightBrick().getRobotPositionY());
					}
				break;
				
				case IBluetoothService.MOVE_COMPLETE:
					Message NewMsg = new Message();
					NewMsg.what = msg.what;
					NewMsg.setData(msg.getData());
					
					if(getFragmentManager().findFragmentByTag("training") != null) {
						((TrainingFragment) getFragmentManager().findFragmentByTag("training")).mTrainingHandler.sendMessage(NewMsg);
					}
					else if(getFragmentManager().findFragmentByTag("play") != null) {
						((PlayFragment) getFragmentManager().findFragmentByTag("play")).mPlayHandler.sendMessage(NewMsg);
					}
				break;
				
				case IBluetoothService.LOST_CONNECTION:
					device = msg.getData().getInt("Integer");
					
					if(device == IBluetoothService.LEFT_DEVICE)
						popup("Connexion Perdu sur "+mSelectedDevice2.getKey(), MessageType.ERROR);
					else
						popup("Connexion Perdu sur "+mSelectedDevice1.getKey(), MessageType.ERROR);
					
					changeLayoutAfterLostConnection(device);
				break;
				
				case IBluetoothService.LOW_BATTERY:
					device = msg.getData().getInt("Integer");

					if(device == IBluetoothService.LEFT_DEVICE)
						popup("Batterie Faible : "+mSelectedDevice2.getKey(), MessageType.ERROR);
					else
						popup("Batterie Faible : "+mSelectedDevice1.getKey(), MessageType.ERROR);
					
					changeLayoutAfterLostConnection(device);
				break;
				
				case IBluetoothService.BATTERY_LEVEL:
					updateBatteryLevel(msg.arg1, msg.arg2);
				break;
				
				case IBluetoothService.BLUETOOTH_FAIL_CONNECT:
					device = msg.getData().getInt("Integer");
					
					if(device != IBluetoothService.RIGHT_DEVICE && device != IBluetoothService.LEFT_DEVICE)
						break;
					
					popup("Connexion Echoué, Try Again :)", MessageType.ERROR);
					
					if(device == IBluetoothService.LEFT_DEVICE) {
						//On change l'UI pour notifier la connexion à l'utilisateur
						changeLayout(R.id.layoutBtConnectionLeft, R.id.layoutBtDeviceLeft);
						
						mSelectedDevice2 = null;
						
						//On reactive le bouton
						findViewById(R.id.BTconnectLeft).setEnabled(true);
						((TextView) findViewById(R.id.BTconnectLeft)).setText("Connexion");
					}
					else {
						//On change l'UI pour notifier la connexion à l'utilisateur
						changeLayout(R.id.layoutBtConnectionRight, R.id.layoutBtDeviceRight);
						
						mSelectedDevice1 = null;
						
						//On reactive le bouton
						findViewById(R.id.BTconnectRight).setEnabled(true);
						((TextView) findViewById(R.id.BTconnectRight)).setText("Connexion");
					}
				break;
				
				case IBluetoothService.SAME_ADDR_MAC_FOR_BRICK:
					popup("Choisissez deux Briques différentes pour la connexion", MessageType.INFO);
				break;
				
				case IBluetoothService.COMMAND_SUCCESS:
					dialog = getFragmentManager().findFragmentByTag(DIALOG_TAG);
					
					if(dialog != null) 
						if(dialog instanceof MoveDialogFragment) {
							((MoveDialogFragment) dialog).dismiss();
					
							popup("Command sucess =D", MessageType.SUCCESS);
						}
				break;
				
				case IBluetoothService.COMMAND_FAILED:
					dialog = getFragmentManager().findFragmentByTag(DIALOG_TAG);
					
					if(dialog != null) 
						((MoveDialogFragment) dialog).dismiss();
					
					popup("Command failed :/", MessageType.ERROR);
				break;
				
				case IBluetoothService.DEVICE_DETECTED:
					ArrayList<String> deviceInfo = msg.getData().getStringArrayList("ArrayList");
					mListBluetoothDevice.addItem(deviceInfo.get(0), deviceInfo.get(1));
				break;
			}
		}
	};
	
	private void updateBatteryLevel(int device, int pourcent) {
		ProgressBar b = (device == IBluetoothService.RIGHT_DEVICE) ? (ProgressBar) findViewById(R.id.RightBatteryLevel) : (ProgressBar) findViewById(R.id.leftBatteryLevel);
		TextView t = (device == IBluetoothService.RIGHT_DEVICE) ? (TextView) findViewById(R.id.RightBatteryText) : (TextView) findViewById(R.id.leftBatteryText);
		
		if(pourcent > 50)
			b.getProgressDrawable().setColorFilter(Color.GREEN, Mode.MULTIPLY);
		else if(pourcent >15)
			b.getProgressDrawable().setColorFilter(Color.YELLOW, Mode.MULTIPLY);
		else
			b.getProgressDrawable().setColorFilter(Color.RED, Mode.MULTIPLY);
		
		b.setProgress(pourcent);
		t.setText(pourcent+" %");
	}
	
	/**
	 * Affiche un bref message sur l'écran
	 * 
	 * @param str le message à afficher
	 */
	public void popup(String str, MessageType type) {
		
		final RelativeLayout rl = (RelativeLayout)findViewById(R.id.ToastLayout);

		//on change l'UI
		switch(type) {
			case SUCCESS:
				rl.setBackgroundResource(R.drawable.popup_footer_green);
				((ImageView) findViewById(R.id.toastImage)).setImageResource(R.drawable.green_tick);
			break;
			
			case ERROR:
				rl.setBackgroundResource(R.drawable.popup_footer_red);
				((ImageView) findViewById(R.id.toastImage)).setImageResource(R.drawable.red_cross);
			break;
			
			case INFO:
				rl.setBackgroundResource(R.drawable.popup_footer_yellow);
				((ImageView) findViewById(R.id.toastImage)).setImageResource(R.drawable.yellow_info);
			break;
		}
		
		((TextView)findViewById(R.id.toastMessage)).setText(str);
		
		//on lance l'animation
		Animation fadeIn = AnimationUtils.loadAnimation(mInstance, android.R.anim.fade_in);
		fadeIn.setFillAfter(true);
		rl.setVisibility(View.VISIBLE);
		rl.startAnimation(fadeIn);
		
		//on ferme l'animation
		mServiceHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				System.out.println("FadeOut");
				Animation fadeOut = AnimationUtils.loadAnimation(mInstance, android.R.anim.fade_out);
				fadeOut.setFillAfter(true);
				rl.startAnimation(fadeOut);
			}
		}, 2000);
	}
	
	/**
	 * Fonction permettant de récupérer une instance du Service
	 * 
	 * @return le service si l'activité est connectée à celui-ci, null si elle n'est pas connectée
	 */
	public IBluetoothService getService() {
		if(mIsBound)
			return mService;
		else
			return null;
	}
}
