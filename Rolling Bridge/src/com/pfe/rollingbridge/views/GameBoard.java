package com.pfe.rollingbridge.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;

import com.pfe.rollingbridge.R;

public class GameBoard extends View {

	private int mLineNumber, mRowNumber;
	private boolean isTrainingMode;
	
	private Paint mRedPaint, mGreenPaint, mBluePaint;
	private Paint mLinePaint, mArrowPaint, mBackgroundPaint;
	private Paint mCaseDepartPaint, mCaseArriveePaint, mRobotPaint;
	private float mBoxWidth, mBoxHeight;
	private SparseArray<SparseIntArray> departPiece, arriveePiece;
	
	private boolean drawArrow;
	private float startArrowX, startArrowY, stopArrowX, stopArrowY;
	private RectF caseDepart, caseArrivee;
	private Point mRobotPosition;
	
	public GameBoard(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GameBoard, 0, 0);
		
		try {
			mLineNumber = a.getInteger(R.styleable.GameBoard_linesNumber, 8);
			mRowNumber = a.getInteger(R.styleable.GameBoard_colsNumber, 5);
			isTrainingMode = a.getBoolean(R.styleable.GameBoard_isTraining, true);
		} finally {
			a.recycle();
		}
		
		startArrowX = 0; startArrowY = 0;
		stopArrowX = 0; stopArrowY = 0;
		mRobotPosition = new Point(0,0);
		
