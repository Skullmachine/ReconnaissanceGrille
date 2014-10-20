package com.pfe.rollingbridge.pvc;

import android.os.AsyncTask;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.pfe.rollingbridge.MainActivity;
import com.pfe.rollingbridge.views.HoughView;

public class Solver extends AsyncTask<SparseArray<SparseIntArray>, Void, Integer> {
	
	private MainActivity mActivity;
	private SparseArray<SparseIntArray> depart;
	private SparseArray<SparseIntArray> arrivee;
		
	public Solver(MainActivity activity) {
		mActivity = activity;
	}
	
	@Override
	protected Integer doInBackground(SparseArray<SparseIntArray>... params) {
		
		if(params.length != 2)
			return null;
		
		depart = params[0];
		arrivee = params[1];
		
		long start = System.currentTimeMillis();

		Carbre arbre = new Carbre(depart, arrivee);
		SparseIntArray solution = arbre.resolution();

		long stop = System.currentTimeMillis();
		
		if(solution != null) {
			
			mActivity.setSolutionPVC(solution);
			
			for(int i = 0 ; i < solution.size() ; i++) {
				int key = solution.keyAt(i);
				
				switch(key) {
					case HoughView.indiceRose: System.out.println((i != solution.size()-1) ? "Rose -> " : "Rose"); break;
					case HoughView.indiceBleu: System.out.println((i != solution.size()-1) ? "Bleu -> " : "Bleu"); break;
					case HoughView.indiceJaune: System.out.println((i != solution.size()-1) ?"Jaune -> ": "Jaune"); break;
					case HoughView.indiceRouge: System.out.println((i != solution.size()-1) ?"Rouge -> ":"Rouge"); break;
					case HoughView.indiceVert: System.out.println((i != solution.size()-1) ?"Vert -> ":"Vert"); break;
				}
			}
			System.out.println("");
			System.out.println("Cožt de la solution : "+ arbre.getCoutSolution());
			System.out.println("Temps de RŽsolution du StackerCraneProblem : "+ (stop-start)+" ms");
		}
		else
			System.out.println("Solution Infaisable");
		
		return arbre.getCoutSolution();
	}
	
	@Override
	protected void onPostExecute(Integer nbCoup) {
		mActivity.setScoreABattre(nbCoup);
	}
}
