package compilateur;

import org.antlr.grammar.v3.ANTLRParser.elementNoOptionSpec_return;

import analyseurs.UtilLex;
import edl.*;
import libIO.*;

/**
 * classe de mise en oeuvre du compilateur
 * =======================================
 * (verifications semantiques + production du code objet)
 * 
 * @author Girard, Masson, Perraudeau
 *
 */

public class PtGen {

	// Renseigner ici un nom pour le trinome, constitué UNIQUEMENT DE LETTRES
	public static String trinome = "PAILLIERRAMIREZCAETANO";

	// taille max de la table des symboles
	private static final int MAXSYMB = 300;

	// TABLE DES SYMBOLES
	private static EltTabSymb[] tabSymb = new EltTabSymb[MAXSYMB + 1];
	private static int it; // indice de remplissage de tabSymb
	private static int bc; // bloc courant (=1 si le bloc courant est le programme principal)

	// codes MAPILE
	private static final int RESERVER = 1, EMPILER = 2, CONTENUG = 3, AFFECTERG = 4,
			OU = 5, ET = 6, NON = 7, INF = 8, INFEG = 9, SUP = 10, SUPEG = 11, EG = 12, DIFF = 13,
			ADD = 14, SOUS = 15, MUL = 16, DIV = 17,
			BSIFAUX = 18, BINCOND = 19,
			LIRENT = 20, LIREBOOL = 21, ECRENT = 22, ECRBOOL = 23,
			ARRET = 24,
			EMPILERADG = 25, EMPILERADL = 26, CONTENUL = 27, AFFECTERL = 28, APPEL = 29, RETOUR = 30;

	// codes des valeurs vrai/faux
	private static final int VRAI = 1, FAUX = 0;

	// types permis
	private static final int ENT = 1, BOOL = 2, NEUTRE = 3;

	// categories des identificateurs
	private static final int CONSTANTE = 1, VARGLOBALE = 2, VARLOCALE = 3,
			PARAMFIXE = 4, PARAMMOD = 5, PROC = 6, DEF = 7, REF = 8, PRIVEE = 9;

	// production du code objet en memoire
	private static ProgObjet po;

	// pile de reprise pour les branchements en avant et en arrière
	private static TPileRep pileRep;

	// compilation des littéraux (entier ou booléen)
	private static int vCour;

	// contrôle de type : type de l'expression compilee
	private static int tCour;

	private static int nbrVarGlobale,idIdent,nbrVarLocale,nbrparam,nbparamAt,nbparamRe;

	// TABLE DES SYMBOLES
	// ----------------------
	/**
	 * utilitaire de recherche de l'ident courant (ayant pour code
	 * UtilLex.numIdCourant) dans tabSymb
	 * 
	 * @param borneInf recherche de l'indice it vers borneInf (=1 si recherche
	 *                 dans tout tabSymb)
	 * @return indice de l'ident courant (de code UtilLex.numIdCourant) dans
	 *         tabSymb (O si absence)
	 */
	private static int presentIdent(int borneInf) {
		int i = it;
		while (i >= borneInf && tabSymb[i].code != UtilLex.numIdCourant)
			i--;
		if (i >= borneInf)
			return i;
		else
			return 0;
	}

	/**
	 * utilitaire de placement des caracteristiques d'un nouvel ident dans tabSymb
	 * 
	 * @param code UtilLex.numIdCourant de l'ident
	 * @param cat  categorie de l'ident parmi CONSTANTE, VARGLOBALE, PROC, etc.
	 * @param type ENT, BOOL ou NEUTRE
	 * @param info valeur pour une constante, ad d'exécution pour une variable, etc.
	 */
	private static void placeIdent(int code, int cat, int type, int info) {
		if (it == MAXSYMB)
			UtilLex.messErr("debordement de la table des symboles");
		it = it + 1;
		tabSymb[it] = new EltTabSymb(code, cat, type, info);
	}

	/**
	 * utilitaire d'affichage de la table des symboles
	 */
	private static void afftabSymb() {
		System.out.println("       code           categorie      type    info");
		System.out.println("      |--------------|--------------|-------|----");
		for (int i = 1; i <= it; i++) {
			if (i == bc) {
				System.out.print("bc=");
				Ecriture.ecrireInt(i, 3);
			} else if (i == it) {
				System.out.print("it=");
				Ecriture.ecrireInt(i, 3);
			} else
				Ecriture.ecrireInt(i, 6);
			if (tabSymb[i] == null)
				System.out.println(" reference NULL");
			else
				System.out.println(" " + tabSymb[i]);
		}
		System.out.println();
	}

