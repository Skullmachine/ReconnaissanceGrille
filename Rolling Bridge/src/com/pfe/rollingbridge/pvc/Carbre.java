package com.pfe.rollingbridge.pvc;

import android.util.SparseArray;
import android.util.SparseIntArray;

public class Carbre {
	
	private SparseArray<SparseIntArray> mDepart, mArrivee;
	private Cmatrice matrice;
	private Cnoeud noeudRacine;
	private Cnoeud noeudSolution;
	private int coutSolution;

	/**
	 * Création/Initialisation d'un Carbre
	 * 
	 * @param depart
	 *            hashmap des coordonnées de départs des villes
	 * @param arrivee
	 *            hashmap des coordonnées d'arrivées des villes
	 */
	public Carbre(SparseArray<SparseIntArray> depart, SparseArray<SparseIntArray> arrivee) {
		mDepart = depart;
		mArrivee = arrivee;
		
		matrice = new Cmatrice(mDepart, mArrivee);
		noeudRacine = new Cnoeud(null, null, null, matrice, 0, 0, -1, -1, true);
		noeudSolution = null;
		coutSolution = -1;
	}

	/**
	 * Méthode de séparation évaluation
	 * 
	 * @param noeud
	 *            Noeud sur lequel s'applique la séparation et l'évaluation
	 */
	public void separation_evaluation(Cnoeud noeud) {
		// SI aucune solution trouvée
		// OU un coût actuel inférieur à la meilleure solution trouvée
		if (((coutSolution > noeud.getCout()) || (coutSolution == -1)) && !noeud.EstCoupe()) {
			// Réduction & séparation du noeud courant
			noeud.Separation();

			// SI le noeud est un noeud feuille
			// ALORS une solution a été trouvée
			if (noeud.EstFeuille()) {
				// System.out.println("Noeud feuille \n");

				// SI aucune solution trouvée
				// OU la solution trouvée est meilleure que la précédente
				if (noeud.getCout() < coutSolution || coutSolution == -1) {
					coutSolution = noeud.getCout();
					noeudSolution = noeud;
				}
			} else if (!noeud.EstCoupe()) {
				// Parcours en profondeur
				// noeud.fgauche.Separation();
				// noeud.fdroit.Separation();

				// Parcours par evaluation
				if (noeud.getFgauche().getCout() < noeud.getFdroit().getCout()) {
					// System.out.println("Fils gauche \n");
					separation_evaluation(noeud.getFgauche());
					// System.out.println("Fils droit \n");
					separation_evaluation(noeud.getFdroit());
				} else {
					// System.out.println("Fils droit \n");
					separation_evaluation(noeud.getFdroit());
					// System.out.println("Fils gauche \n");
					separation_evaluation(noeud.getFgauche());
				}
			}
		} else {
			noeud.setEstCoupe(true);
			// System.out.println("Noeud coupé \n");
		}
	}

	/**
	 * Vérification de la solution
	 * 
	 * @param test
	 *            Tableau de la solution à tester
	 * @return si la solution est fonctionnelle vrai sinon faux
	 */
	public boolean solutionOk(int[] test) {
		boolean[] verification = new boolean[test.length];

		// Initialisation
		for (int i = 0; i < test.length; i++)
			verification[i] = false;

		// Parcours de la solution
		for (int i = 0, k = 0; i < test.length; i++) {
			verification[k] = true;
			k = test[k];
		}

		for (int i = 0; i < test.length; i++)
			// SI une ville n'a pas été visitée alors la solution n'est pas
			// fonctionnelle
			if (verification[i] == false)
				return false;
		return true;
	}

	/**
	 * Retourne la solution
	 * 
	 * @return hasmap<i,j> de la solution avec i le numéro de l'itération et j
	 *         la ville à visiter
	 */
	public SparseIntArray solution() {
		System.out.println("-- SOLUTION --");

		SparseIntArray solution = new SparseIntArray();

		// SI une solution existe
		if (noeudSolution != null) {
			int[][] tmp = new int[2][matrice.getTaille()]; // 0->depart
															// 1->arrivee
			int[] manquant = new int[4];

			// Initialisation de tmp
			for (int i = 0; i < matrice.getTaille(); i++) {
				tmp[0][i] = -1;
				tmp[1][i] = -1;
			}

			// Récupération des arcs pris
			for (Cnoeud cN = noeudSolution; cN != null; cN = cN.getPere()) {
				if (cN.getArc()[0] != -1) {
					tmp[0][cN.getArc()[0]] = cN.getArc()[1];
					tmp[1][cN.getArc()[1]] = cN.getArc()[0];
				}
			}

			// Recherche des chemins manquants
			for (int i = 0, k = 0, l = 2; i < matrice.getTaille(); i++) {
				if (tmp[0][i] == -1) {
					manquant[k] = i;
					k++;
				}
				if (tmp[1][i] == -1) {
					manquant[l] = i;
					l++;
				}
			}

			// Détermination des chemins manquants
			// Première solution
			tmp[0][manquant[0]] = manquant[2];
			tmp[0][manquant[1]] = manquant[3];

			// SI la solution n'est pas valide
			// ALORS essayer la deuxième solution
			if (!solutionOk(tmp[0])) {
				tmp[0][manquant[1]] = manquant[2];
				tmp[0][manquant[0]] = manquant[3];

				// Vérification de la deuxième solution
				if (!solutionOk(tmp[0])) {
					System.out.print("Solution invalide");
					return null;
				}
			}

			// Ecriture de la solution
			for (int i = 0, k = 0; i < matrice.getTaille(); i++) {
				solution.put(i, k);
				k = tmp[0][k];
				System.out.print(solution.get(i) + " -> ");
			}
			
			System.out.println("0");
			
			//calcul des co�t de d�placement quand la pince d�tient un objet
			for(int i = 1 ; i < solution.size() ; i++) {
				coutSolution += ( Math.abs(mDepart.get(i).get(0) - mArrivee.get(i).get(0)) + Math.abs(mDepart.get(i).get(1) - mArrivee.get(i).get(1)) );
			}
			

			return solution;
			
		} else {
			System.out.println("Aucune solution trouv�e");
			return null;
		}
	}

	public int getCoutSolution() {
		return coutSolution;
	}
	
	/**
	 * Résolution de la matrice
	 * 
	 * @return la solution du problème
	 */
	public SparseIntArray resolution() {
		//matrice.affichage();
		
		separation_evaluation(noeudRacine);
		
		return solution();
	}
}