		initDrawing();
	}
	
	private void initDrawing() {
		mRedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mRedPaint.setColor(getResources().getColor(android.R.color.holo_red_dark));
	
		mGreenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mGreenPaint.setColor(getResources().getColor(android.R.color.holo_green_dark));
		
		mBluePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBluePaint.setColor(getResources().getColor(android.R.color.holo_blue_dark));
		
		mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mLinePaint.setStrokeWidth(2);
		mLinePaint.setColor(getResources().getColor(R.color.GameBoard_line));
		
		mArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mArrowPaint.setStrokeWidth(3);
		mArrowPaint.setStyle(Paint.Style.FILL);
		mArrowPaint.setColor(getResources().getColor(R.color.GameBoard_arrow));
		
		mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBackgroundPaint.setColor(getResources().getColor(R.color.GameBoard_Background));
		
		mCaseArriveePaint = new Paint();
		mCaseArriveePaint.setColor(getResources().getColor(R.color.GameBoard_case));		
		
		mCaseDepartPaint = new Paint();
		mCaseDepartPaint.setColor(getResources().getColor(R.color.GameBoard_case));
		
		mRobotPaint = new Paint();
		mRobotPaint.setColor(getResources().getColor(R.color.GameBoard_robot));
		
		drawArrow = false;
		caseDepart = new RectF();
		caseArrivee = new RectF();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		Log.d("GameBoard", "OnSizeChanged");
		
		mBoxWidth = w / (float)mRowNumber;
		mBoxHeight = h / (float)mLineNumber;
		
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Log.d("GameBoard", "OnDraw");
		
		//On dessine le background
		canvas.drawRect(0, 0, getWidth(), getHeight(), mBackgroundPaint);
		
		//On dessine les lignes horizontales
		for(int i = 0 ; i < (mLineNumber+2) ; i++)
				canvas.drawLine(0, i*mBoxHeight, getWidth(), i*mBoxHeight, mLinePaint);
		
		//on dessine les lignes verticales
		for(int i = 0 ; i < (mRowNumber+2) ; i++)
			canvas.drawLine(i*mBoxWidth, 0, i*mBoxWidth, getHeight(), mLinePaint);
		
		//si nous devons dessiner une flêche et les cases de depart d'arrivé
		if(drawArrow) {
			//Case Depart
			caseDepart.left = ((int)( mBoxWidth * ((int) startArrowX / (int) mBoxWidth) ))+2;
			caseDepart.bottom = ((int)(mBoxHeight * ((int) startArrowY / (int) mBoxHeight) ))+2; 
			caseDepart.right = ((int)(caseDepart.left + mBoxWidth))-3; 
			caseDepart.top = ((int)(caseDepart.bottom + mBoxHeight))-3;
				
			canvas.drawRect(caseDepart, mCaseDepartPaint);
		
			//Case Arrivée
			caseArrivee.left = ((int)( mBoxWidth * ((int) stopArrowX / (int) mBoxWidth) ))+2;
			caseArrivee.bottom = ((int)(mBoxHeight * ((int) stopArrowY / (int) mBoxHeight) ))+2; 
			caseArrivee.right = ((int)(caseArrivee.left + mBoxWidth))-3; 
			caseArrivee.top = ((int)(caseArrivee.bottom + mBoxHeight))-3;
			
			canvas.drawRect(caseArrivee, mCaseArriveePaint);
				
			//Fleche
			canvas.drawLine(startArrowX, startArrowY, stopArrowX, stopArrowY, mArrowPaint);
		}
		
		//en mode play, on dessine les pieces
		if(!isTrainingMode) {
			if(departPiece != null && arriveePiece != null) {
				
				//for(int i = 0 ; i < departPiece.size() ; i++) 
					//canvas.drawCircle(cx, cy, 20f, );
				
			}
		}
		
		//draw position robot
		canvas.drawCircle((mBoxWidth * mRobotPosition.x + mBoxWidth/2), 
						  (mBoxHeight * mRobotPosition.y + mBoxHeight/2), 
						  10.0f, mRobotPaint);
		
		super.onDraw(canvas);
	}
		
	public void drawPiecePosition(SparseArray<SparseIntArray> depart, SparseArray<SparseIntArray> arrivee) {
		departPiece = depart;
		arriveePiece = arrivee;
		
		invalidate();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				startArrowX = event.getX();
				startArrowY = event.getY();
				stopArrowX = startArrowX;
				stopArrowY = startArrowY;
			return true;
			
			case MotionEvent.ACTION_MOVE:
				
				stopArrowX = event.getX();
				stopArrowY = event.getY();
				
				//Si c'est la case du robot on dessine
				if( (((int)startArrowX / (int)mBoxWidth) == mRobotPosition.x) && (((int)startArrowY / (int)mBoxHeight) == mRobotPosition.y) ) {
					
					//le startArrow prend les coordonnées du robot
					startArrowX = (mRobotPosition.x * mBoxWidth) + (mBoxWidth/2);
					startArrowY = (mRobotPosition.y * mBoxHeight) + (mBoxHeight/2);
					
					//on controle la fleche pour qu'elle ne depasse pas les bornes du canvas
					//1 valeur sans incidence sur le rendu mais permettant de garder la case sélectionnée
					stopArrowX = (stopArrowX > getWidth()) ? getWidth()-1 : stopArrowX;
					stopArrowX = (stopArrowX < 0) ? 1 : stopArrowX;
					stopArrowY = (stopArrowY > getHeight()) ? getHeight()-1 : stopArrowY;
					stopArrowY = (stopArrowY < 0) ? 1 : stopArrowY;
					
					drawArrow = true;
				}
					
				
				//on demande à la vue d'être redessiné
				invalidate();
			return true;
			
			case MotionEvent.ACTION_UP:
				drawArrow = false;
				
				//on demande à la vue d'être redessiné
				invalidate();
			return false;
		}
		
		return false;
	}
	
	public Point[] getLastArrowCoordinate() {
		
		Point[] result = new Point[2];
		
		result[0] = new Point(((int) startArrowX / (int) mBoxWidth), ((int) startArrowY / (int) mBoxHeight));
		result[1] = new Point(((int) stopArrowX / (int) mBoxWidth), ((int) stopArrowY / (int) mBoxHeight));
		
		return result;
	}
	
	public void changeRobotPosition(int x, int y) {
		mRobotPosition.x = (x >= 0 && x < getNumberOfRows()) ? x : 0 ;
		mRobotPosition.y = (y >= 0 && y < getNumberOfLine()) ? y : 0 ;
		
		invalidate();
	}
	
	public Point getRobotPosition() {
		return mRobotPosition;
	}
	
	public int getNumberOfLine() {
		return mLineNumber;
	}
	public int getNumberOfRows() {
		return mRowNumber;
	}
}