	// VERIFICATION DE TYPE
	// ----------------------

	/**
	 * verification du type entier de l'expression en cours de compilation
	 * (arret de la compilation sinon)
	 */
	private static void verifEnt() {
		if (tCour != ENT)
			UtilLex.messErr("expression entiere attendue");
	}

	/**
	 * verification du type booleen de l'expression en cours de compilation
	 * (arret de la compilation sinon)
	 */
	private static void verifBool() {
		if (tCour != BOOL)
			UtilLex.messErr("expression booleenne attendue");
	}

	// COMPILATION SEPAREE
	// -------------------

	// Valeurs possible du vecteur de translation
	private static final int TRANSDON = 1, TRANSCODE = 2, REFEXT = 3;

	// descripteur associe a un programme objet
	private static Descripteur desc;

	/**
	 * modification du vecteur de translation associe au code produit
	 * + incrementation attribut nbTransExt du descripteur
	 * NB: effectue uniquement si c'est une reference externe ou si on compile un
	 * module
	 * 
	 * @param valeur TRANSDON, TRANSCODE ou REFEXT
	 */
	private static void modifVecteurTrans(int valeur) {
		if (valeur == REFEXT || desc.getUnite().equals("module")) {
			po.vecteurTrans(valeur);
			desc.incrNbTansExt();
		}
	}

	// À COMPLÉTER SI BESOIN
	// ---------------------

	/**
	 * initialisations A COMPLETER SI BESOIN
	 * -------------------------------------
	 */
	public static void initialisations() {

		// indices de gestion de la table des symboles
		it = 0;
		bc = 1;

		// pile des reprises pour compilation des branchements en avant
		pileRep = new TPileRep();
		// programme objet = code Mapile de l'unite en cours de compilation
		po = new ProgObjet();
		// COMPILATION SEPAREE: desripteur de l'unite en cours de compilation
		desc = new Descripteur();

		// initialisation necessaire aux attributs lexicaux
		UtilLex.initialisation();

		// initialisation du type de l'expression courante
		tCour = NEUTRE;

		// TODO si necessaire
		nbrVarGlobale = 0;
		nbrVarLocale = 0;
		nbrparam = 0;
	}

