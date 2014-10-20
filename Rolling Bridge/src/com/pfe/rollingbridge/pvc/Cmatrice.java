package com.pfe.rollingbridge.pvc;

import android.util.SparseArray;
import android.util.SparseIntArray;

public class Cmatrice {
	private int matrice[][];
	private int taille;

	/**
	 * Création d'une Cmatrice à partir des coordonnées de départs et d'arrivées des villes
	 * @param depart hashmap contenant les coordonnées de départs des villes
	 * @param arrivee hashmap contenant les coordonnées d'arrivées des villes
	 */
	public Cmatrice(SparseArray<SparseIntArray> depart, SparseArray<SparseIntArray> arrivee){
		this.taille = depart.size();
		this.matrice = new int[taille][taille];
		
		for(int i = 0; i < taille; ++i){
			for(int j = 0; j < taille; ++j){
				// Si on est sur la diagonale alors chemin impossible
				if(i == j)
					this.matrice[i][j] = -1;
				// Sinon chemin possible avec un coût égal à la distance
				else
					this.matrice[i][j] = Math.abs(depart.get(j).get(0) - arrivee.get(i).get(0)) + Math.abs(depart.get(j).get(1) - arrivee.get(i).get(1));
			}
		}
	}

	/**
	 * Création d'une Cmatrice par recopie
	 * @param mat Cmatrice à copier
	 */
	public Cmatrice(Cmatrice mat){
		taille = mat.taille;
		matrice = mat.getMatrice();
	}

	/**
	 * Retourner la taille de la matrice
	 * @return Taille de la matrice
	 */
	public int getTaille(){
		return taille;
	}

	/**
	 * Retourner la valeur ij de la matrice
	 * @param i Numéro de la ligne
	 * @param j Numéro de la colonne
	 * @return Valeur ij de la matrice
	 */
	public int getValeur(int i, int j){
		return ((i < taille) && (j < taille))? matrice[i][j]:-1;
	}

	/** 
	 * Retourne une copie de la matrice 
	 */
	public int[][] getMatrice(){
		int tmp [][] = new int [taille][taille];

		for(int i = 0; i < taille; i++){
			for(int j = 0; j < taille; j++){
				tmp[i][j] = matrice[i][j];
			}
		}
		return tmp;
	}

	/**
	 * Affichage de la matrice
	 */
	public void affichage(){
		System.out.println("-- MATRICE --");
		for (int i = 0; i < taille; i++) {
			for (int j = 0; j < taille; j++) {
				if((matrice[i][j] == -1) || (matrice[i][j] >=10))
					System.out.print(matrice[i][j] + " ");
				else
					System.out.print(" " + matrice[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println();
	}

	/**
	 * Calcul de l'arc de plus grand regret
	 * @return [0] depart [1] arrivée [2] regret
	 */
	public int[] arc_plus_grand_regret(){
		int coord[] = new int[3];
		coord[2] = -1;
		int minij;

		for (int i = 0; i < taille; i++){
			for (int j = 0; j < taille; j++){
				if (matrice[i][j] == 0){
					minij = minimum_ligne(i, j) + minimum_colonne(i, j);
					if (minij > coord[2]){
						coord[2] = minij;
						coord[0] = i;
						coord[1] = j;
					}
				}
			}
		}
		return coord;
	}

	/**
	 * Retourne le minimum de la ligne
	 * Si la colonne est précisée, le minimum ne prend pas en compte l'élément [ligne][colonne]
	 */
	public int minimum_ligne(int ligne, int colonne) {
		int min = -1;

		for (int j = 0; j < taille; j++) {
			if ((min == -1 || ((matrice[ligne][j] != -1) && (min > matrice[ligne][j]))) && (j != colonne)) {
				min = matrice[ligne][j];
			}
		}
		return min;
	}

	/**
	 * Retourne le minimum de la colonne
	 * Si la ligne est précisée, le minimum ne prend pas en compte l'élément [ligne][colonne]
	 */
	public int minimum_colonne(int ligne, int colonne) {
		int min = -1;

		for (int i = 0; i < taille; i++) {
			if ((min == -1 || ((matrice[i][colonne] != -1) && (min > matrice[i][colonne]))) && (i != ligne)) {
				min = matrice[i][colonne];
			}
		}
		return min;
	}

	/**
	 * Réduction de la matrice
	 * @return le coût minimum de la matrice
	 */
	public int reduction() {
		int D = 0;
		int imin_ligne = 0;
		int imin_colonne = 0;
		int min;

		// ******** Reduction des lignes ******//
		for (int i = 0; i < taille; i++) {
			min = minimum_ligne(i, -1);
			if (min != -1) {
				imin_ligne += min;

				for (int j = 0; j < taille; j++) {
					if (matrice[i][j] != -1)
						matrice[i][j] -= min;
				}
			}
		}

		// ******** Reduction des Colonnes ******//
		for (int j = 0; j < taille; j++) {
			min = minimum_colonne(-1, j);
			if (min != -1) {
				imin_colonne += min;

				for (int i = 0; i < taille; i++) {
					if (matrice[i][j] != -1)
						matrice[i][j] -= min;
				}
			}
		}

		D = imin_ligne + imin_colonne;
		return D;
	}

	/**
	 * Suppression du chemin pris
	 * @param ligne numéro de la ligne prise
	 * @param colonne numéro de la colonne prise
	 */
	public void suppression_chemin_pris(int ligne, int colonne) {
		for (int i = 0; i < taille; i++) {
			matrice[ligne][i] = -1;
			matrice[i][colonne] = -1;
		}
	}

	/**
	 * Suppression du chemin non pris
	 * @param ligne numero de la ligne
	 * @param colonne numero de la colonne
	 */
	public void suppression_chemin_non_pris(int ligne, int colonne) {
		matrice[ligne][colonne] = -1;
	}	
}
