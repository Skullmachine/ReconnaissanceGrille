package com.pfe.rollingbridge.camera;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Hough Transform
 * 
 * @author Xavier Philippeau
 *
 */
public class Hough {

	// image size
	private int Width,Height;
 
	// max Rho walue (= length of the diagonal)
	private double maxRho;

	// max Theta walue (= 180 degrees = PI)
	private double maxTheta = Math.PI;

	// size of the accumulators array
	private int maxIndexTheta,maxIndexRho;
	
	// accumulators array
	private int[][] acc;
	
	// 8 neighboors offsets
	private int[] dx8 = new int[] {-1, 0, 1, 1, 1, 0, -1, -1};
	private int[] dy8 = new int[] {-1,-1,-1, 0, 1, 1,  1,  0};

	// Constructor
	public Hough(int width,int height) {
		this.Width=width;
		this.Height=height;

		// diagonal
		this.maxRho = Math.sqrt( width*width + height*height );
		
		// size of the accumulators array
		this.maxIndexTheta= 360; // precision : 180/360 = 0.5 degree
		this.maxIndexRho=(int)(1+this.maxRho); // precision : 1 pixel
		this.acc = new int[maxIndexRho][maxIndexTheta];
	}
	
	// ---------------------------------------------------------------------------------
	//                            HOUGH TRANSFORM ALGORITHM 
	// ---------------------------------------------------------------------------------
	
	// votes for pixel (x,y)
	public void vote(int x,int y) {
		// centered 
		x-=Width/2; y-=Height/2;
		
		for(int indexTheta=0; indexTheta<maxIndexTheta; indexTheta+=1) {
			double theta = IndexToTheta(indexTheta);
			double rho = x*Math.cos(theta) + y*Math.sin(theta);
			
			// (rho,theta) -> index
			int indexRho   = RhoToIndex(rho);
	
			if (indexTheta<maxIndexTheta && indexRho<maxIndexRho) 
				acc[indexRho][indexTheta]++;
		}
	}
	
	// compute list of local extrema in Hough Space
	public List<double[]> getWinners(int threshold, int radius) {
		
		// maximum in the array
		int highestvote=0;
		for(int r=0;r<maxIndexRho;r++)
			for(int t=0;t<maxIndexTheta;t++)
				if (acc[r][t]>highestvote) highestvote=acc[r][t];
				
		// minimum vote needed to be a local extrema 
		int minvote=(highestvote*threshold)/100;

		// parsing the accumulators
		List<int[]> coords = new ArrayList<int[]>();
		for(int r=0;r<maxIndexRho;r++) {
			for(int t=0;t<maxIndexTheta;t++) {
				
				// value of current accumulator
				if (acc[r][t]<minvote) continue;
				
				// maxima in the neighborhood of this accumulator
				int nmax=0;
				for(int k=0;k<dx8.length;k++) {
					int rk=r+dx8[k];
					int tk=t+dy8[k];
					
					if (rk<0) continue;
					if (rk>=maxIndexRho) continue;
					if (tk<0) tk+=maxIndexTheta;
					if (tk>=maxIndexTheta) tk-=maxIndexTheta;

					if (acc[rk][tk]>nmax) nmax=acc[rk][tk];
				}
				
				// the current accumulator is not the highest value -> ignore
				if (nmax>acc[r][t]) continue;

				// prevent extrema to be too close to each others
				// => compare this coord to the others 
				boolean ignore=false;
				Iterator<int[]> iter = coords.iterator();
				while(iter.hasNext()) {
					int[] coord = iter.next();
					int dist=distance(coord[0],coord[1],r,t);
					if (dist>(2*radius)) continue;

					// this extrema is too close from the current one.
					// We keep the extrema with the highest vote.
					
					if (acc[r][t]>=acc[coord[0]][coord[1]]) {
						iter.remove(); // remove the other
					} else {
						ignore=true; break; // remove me
					} 
				}

				// store extrema in the array
				if (!ignore) coords.add( new int[] {r,t} );
			}
		}
		
		// convert array index to real (rho,theta) values
		List<double[]> winners = new ArrayList<double[]>();
		for(int[] coord:coords) {
			int r=coord[0];
			int t=coord[1];
			
			// convert to real (rho,theta) value
			double rho   = IndexToRho(r);
			double theta = IndexToTheta(t);
			
			// store in the list
			winners.add( new double[] {rho,theta} );
		}
		
		return winners;
	}

	// minimum distance between 2 coords in the array (mobius) 
	private int distance(int r0,int t0, int r1, int t1) {
		/* distance between (r0,t0) and (r1,t1) */
		int dist = Math.max(Math.abs(r0-r1), Math.abs(t0-t1));

		if (t0<t1) {
			/* distance between (-r0,t0+PI) and (r1,t1) */
			t0 = t0+maxIndexTheta; // theta=theta+PI => tindex=tindex+maxindex
			r0 = maxIndexRho-r0-1; // rho=-rho => rindex=max-rindex
		} else {
			/* distance between (r0,t0) and (-r1,t1+PI) */
			t1 = t1+maxIndexTheta; // theta=theta+PI => tindex=tindex+maxindex
			r1 = maxIndexRho-r1-1; // rho=-rho => rindex=max-rindex
		}
		dist = Math.min (dist, Math.max(Math.abs(r0-r1), Math.abs(t0-t1)) );
		
		return dist;
	}

	// ---------------------------------------------------------------------------------
	//                                   CONVERTERS 
	// ---------------------------------------------------------------------------------

	// convert rho[-maxRho,maxRho] and theta[0,maxTheta] from/to array index [0,maxIndex]
	public int RhoToIndex(double rho) { 
		return (int) (0.5 + (rho/this.maxRho + 0.5) * this.maxIndexRho );
	}
	public double IndexToRho(int index) {
		return ((double)index/this.maxIndexRho - 0.5)*this.maxRho;
	}
	public int ThetaToIndex(double theta) {
		return (int) (0.5 + (theta/this.maxTheta) * maxIndexTheta );
	}
	public double IndexToTheta(int index) {
		return ((double)index/this.maxIndexTheta)*this.maxTheta;
	}

	// convert (rho,theta) to (a,b) such that Y=a.X+b
	public double[] rhotheta_to_ab(double rho, double theta) {
		// vertical case
		if(Math.abs(Math.sin(theta))<0.01) {
			double a = Double.NaN;
			double b = Width/2+((theta<1.57)?rho:-rho);
			return new double[] {a,b};
		}
		
		double a = -Math.cos(theta)/Math.sin(theta);
		double b = rho/Math.sin(theta)+Height/2-a*Width/2;
		return new double[] {a,b};
	}

	// ---------------------------------------------------------------------------------
	//                                   GETTER/SETTER 
	// ---------------------------------------------------------------------------------

	public int getMaxIndexTheta() {
		return maxIndexTheta;
	}

	public int getMaxIndexRho() {
		return maxIndexRho;
	}

	public int[][] getAccumulator() {
		return acc;
	}	
}