	/**
	 * code des points de generation A COMPLETER
	 * -----------------------------------------
	 * 
	 * @param numGen : numero du point de generation a executer
	 */
	public static void pt(int numGen) {
		int tmp;
		int repbsif;
		switch (numGen) {
			case 0:
				initialisations();
				break;
			case 1:
				po.produire(EMPILER);
				po.produire(vCour);
				break;
			case 2:
				tCour = ENT;
				break;
			case 3:
				tCour = BOOL;
				break;
			case 4://empilement de la valeur de la constante ou de la variable
				tmp = presentIdent(1);
				if (tabSymb[tmp].categorie == CONSTANTE) {
					po.produire(EMPILER);
					po.produire(tabSymb[tmp].info);
				}else if(bc== 1) {
					po.produire(CONTENUG);
					po.produire(tabSymb[tmp].info);
				}else{
					po.produire(CONTENUL);
					po.produire(tabSymb[tmp].info);
					if (tabSymb[tmp].categorie == PARAMFIXE || tabSymb[tmp].categorie == VARLOCALE) {
						po.produire(0);
					}else po.produire(1);
				} 
				tCour = tabSymb[tmp].type;
				break;
			case 5: // addition
				po.produire(ADD);
				break;
			case 6:// soustraction
				po.produire(SOUS);
				break;
			case 7: // multiplication
				po.produire(MUL);
				break;
			case 8: //division
				po.produire(DIV);
				break;
			case 9: // '='
				po.produire(EG);
				tCour = BOOL;
				break;
			case 10: // '<>'
				po.produire(DIFF);
				tCour = BOOL;
				break;
			case 11: // '>'
				po.produire(SUP);
				tCour = BOOL;
				break;
			case 12:// '>='
				po.produire(SUPEG);
				tCour = BOOL;
				break;
			case 13:// '<'
				po.produire(INF);
				tCour = BOOL;
				break;
			case 14:// '<='
				po.produire(INFEG);
				tCour = BOOL;
				break;
			case 15:// vérification boolean
				verifBool();
				break;
			case 16:// verification entier
				verifEnt();
				break;
			case 17: // gestion du OU
				po.produire(OU);
				break;
			case 18:// gestion du ET
				po.produire(ET);
				break;
			case 19:// gestion du non
				po.produire(NON);
				break;
			case 20:// entier positif
				vCour = 0 + UtilLex.valEnt;
				break;
			case 21:// entier négatif
				vCour = -UtilLex.valEnt;
				break;
			case 22:
				vCour = VRAI;
				break;
			case 23:
				vCour = FAUX;
				break;
			case 24:// gestion du lire
				tmp = presentIdent(1);
				if (tabSymb[tmp].categorie == CONSTANTE || tabSymb[tmp].categorie == PARAMFIXE) {
					UtilLex.messErr("une constante ou un paramètre fixe ne pas être modifier");
					break;
				}else if (tabSymb[tmp].type == ENT) {
					po.produire(LIRENT);
				}else{
					po.produire(LIREBOOL);
				}

				if (bc == 1) {
					po.produire(AFFECTERG);
					po.produire(tabSymb[tmp].info);
				}else{
					po.produire(AFFECTERL);
					po.produire(tabSymb[tmp].info);
					if (tabSymb[tmp].categorie == VARLOCALE) {
						po.produire(0);
					}else po.produire(1);
				}
				break;
			case 25:// gestion du ecrire
				if (tCour == ENT) {
					po.produire(ECRENT);
				}else po.produire(ECRBOOL);
				break;
			case 26:// gestion des constantes
				if(presentIdent(bc)!=0){
					UtilLex.messErr(UtilLex.chaineIdent(UtilLex.numIdCourant)+" est déja présente");
				}else placeIdent(UtilLex.numIdCourant, CONSTANTE, tCour, vCour);
				break;
			case 27://gestion des variables dans la table des symboles
				if (presentIdent(bc)!=0) {
					UtilLex.messErr(UtilLex.chaineIdent(UtilLex.numIdCourant)+" est déja déclaré");
				}else {
					if(bc== 1){
						placeIdent(UtilLex.numIdCourant, VARGLOBALE, tCour, nbrVarGlobale);
						nbrVarGlobale ++;
					}else{
						placeIdent(UtilLex.numIdCourant, VARLOCALE, tCour, nbrVarLocale+(2+nbrparam));
						nbrVarLocale++;
					}
				}
				break;
			case 28:// gestion des variables (reserver nbr)
				po.produire(RESERVER);
				if (bc==1) {	
					po.produire(nbrVarGlobale);
				}else po.produire(nbrVarLocale);
				break;
			case 29://entier quelconque
				vCour = UtilLex.valEnt;
				break;
	// gestion du si
			case 30: // empilement en pile reprise de l'adresse de la prochaine instruction a generer
                po.produire(BSIFAUX);
                po.produire(-1); // adresse de reprise a completer plus tard
                pileRep.empiler(po.getIpo());
                break;
            case 31: // gestion du sinon
                po.produire(BINCOND);
                po.produire(-1);
                int adresseReprise = pileRep.depiler();
                pileRep.empiler(po.getIpo());
                po.modifier(adresseReprise, po.getIpo()+1);
                break;
            case 32: // depilement de & reprise et modification du code de addresse de reprise
                    po.modifier(pileRep.depiler(), po.getIpo()+1);
                    break;
	//gestion du tant que
			case 33: // empiler ipo +1
				pileRep.empiler(po.getIpo()+1);
				break;
			case 34:
				repbsif = pileRep.depiler();
				po.produire(BINCOND);
				po.produire(pileRep.depiler());
				po.modifier(repbsif, po.getIpo()+1);
				break;
	//gestion du cond///
			case 35:
				pileRep.empiler(0);
				break;
			case 36:
				int adrBsifauxReme = pileRep.depiler(); 
    			int adrAncienBincond = pileRep.depiler(); 

    			po.produire(BINCOND);
    			po.produire(adrAncienBincond);

    			pileRep.empiler(po.getIpo()); 

    			po.modifier(adrBsifauxReme, po.getIpo() + 1);
    			break;
			case 37://fin du cond
				int destinationFinale = po.getIpo() + 1;
    			int adrActuelle = pileRep.depiler();

				if (po.getElt(adrActuelle) == -1) {
					po.modifier(adrActuelle, destinationFinale);
					adrActuelle = pileRep.depiler();
				}

				while (adrActuelle != 0) {
					int adrPrecedente = po.getElt(adrActuelle);
					po.modifier(adrActuelle, destinationFinale);
					adrActuelle = adrPrecedente;
				}
   				break;
			case 38://BINCOND après les variables globales
				po.produire(BINCOND);
				po.produire(-1);
				pileRep.empiler(po.getIpo());
				break;
			case 39://modification du BINCOND du case 38
				if (bc == 1) {
					int ipomodif = pileRep.depiler();
					po.modifier(ipomodif, po.getIpo()+1);
				}
				break;
			case 40:// sauvegarde de l'id pour l'affection ou l'appel
				idIdent = presentIdent(1);
				if (idIdent == 0) {
					UtilLex.messErr(UtilLex.chaineIdent(UtilLex.numIdCourant)+" n'est pas déclaré");
					break;
				}
				break;
			case 41://affectation
				switch (tabSymb[idIdent].categorie) {
					case CONSTANTE:
						UtilLex.messErr("une constante ne pas être modifier");
						break;
					case PARAMFIXE:
						UtilLex.messErr("un paramètre fixe ne pas être modifier");
						break;
					case PARAMMOD:
						po.produire(AFFECTERL);
						po.produire(tabSymb[idIdent].info);
						po.produire(1);
						break;
					case VARLOCALE:
						po.produire(AFFECTERL);
						po.produire(tabSymb[idIdent].info);
						po.produire(0);
						break;
					default:
						po.produire(AFFECTERG);
						po.produire(tabSymb[idIdent].info);
						break;
				}
				break;
			case 42://ajout de la Proc dans la table des symboles
				if(presentIdent(1)!=0){
					UtilLex.messErr(UtilLex.chaineIdent(UtilLex.numIdCourant)+" est déja déclaré");
				}else{
					placeIdent(UtilLex.numIdCourant, PROC, NEUTRE, po.getIpo()+1);
					placeIdent(-1,PRIVEE,NEUTRE,-1);
					bc=it+1;
				}
				break;
			case 43://gestion des parametre fixes
				if (presentIdent(bc)!=0) {
					UtilLex.messErr(UtilLex.chaineIdent(UtilLex.numIdCourant)+" est déja déclaré");
				}else{
					placeIdent(UtilLex.numIdCourant, PARAMFIXE, tCour, nbrparam);
					nbrparam ++;
				}
				break;
			case 44://gestion des parametre mod
				if (presentIdent(bc)!=0) {
					UtilLex.messErr(UtilLex.chaineIdent(UtilLex.numIdCourant)+" est déja déclaré");
					break;
				}else{
					placeIdent(UtilLex.numIdCourant, PARAMMOD, tCour, nbrparam);
					nbrparam ++;
				}
				break;
			case 45://modification de la case info pour mettre le nombre de parametres
				tabSymb[bc-1].info = nbrparam;
				break;
			case 46:
				po.produire(APPEL);
				po.produire(tabSymb[idIdent].info);
				po.produire(tabSymb[idIdent+1].info);
				break;
			case 47://gestion des param Mod dans un appel
				tmp=presentIdent(bc);
				if (tabSymb[tmp].type != tabSymb[bc+nbparamRe].type) {
					UtilLex.messErr("le type de l'expression ne correspond pas au type attendu");
				}
				if (tabSymb[bc+nbparamRe].categorie == PARAMFIXE) {
					UtilLex.messErr("le nombre de paramètre fixe entrés dans l'appel est incorrect");
				}
				if (tabSymb[tmp].categorie == VARGLOBALE) {
					po.produire(EMPILERADG);
					po.produire(tabSymb[tmp].info);
				}else if (tabSymb[tmp].categorie == CONSTANTE) {
					UtilLex.messErr("Une constante ne peut pas être un paramètre modifiable");
				}else{
					po.produire(EMPILERADL);
					po.produire(tabSymb[tmp].info);
					if(tabSymb[tmp].categorie==PARAMMOD){
						po.produire(1);
					}else po.produire(0);
				}
				nbparamRe ++;
				break;
			case 49://gestion des param fixe dans un appel
				if(tCour != tabSymb[bc+nbparamRe].type){
					UtilLex.messErr("le type de l'expression ne correspond pas au type attendu");
				}
				if (tabSymb[bc+nbparamRe].categorie == PARAMMOD) {
					UtilLex.messErr("le nombre de paramètre fixe entrés dans l'appel est incorrect");
				}
				nbparamRe++;
				break;
			case 254://fin de corps
				if (bc== 1){
					po.produire(ARRET);
				}else{
					nbrparam = tabSymb[bc-2].info;
					for(int i = bc;i<nbrparam;i++){
						tabSymb[i].code = -1;
					}
					it = bc+(nbrparam-1);
					po.produire(RETOUR);
					po.produire(nbrparam);
					bc =1;
				}
				break;
			case 255:
				// En fin de compilation :
				// - création des fichiers contenant le code produit (exécutable et mnémonique)
				// - affichage de la table des symboles
				// TODO À compléter si besoin
				po.constObj();
				po.constGen();
				afftabSymb();
				break;
			default:
				System.out.println("Point de generation non prevu dans votre liste");
				break;

		}
	}
}
