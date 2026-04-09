**TP Compilation**
==================
Par : **Allan Paillier** et **Alfonso Ramirez**

## Version Finale

Nous avons terminé le travail à rendre pour la version finale, en complétant les différents fichiers de code :
* `PtGen.java`
* `projet.g`

En plus d’avoir ajouté différents tests individuels pour tester nos implémentations.

---

## 1. Ce qui fonctionne

* **Gestion des variables et constantes :**
    * Déclaration et affectation des variables globales et locales.
    * Déclaration des constantes (qui sont protégées contre la modification).
* **Expressions arithmétiques et booléennes :**
    * Toutes les opérations de base (`+`, `-`, `*`, `/`) avec vérification sémantique de type (`verifEnt`).
    * Opérateurs logiques (`OU`, `ET`, `NON`) et relationnels (`<`, `>`, `=`, etc.) avec vérification (`verifBool`).
* **Structures de contrôle :**
    * Conditions `SI` / `SINON`.
    * Boucles `TANT QUE`.
    * Structure `COND`.
* **Procédures et fonctions :**
    * Déclaration de procédures.
    * Gestion des paramètres par valeur (`PARAMFIXE`) et par référence (`PARAMMOD`).
    * Appel de procédures avec vérification du nombre et du type des paramètres.
* **Entrées/Sorties :**
    * Instructions `LIRE` et `ECRIRE` pour les entiers et les booléens.

---

## 2. Ce qui ne fonctionne pas

* **Toutes les fonctionnalités requises sont opérationnelles.** Les tests fournis et nos propres tests individuels passent avec succès.
* Les erreurs sémantiques très spécifiques arrêtent le compilateur classiquement via `UtilLex.messErr`, mais la génération de code pour des cas syntaxiquement corrects est fonctionnelle.

---

## 3. Difficultés rencontrées et points problématiques

* **Gestion des branchements pour l'instruction `COND` (`inscond`) :**

    * Problème : Lors de l'implémentation de la structure `COND`, nous avons rencontré une difficulté majeure avec le chaînage des branchements inconditionnels (`BINCOND`). Le problème résidait dans le fait que, à la fin de chaque bloc conditionnel, l'adresse de saut vers la fin de la structure globale ne se mettait pas à jour correctement (les adresses temporaires restaient irrésolues).
    * Solution : En analysant l'état de notre pile de reprise (`pileRep`), nous avons compris que l'erreur provenait de notre logique dans les points de génération **36** et **37**. En corrigeant l'ordre de dépilement et l'application de la méthode `po.modifier()`, nous avons pu garantir que toutes les adresses en attente pointent correctement vers la destination finale de l'instruction.

* **Saut initial vers le programme principal (PtG 39) :**
    * Problème : Au début, nous avions oublié d'encadrer le traitement du `case 39` avec la condition `if (bc == 1)`. Ce point sert à mettre à jour le `BINCOND` (généré au pt 38) qui permet de chainer les déclarations de procédures pour aller directement au début de l'exécution du programme principal. Sans cette vérification, le compilateur essayait de dépiler une adresse même après avoir déclaré des variables locales à l'intérieur d'une procédure. Avec ça, on dépilait des adresses qui n'avaient rien à voir dans la `pileRep`, ce qui corrompait toute la logique des sauts.
    * Solution : L'ajout de `if (bc == 1)` nous a permis de restreindre ce comportement strictement au programme principal (quand on est dans l'environnement global), protégeant ainsi l'intégrité de la pile pour le reste de la compilation.

---

## 4. Optimisation du code

Une fois que nous avons réussi à avoir une version complète qui passait tous les tests, nous avons décidé de repasser sur notre code pour l'améliorer un peu. L'idée de cette section est de montrer les quelques modifications que nous avons faites pour éviter de générer des instructions inutiles dans la machine virtuelle et rendre notre compilateur un peu plus propre et efficace. Voici les points que nous avons modifiés :


```bash
    private static int exigerIdentDeclare() {
		int i = presentIdent(1);
		if (i == 0) {
			UtilLex.messErr("identificateur non déclaré");
		}
		return i;
    }
```

```bash
	private static void declarerSiAbsentBlocCourant(int cat, int type, int info, String messageSiPresent) {
        if (presentIdent(bc) != 0) {
            UtilLex.messErr(messageSiPresent);
        } else {
            placeIdent(UtilLex.numIdCourant, cat, type, info);
        }
    }
```

```bash
	private static void produireOpBinaire(int opcode, int typeResultat) {
		po.produire(opcode);
		tCour = typeResultat;
	}
```