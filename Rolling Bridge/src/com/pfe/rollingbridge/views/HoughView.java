package com.pfe.rollingbridge.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.pfe.rollingbridge.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.widget.ImageView;

public class HoughView extends ImageView {

	private HashMap<Integer, ArrayList<Point>> mLineToDraw;
	private TreeMap<Integer, ArrayList<Integer>> mIntersectionPoint;
	private Bitmap mBitmapToDraw = null;
	
	public static final int indiceRose = 1, indiceBleu = 2, indiceVert = 3, indiceRouge = 4, indiceJaune = 5;
	private SparseArray<SparseIntArray> departPiece;
	private SparseArray<SparseIntArray> arriveePiece;
	
	private Paint mRedPaint, mGreenPaint, mBluePaint;
	private Paint mLinePaint, mBlackPaint;
	private Paint mYellowPaint;
	
	public HoughView(Context context, AttributeSet attr, int defStyle) {
		super(context, attr, defStyle);
		
		mLineToDraw = new HashMap<Integer, ArrayList<Point>>();
		mIntersectionPoint = new TreeMap<Integer, ArrayList<Integer>>();
		
		departPiece = new SparseArray<SparseIntArray>();
		arriveePiece = new SparseArray<SparseIntArray>();
				
		initDrawing();
	}
	
	public HoughView(Context context, AttributeSet attr) {
		super(context, attr);
		
		mLineToDraw = new HashMap<Integer,ArrayList<Point>>();
		mIntersectionPoint = new TreeMap<Integer, ArrayList<Integer>>();
		
		departPiece = new SparseArray<SparseIntArray>();
		arriveePiece = new SparseArray<SparseIntArray>();
				
		initDrawing();
	}
	
	public HoughView(Context context) {
		super(context);
		
		mLineToDraw = new HashMap<Integer,ArrayList<Point>>();
		mIntersectionPoint = new TreeMap<Integer, ArrayList<Integer>>();
		
		departPiece = new SparseArray<SparseIntArray>();
		arriveePiece = new SparseArray<SparseIntArray>();
				
		initDrawing();
	}
	
	public void defineRobotPosition(Point position) {
		//Premier élement est égale à la position du robot 
		//0 = x   ---  1 = y
		SparseIntArray robotDepart = new SparseIntArray();
		robotDepart.append(0, position.x);
		robotDepart.append(1, position.y);
		
		SparseIntArray robotArrivee = new SparseIntArray();
		robotArrivee.append(0, position.x);
		robotArrivee.append(1, position.y);
		
		departPiece.append(0, robotDepart);
		arriveePiece.append(0, robotArrivee);
	}
	
	public void addPointDepart(int indice, Point depart) {
		//0 = x   --- 1 = y
		SparseIntArray departPosition = new SparseIntArray();
		departPosition.append(0, depart.x);
		departPosition.append(1, depart.y);

		departPiece.append(indice, departPosition);
	}
	
	public void addPointArrivee(int indice, Point arrivee) {
		//0 = x   --- 1 = y
		SparseIntArray arriveePosition = new SparseIntArray();
		arriveePosition.append(0, arrivee.x);
		arriveePosition.append(1, arrivee.y);

		arriveePiece.append(indice, arriveePosition);
	}
	
	public SparseArray<SparseIntArray> getDepartPiece() {
		return departPiece;
	}
	
	public SparseArray<SparseIntArray> getArriveePiece() {
		return arriveePiece;
	}
	
	private void initDrawing() {
		mRedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mRedPaint.setColor(getResources().getColor(android.R.color.holo_red_dark));
	
		mBlackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBlackPaint.setColor(getResources().getColor(android.R.color.black));
		mBlackPaint.setTextSize(16);
		
		mGreenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mGreenPaint.setColor(getResources().getColor(android.R.color.holo_green_dark));
		
		mBluePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBluePaint.setColor(getResources().getColor(android.R.color.holo_blue_dark));
		
		mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mLinePaint.setStrokeWidth(2);
		mLinePaint.setColor(getResources().getColor(R.color.GameBoard_line));
		
		mYellowPaint = new Paint();
		mYellowPaint.setColor(getResources().getColor(R.color.GameBoard_robot));
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if(mBitmapToDraw != null)
			canvas.drawBitmap(mBitmapToDraw, 0,0, null);
			
		//On dessine les lignes de la transformée de Hough
		for(int i = 0 ; i < mLineToDraw.size() ; i++) {
			canvas.drawLine(mLineToDraw.get(i).get(0).x, mLineToDraw.get(i).get(0).y, 
							mLineToDraw.get(i).get(1).x, mLineToDraw.get(i).get(1).y, mGreenPaint);
		}
		
		//on calcul les points d'intersection
		getIntersectionPoint();
		
		//On dessine les intersections
		for(Entry<Integer, ArrayList<Integer>> entry : mIntersectionPoint.entrySet()) 
			for(Integer x : entry.getValue()) 
				canvas.drawCircle(x, entry.getKey(), 2f, mRedPaint);
		
		//On dessine les formes
		calculatePosition(canvas);
	}
	
