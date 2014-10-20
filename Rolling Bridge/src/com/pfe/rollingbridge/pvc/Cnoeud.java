package com.pfe.rollingbridge.pvc;

public class Cnoeud {

	private Cnoeud pere;
	private Cnoeud fgauche;
	private Cnoeud fdroit;

	private Cmatrice matrice;
	private int cout;

	private int etage;
	private int arc[];

	private boolean EstCoupe;
	private boolean EstFilsGauche;

	/**
	 * Création/Initialisation d'un noeud
	 * @param gauche Fils gauche du noeud
	 * @param pere Père du noeud
	 * @param droit Fils droit du noeud
	 * @param matrice Matrice du noeud
	 * @param cout Cout du noeud
	 * @param unEtage Etage du noeud par rapport à la racine
	 * @param arcDepart Départ de l'arc pris
	 * @param arcArrive Arrivée de l'arc pris
	 * @param filsGauche si c'est un fils gauche vrai sinon faux
	 */
	public Cnoeud(Cnoeud gauche, Cnoeud pere, Cnoeud droit, Cmatrice matrice, int cout, int unEtage, int arcDepart, int arcArrive, boolean filsGauche) {
		this.pere = pere;
		this.fgauche = gauche;
		this.fdroit = droit;

		this.matrice = new Cmatrice(matrice);
		this.cout = cout;

		this.etage = unEtage;
		this.arc = new int[2];
		this.arc[0] = arcDepart;
		this.arc[1] = arcArrive;

		this.EstCoupe = false;
		this.EstFilsGauche = filsGauche;
	}

	/**
	 * Retourne le père
	 */
	public Cnoeud getPere() {
		return pere;
	}

	/**
	 * Retourne le fils gauche
	 */
	public Cnoeud getFgauche() {
		return fgauche;
	}

	/**
	 * Retourne le fils droit
	 */
	public Cnoeud getFdroit() {
		return fdroit;
	}

	/**
	 * Retourne le coût
	 */
	public int getCout() {
		return cout;
	}

	/**
	 * Retourne si le noeud est coupé ou non
	 */
	public boolean EstCoupe() {
		return EstCoupe;
	}

	/**
	 * Modifier l'état coupé ou non du noeud
	 */
	public void setEstCoupe(boolean estCoupe) {
		EstCoupe = estCoupe;
	}

	/**
	 * Retourne l'arc pris par le noeud
	 */
	public int[] getArc() {
		return arc;
	}

	/**
	 * Retourne l'étage du noeud
	 */
	public int getEtage() {
		return etage;
	}

	/**
	 * Retourne la matrice du noeud
	 */
	public Cmatrice getMatrice(){
		return new Cmatrice(matrice);
	}

	/**
	 * Détermine si un noeud est feuille
	 * @return si c'est une feuille vrai sinon faux
	 */
	public boolean EstFeuille() {
		if (this.etage == matrice.getTaille() - 2) {
			return true;
		} else
			return false;
	}

	/**
	 * Méthode de séparation d'un noeud
	 */
	public void Separation() {
		int D = matrice.reduction();
		
		// SI c'est un fils gauche alors on ajoute à son coût le coût de la réduction
		if(EstFilsGauche) this.cout += D;

		int coordonne[] = new int[3];
		coordonne = matrice.arc_plus_grand_regret();
		if(coordonne[2] == -1)
			this.setEstCoupe(true);

		if ((!this.EstFeuille()) && (!this.EstCoupe())) {
			// FILS GAUCHE
			fgauche = new Cnoeud(null, this, null, this.matrice, this.cout, this.etage + 1, coordonne[0], coordonne[1], true);
			fgauche.matrice.suppression_chemin_pris(coordonne[0], coordonne[1]);
			fgauche.matrice.suppression_chemin_non_pris(coordonne[1], coordonne[0]);
			fgauche.Arc_interdit();

			// FILS DROIT
			fdroit = new Cnoeud(null, this, null, this.matrice, this.cout + coordonne[2], this.etage, -1, -1, false);
			fdroit.matrice.suppression_chemin_non_pris(coordonne[0], coordonne[1]);
		}
	}
	
	/**
	 * Méthode de détection d'une boucle si l'on ajoute l'arc départ->arrivée
	 * @param tabArcs Tableau des arcs pris
	 * @param depart Départ de l'arc testé
	 * @param arrivee Arrivée de l'arc testé
	 * @return si une boucle existe vrai sinon faux
	 */
	public boolean dectectionBoucle(int [][] tabArcs, int depart, int arrivee){
		int [] sortie = new int [matrice.getTaille()];
		boolean [] verification = new boolean [matrice.getTaille()];
		int i, k;
		
		// Initialisation
		for(i = 0; i < matrice.getTaille(); ++i){
			sortie[i] = -1;
			verification[i] = false;
		}
		
		for(i = 0; i < tabArcs.length; ++i){
			sortie[tabArcs[i][0]] = tabArcs[i][1];
		}
		
		// Ajout de l'arc à tester
		sortie[depart] = arrivee;
		
		// Vérification que l'arc ne créé pas de boucle
		for(i = 0, k = depart; (i < matrice.getTaille()) && (sortie[k] != -1); ++i, k = sortie[k]){
			if(verification[sortie[k]] == true){
				//System.out.println("Arc interdit : " + depart + " -> " + arrivee);
				return true;
			}
			else
				verification[sortie[k]] = true;
		}
		
		return false;
	}

	/**
	 * Méthode de détection des arcs interdits
	 */
	public void Arc_interdit(){
		Cnoeud tmp;
		int i, j;
		int tabArcs[][] = new int[this.etage][2];
		
		// Initialisation
		for(i = 0, tmp = this; (tmp != null) && (i < this.etage); tmp = tmp.pere){
			if(tmp.arc[0] != -1){
				tabArcs[i][0] = tmp.arc[0];
				tabArcs[i][1] = tmp.arc[1];
				
				++i;
			}
		}
		// Détection des arcs interdits
		for(i = 0; i < this.etage; ++i){
			for(j = 0; j < this.etage; ++j){
				if((i != j) && (tabArcs[j][1] != tabArcs[i][0]) && (matrice.getValeur(tabArcs[j][1], tabArcs[i][0]) != -1)){
					// SI l'arc peut créer une boucle alors on le supprime
					if(dectectionBoucle(tabArcs, tabArcs[j][1], tabArcs[i][0])){
						matrice.suppression_chemin_non_pris(tabArcs[j][1], tabArcs[i][0]);
					}
				}
			}
		}
	}
}