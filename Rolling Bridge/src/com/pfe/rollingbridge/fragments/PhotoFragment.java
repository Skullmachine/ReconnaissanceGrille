package com.pfe.rollingbridge.fragments;


import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.pfe.rollingbridge.MainActivity;
import com.pfe.rollingbridge.R;
import com.pfe.rollingbridge.camera.PictureHandler;
import com.pfe.rollingbridge.enumeration.FragmentWindow;
import com.pfe.rollingbridge.enumeration.MessageType;
import com.pfe.rollingbridge.pvc.Solver;
import com.pfe.rollingbridge.views.HoughView;

public class PhotoFragment extends Fragment implements OnClickListener, SurfaceHolder.Callback {
	
	public static final int DRAW_LINE = 2;
	
	private Camera camera;
	private int cameraId = -1;
	private MainActivity mInstance;
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	private RelativeLayout mCameraPreview, mHoughDetection;
	private HoughView hv;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	    if (!mInstance.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
	    	mInstance.popup("Aucune caméra présente sur ce périphérique", MessageType.ERROR );
	    
	    else {
	      cameraId = findBackCamera();
	      
	      if (cameraId < 0) 
	    	  mInstance.popup("Erreur : Caméra Non Disponible", MessageType.ERROR );
	    }
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.capture_photo_layout, container, false);
		
		v.findViewById(R.id.capturePhoto).setOnClickListener(this);
		v.findViewById(R.id.FakeButton1).setOnClickListener(this);
		v.findViewById(R.id.FakeButton2).setOnClickListener(this);
		v.findViewById(R.id.FakeButton3).setOnClickListener(this);
		v.findViewById(R.id.FakeRealButton1).setOnClickListener(this);
		v.findViewById(R.id.FakeRealButton2).setOnClickListener(this);
		v.findViewById(R.id.FakeRealButton3).setOnClickListener(this);
		v.findViewById(R.id.resetDetection).setOnClickListener(this);
		v.findViewById(R.id.confirmHoughDetection).setOnClickListener(this);
		
		mSurfaceView = (SurfaceView) v.findViewById(R.id.surface_camera);
		mCameraPreview = (RelativeLayout) v.findViewById(R.id.CameraPreview);
		mHoughDetection = (RelativeLayout) v.findViewById(R.id.HoughDetection);
		hv = (HoughView) mHoughDetection.findViewById(R.id.houghBitmap);
		
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		
		return v;
	}
	
	private int findBackCamera() {
	    int cameraId = -1;
	    int numberOfCameras = Camera.getNumberOfCameras();
	    
	    for (int i = 0; i < numberOfCameras; i++) {
	    	
	      CameraInfo info = new CameraInfo();
	      Camera.getCameraInfo(i, info);
	      
	      if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
	    	System.out.println("Camera Found");
	        cameraId = i;
	        
	        break;
	      }
	    }
	    return cameraId;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		mInstance = (MainActivity) activity;
	}
	
	@Override
	public void onResume() {
		if(cameraId >= 0) 
			camera = Camera.open(cameraId);
		
		super.onResume();
	}
	
	@Override
	public void onPause() {
		if (camera != null) {
			camera.release();
			camera = null;
		}
	    super.onPause();
	}
	
	private void animateAfterTakePhoto() {
		HoughView hv = (HoughView) mHoughDetection.findViewById(R.id.houghBitmap);
		
		ScaleAnimation Scaleanimation = new ScaleAnimation(1f, (float)hv.getWidth() / (float)mCameraPreview.getWidth(), 
												      1f, (float)hv.getHeight() / (float)mCameraPreview.getHeight(),
												      Animation.RELATIVE_TO_SELF, 0.5f,
												      Animation.RELATIVE_TO_SELF, 0.2f);
		
		Scaleanimation.setDuration(200);
		
		Scaleanimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation arg0) {
				mCameraPreview.setAlpha(0f);
			}
			@Override
			public void onAnimationRepeat(Animation arg0) {}
			@Override
			public void onAnimationStart(Animation arg0) {
				mHoughDetection.animate().alpha(1f).setDuration(600).setListener(null);
			}
		});
		
		mCameraPreview.startAnimation(Scaleanimation);
	}
	
	public void resetPicturePhoto() {
		final HoughView hv = (HoughView) mHoughDetection.findViewById(R.id.houghBitmap);
		
		ScaleAnimation Scaleanimation = new ScaleAnimation(1f, (float) mCameraPreview.getWidth() / (float)hv.getWidth(),
												           1f, (float) mCameraPreview.getHeight() / (float)hv.getHeight(),
												           Animation.RELATIVE_TO_SELF, 0.5f,
												           Animation.RELATIVE_TO_SELF, 0.2f);
		
		camera.startPreview();
		
		Scaleanimation.setDuration(600);
		
		Scaleanimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation arg0) {
				mHoughDetection.setAlpha(0f);
				hv.cleanCanvas();
			}
			@Override
			public void onAnimationRepeat(Animation arg0) {}
			@Override
			public void onAnimationStart(Animation arg0) {
				mCameraPreview.animate().alpha(1f).setDuration(600).setListener(null);
			}
		});
		
		hv.startAnimation(Scaleanimation);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.capturePhoto:
			case R.id.FakeButton1:
			case R.id.FakeButton2:
			case R.id.FakeButton3:
			case R.id.FakeRealButton1:
			case R.id.FakeRealButton2:
			case R.id.FakeRealButton3:
				
				if(camera != null) {
					camera.takePicture(null, null, new PictureHandler(this, v.getId()));
					
					hv.defineRobotPosition(new Point(mInstance.getService().getRightBrick().getRobotPositionX(),
													 mInstance.getService().getRightBrick().getRobotPositionY()));
					
					animateAfterTakePhoto();
				}
				else
					mInstance.popup("Camera non initialisée", MessageType.INFO);
			break;
			
			case R.id.resetDetection:
				resetPicturePhoto();
			break;
			
			case R.id.confirmHoughDetection:
				if(hv.getDepartPiece().size() != hv.getArriveePiece().size())
					Toast.makeText(mInstance, "La reconnaissance ne peux aboutir, merci de réessayer en changeant l'angle, la luminosité, etc ... ("+hv.getDepartPiece().size()+" VS "+hv.getArriveePiece().size()+")",  Toast.LENGTH_LONG).show();
				else {
					new Solver(mInstance).execute(hv.getDepartPiece(), hv.getArriveePiece());
					
					mInstance.setPiecePosition(hv.getDepartPiece(), hv.getArriveePiece());
					mInstance.changeFragment(FragmentWindow.PLAY);
				}
			break;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) { }

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			camera.setPreviewDisplay(holder);
			camera.startPreview();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if(camera != null) {
			camera.stopPreview();
			camera.release();
		}
	}
	
	@SuppressLint("HandlerLeak")
	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case DRAW_LINE:
					
					Serializable data = msg.getData().getSerializable("lines");
					
					if(data instanceof HashMap<?, ?>) {
						HashMap<Integer, ArrayList<Point>> lines = (HashMap<Integer, ArrayList<Point>>) data;
						hv.drawLineOnImage(lines);
					}
				break;
			}
		}
	};
}