	public void cleanCanvas() {
		mBitmapToDraw = null;
		mLineToDraw.clear();
		mIntersectionPoint.clear();
		invalidate();
	}
	
	
	public void drawBitmap(Bitmap val) {
		mBitmapToDraw = val;
		invalidate();
	}
	
	public void drawLineOnImage(HashMap<Integer, ArrayList<Point>> lines) {
		mLineToDraw = lines;			
		invalidate();
	}
	
	public void calculatePosition(Canvas canvas) {
		int indiceI = 0, indiceJ = 0;
		
		//on vide les matrices pour éviter tout probleme de cumul de point
		departPiece.clear();
		arriveePiece.clear();
		defineRobotPosition(new Point(0,0));
		
		//dans le cas où il est possible qu'il y ai le quadrillage
		if(mIntersectionPoint.size() > 4) {
			
			Integer[] keys = new Integer[mIntersectionPoint.size()];
			keys = mIntersectionPoint.keySet().toArray(keys);
			
			int i1 = 0,i2 = 1;
			int j1 = 0,j2 = 1; 
			
			//Tant qu'on a pas parcouru toutes les lignes possibles
			while(i2 < mIntersectionPoint.size()) {
				
				//Tant qu'on a pas parcouru toutes les colonnes possibles
				while(j2 < mIntersectionPoint.get(keys[i2]).size()) {
					
					float centreX = (mIntersectionPoint.get(keys[i1]).get(j1) + mIntersectionPoint.get(keys[i1]).get(j2)) / 2f;
					float centreY = (keys[i1] + keys[i2]) / 2f;
										
					int rgb = mBitmapToDraw.getPixel((int)centreX, (int)centreY);
					int rgbCroix = mBitmapToDraw.getPixel(((int)centreX-35), (int)centreY);
					int r = (rgb >>16 ) & 0xFF, rCroix = (rgbCroix >> 16 ) & 0xFF;
					int g = (rgb >> 8 ) & 0xFF, gCroix = (rgbCroix >> 8) & 0xFF;
					int b = rgb & 0xFF, bCroix = rgbCroix & 0xFF;
					
					
					//On affiche les cases dans le cas où c'est une couleur
					if(Color.rgb(r, g, b) != Color.WHITE && Color.rgb(r,g, b) != Color.BLACK) {
						
						String text = "null";

						//On regarde la couleur de la case, puis la position
						
						//Vert :   G > 115   B < 68   R  < 90
						if(r < 90 && b < 68 && g > 70) {
							//Si c'est la croix
							if(Color.rgb(rCroix, gCroix, bCroix) == Color.WHITE) {
								text = "Vert (A)";
								addPointArrivee(indiceVert, new Point(indiceI, indiceJ));
							}
							else {
								text = "Vert (D)";
								addPointDepart(indiceVert, new Point(indiceI, indiceJ));
							}
						}
						//Bleu :   G < 68    B > 115  R  < 90
						else if(r < 90 && b > 70 && g < 68) {
							//si c'est la croix
							if(Color.rgb(rCroix, gCroix, bCroix) == Color.WHITE) {
								text = "Bleu (A)";
								addPointArrivee(indiceBleu, new Point(indiceI, indiceJ));
							}
							else {
								text = "Bleu (D)";
								addPointDepart(indiceBleu, new Point(indiceI, indiceJ));
							}
						}
						//Rouge:   G < 49    B < 49   R > 115
						else if(r > 70 && b < 49 && g < 49) {
							//Si c'est la croix
							if(Color.rgb(rCroix, gCroix, bCroix) == Color.WHITE) {
								text = "Rouge (A)";
								addPointArrivee(indiceRouge, new Point(indiceI, indiceJ));
							}
							else {
								text = "Rouge (D)";
								addPointDepart(indiceRouge, new Point(indiceI, indiceJ));
							}
						}
						//Rose :   G < 81    145 < B < 230  R > 170
						else if(r > 185 && b <= 255 && b > 110 && g < 90) {
							//Si c'est la croix
							if(Color.rgb(rCroix, gCroix, bCroix) == Color.WHITE) {
								text = "Rose (A)";
								addPointArrivee(indiceRose, new Point(indiceI, indiceJ));
							}
							else {
								text = "Rose (D)";
								addPointDepart(indiceRose, new Point(indiceI, indiceJ));
							}
						}
						//Jaune:   200 < G < 240				B < 70		R > 200
						else if(r > 200 && b < 70 && g > 200 && g < 240) {
							//Si c'est la croix
							if(Color.rgb(rCroix, gCroix, bCroix) == Color.WHITE) {
								text = "Jaune (A)";
								addPointArrivee(indiceJaune, new Point(indiceI, indiceJ));
							}
							else {
								text = "Jaune (D)";
								addPointDepart(indiceJaune, new Point(indiceI, indiceJ));
							}
						}
						
						canvas.drawText(text, centreX - 35, centreY + 3, mBlackPaint);
					}
					
					indiceJ++;
					j1 = j2;
					j2 += 1;
				}
				
				j1 = 0; j2 = 1;
				i1 = i2;
				i2 += 1;
				indiceI++;
				indiceJ = 0;
			}
		}
	}
	
