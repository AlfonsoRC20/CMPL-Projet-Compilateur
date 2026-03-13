**TP Compilation**
==================
Par : **Allan Paillier** et **Alfonso Ramirez**

## Version 1 : couverture

Nous avons terminé le travail à rendre pour la version 1, en complétant les différents fichiers de code :
- `PtGen.java`
- `projet.g`

En plus d’avoir ajouté différents tests individuels pour tester nos implémentations.

## Difficultés rencontrées :

L’implémentation du code, aussi bien dans les fichiers `PtGen.java` que dans `projet.g`, a progressé correctement, avec la création des différents points de génération dans la fonction :
```bash
public static void pt(int numGen)
```
Comme par exemple:
```bash
case 25:// gestion du ecrire
    if (tCour == ENT) {
			po.produire(ECRENT);
	}else po.produire(ECRBOOL);
	break;
```

Et les différents points de génération placés pour leur dernier traitement, comme par exemple:
```bash
boucle:
	'ttq'{PtGen.pt(33);} expression {PtGen.pt(30);}'faire' instructions {PtGen.pt(34);}'fait';
```

Une des difficultés que nous avons rencontrées a été au moment de faire le traitement de inscond. Le problème que nous avions était que lorsque les bincond font la chaîne pour changer le numéro, ils ne le changent pas.

Après avoir vu que l’erreur se trouvait dans le traitement fait dans les case 36: et case 37:, nous avons pu compléter correctement le travail.

