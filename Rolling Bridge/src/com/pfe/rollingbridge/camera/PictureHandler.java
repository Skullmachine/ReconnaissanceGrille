package com.pfe.rollingbridge.camera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.pfe.rollingbridge.R;
import com.pfe.rollingbridge.fragments.PhotoFragment;
import com.pfe.rollingbridge.views.HoughView;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PictureHandler implements PictureCallback {

	private PhotoFragment mPhotoParent;
	private HoughView mHoughView;
	private LinearLayout yesOrNo;
	private LinearLayout bePatient;
	private TextView textInfo;
	private int mCallerId;
	
	public PictureHandler(PhotoFragment cxt, int callerId) {
		mPhotoParent = cxt;
		mCallerId = callerId;
		mHoughView = (HoughView) mPhotoParent.getView().findViewById(R.id.houghBitmap);
		bePatient = (LinearLayout) mPhotoParent.getView().findViewById(R.id.LinearWaitTransforme);
		yesOrNo = (LinearLayout) mPhotoParent.getView().findViewById(R.id.LinearValidTransforme);
		textInfo = (TextView) mPhotoParent.getView().findViewById(R.id.textInfoPhotoFragment);
		
		yesOrNo.setVisibility(View.GONE);
		bePatient.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		// On decode l'image
		new decodeBitmapArray().execute(data);
	}
	
	private class decodeBitmapArray extends AsyncTask<byte[], Void, Bitmap> {
		
		@Override
		protected void onPreExecute() {
			textInfo.setText("D�codage de l'Image");
		}
		
		@Override
		protected Bitmap doInBackground(byte[]... data) {
			
			//On cr�e une image BMP
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 2;
			
			switch(mCallerId) {
				case R.id.FakeButton1:
					return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mPhotoParent.getResources(), R.drawable.grille1), mHoughView.getWidth(), mHoughView.getHeight(), false);
				
				case R.id.FakeButton2:
					return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mPhotoParent.getResources(), R.drawable.grille2), mHoughView.getWidth(), mHoughView.getHeight(), false);
				
				case R.id.FakeButton3:
					return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mPhotoParent.getResources(), R.drawable.grille3), mHoughView.getWidth(), mHoughView.getHeight(), false);
				
				case R.id.FakeRealButton1:
					return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mPhotoParent.getResources(), R.drawable.real_photo1), mHoughView.getWidth(), mHoughView.getHeight(), false);
					
				case R.id.FakeRealButton2:
					return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mPhotoParent.getResources(), R.drawable.real_photo2), mHoughView.getWidth(), mHoughView.getHeight(), false);
					
				case R.id.FakeRealButton3:
					return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mPhotoParent.getResources(), R.drawable.real_photo3), mHoughView.getWidth(), mHoughView.getHeight(), false);
					
				default:
					return  Bitmap.createScaledBitmap(BitmapFactory.decodeByteArray(data[0], 0, data[0].length, options), mHoughView.getWidth(), mHoughView.getHeight(), false);
			}
		}
		
		@Override
		protected void onPostExecute(Bitmap result) {
			textInfo.setText("Conversion en Niveau de Gris");
			
			mHoughView.drawBitmap(result);
			
			//On lance la transform�e de hough
			new HoughTransform().execute(result);
		}
	}

	
	private class HoughTransform extends AsyncTask<Bitmap, Void, Void> {
		
		private Bitmap image = null;
		private Hough hough = null;
		private HashMap<Integer, ArrayList<Point>> mLines;
		private int threshold = 50, radius = 4;
		
		@SuppressLint("UseSparseArrays")
		@Override
		protected void onPreExecute() {
			textInfo.setText("Application de la Transform�e de Hough");
			mLines = new HashMap<Integer, ArrayList<Point>>();
		}
		
		@Override
		protected Void doInBackground(Bitmap... pictureFile) {
			System.out.println("Transform�e de Hough");
			
			//On load l'image enregistr�e
			image = pictureFile[0];
			
			doTH(image);
			doLinesExtraction(image);
			
			return null;
		} 
		
		@Override
		protected void onPostExecute(Void result) {
			
			sendLinesToDrawToUiThread(mLines);
			 
			bePatient.setVisibility(View.GONE);
			yesOrNo.setVisibility(View.VISIBLE);
		}
		
		// called by "Load Image"
		private void doTH(Bitmap img0) {

			// new Hough Transformer
			hough = new Hough(img0.getWidth(), img0.getHeight());

			// grayscale conversion
			
			int rgbVal;
			int r,g,b;	
			int conv;
			int grayVal;	
			int[][] gray = new int[img0.getWidth()][img0.getHeight()];
			
			for(int i=0;i<img0.getWidth();i++){
				for(int j=0;j<img0.getHeight();j++){
					
	                rgbVal = img0.getPixel(i,j);
					
					// RGB --> 32 Bit Integer
					r = (rgbVal>>16) & 0xff; //rouge
					g = (rgbVal>>8) & 0xff; //vert
					b = rgbVal & 0xff; //bleu
	 
					// Conversion
					conv = (int) Math.round(0.2126*r + 0.7152*g + 0.0722*b);			
					grayVal = (conv << 16) | (conv << 8) | conv;
	 
					gray[i][j]=grayVal;

				}
			}

			// compute gradient (Sobel) + vote in Hough Space (if gradient>64)
			for (int y = 1 ; y < img0.getHeight()-1 ; y++) {
				
				for (int x = 1 ; x < img0.getWidth()-1 ; x++) {
					
					int gv = (gray[x+1][y-1]-gray[x-1][y-1])+2*(gray[x+1][y]-gray[x-1][y])+(gray[x+1][y+1]-gray[x-1][y+1]);
					int gh = (gray[x-1][y+1]-gray[x-1][y-1])+2*(gray[x][y+1]-gray[x][y-1])+(gray[x+1][y+1]-gray[x+1][y-1]);
					int g2 = (gv*gv + gh*gh)/(16);
					
					if (g2>4096) 
						hough.vote(x,y); // ||gradient||^2 > 64^2
					
				}
			}
		}
		
		// called by "Perform Hough Transform"
		private void doLinesExtraction(Bitmap img0) {
		    System.out.println("\nResults:");

		    // search extrema in Hough Space
		    List<double[]> winners = hough.getWinners(threshold, radius);

		    // display each winner (lines + peak)
		    for (double[] winner:winners) {

		    	double rho   = winner[0];
		    	double theta = winner[1];
		    	
		    	// print theta/rho value
				System.out.println("winner: theta="+Math.toDegrees(theta)+"�, rho="+(int)rho);

		    	// convert (rho,theta) to equation Y=a.X+b
			    double[] c = hough.rhotheta_to_ab(rho, theta);

			    Point A = (Double.isNaN(c[0])) ? new Point((int)c[1],0) : new Point(0,(int)c[1]);
			    Point B = (Double.isNaN(c[0])) ? new Point((int)c[1], img0.getHeight()) : new Point(img0.getWidth(), (int)(c[0] * img0.getWidth() + c[1]));
			    
				ArrayList<Point> p = new ArrayList<Point>();
				p.add(A);
				p.add(B);
			    mLines.put(mLines.size(), p);
		    }
		}
		
		private void sendLinesToDrawToUiThread(HashMap<Integer, ArrayList<Point>> lines) {
			Message m = new Message();
			Bundle b = new Bundle();
			
			b.putSerializable("lines", mLines);
			
			m.what = PhotoFragment.DRAW_LINE;
			m.setData(b);
		
			mPhotoParent.mHandler.sendMessage(m);
		}
	}
}