    public void getIntersectionPoint() {

		Point[] lineA = new Point[2];
		Point[] lineB = new Point[2];
		
		for(int i = 0 ; i < mLineToDraw.size() ; i++) {
			//On prend la première ligne
			lineA[0] = mLineToDraw.get(i).get(0);
			lineA[1] = mLineToDraw.get(i).get(1);
			
			//calcul de l'équation de la droite
			float mA = (float)(lineA[1].y - lineA[0].y) / (float)(lineA[1].x - lineA[0].x);
			float pA = lineA[0].y - (mA * lineA[0].x);
			
			for(int j = 0 ; j < mLineToDraw.size() ; j++) {
				//On compare à la seconde ligne
				lineB[0] = mLineToDraw.get(j).get(0);
				lineB[1] = mLineToDraw.get(j).get(1);
				 
				//calcul de l'equation de la droite	
				float mB = (float)(lineB[1].y - lineB[0].y) / (float)(lineB[1].x - lineB[0].x);
				float pB = lineB[0].y - (mB * lineB[0].x);
				
				Point intersect = new Point();
				
				//Dans le càs où c'est deux droites perpendiculaires
				if( (lineA[0].x == lineA[1].x) && (lineB[0].y == lineB[1].y) ||
					(lineB[0].x == lineB[1].x) && (lineA[0].y == lineA[1].y)) {
					
					intersect.x = (lineB[0].y == 0) ? lineB[0].x : lineA[0].x;
					intersect.y = (lineB[0].x == 0) ? lineB[0].y : lineA[0].y;
				}
				//on continue si ce n'est pas la même droite ou des droites parallèles
				else if((mA != mB)) {
					//On cherche le point d'intersection 
					float x = (float)(pB - pA) / (float)(mA - mB);
					float y = (float)((mA * pB) - (pA * mB)) / (float)(mA - mB);
					
					intersect.x = (int)x;
					intersect.y = (int)y;
				}
				
				//On ajoute le points d'intersections s'il est valide dans les points à dessiner
				if( (intersect.x > 0) && (intersect.x < getWidth()) ) {
					if( (intersect.y > 0) && (intersect.y < getHeight()) ) {
						if(mIntersectionPoint.containsKey(intersect.y)) {
							ArrayList<Integer> xVal = mIntersectionPoint.get(intersect.y);
							if(!xVal.contains(intersect.x))
								xVal.add(intersect.x);
						}
						else {
							ArrayList<Integer> xVal = new ArrayList<Integer>() {
								private static final long serialVersionUID = 1L;

								@Override
								public boolean add(Integer val) {
							        int index = Collections.binarySearch(this, val);
							        if (index < 0) index = ~index;
							        super.add(index, val);
							        return true;
								}
							};
							
							xVal.add(intersect.x);
							mIntersectionPoint.put(intersect.y, xVal);		
						}
					}
				}
			}
		}	
    }
}
