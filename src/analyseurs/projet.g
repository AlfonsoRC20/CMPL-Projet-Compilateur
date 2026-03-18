/*
 * Grammaire du langage PROJET
 * CMPL L3info 
 * Nathalie Girard, Veronique Masson, Laurent Perraudeau
 * 
 * Il convient d'y insérer les appels aux points de génération
 * de la forme {PtGen.pt(k);}
 * 
 * Attention : relancer ANTLR après chaque modification des 
 * points de génération, pour regénérer les analyseurs.
 */

grammar projet;

options {
	language = Java;
	k = 1;
}

@lexer::header { 
  package analyseurs ;

  import libIO.*;
  import compilateur.*;
}

@parser::header {
	package analyseurs ;   

 	import libIO.*;       
  	import compilateur.*;

	import java.io.IOException;
	import java.io.DataInputStream;
	import java.io.FileInputStream;

}

/*
 * Partie syntaxique : description de la grammaire
 * les non-terminaux doivent commencer par une minuscule
 */

@members {
// variables globales et methodes utiles à placer ici
  
}

@rulecatch {
// la directive rulecatch permet d'interrompre l'analyse à la première erreur de syntaxe
	catch (RecognitionException e) {
		reportError (e) ;
		throw e ; 
	}
}

unite:
	unitprog {PtGen.pt(255);} EOF
	| unitmodule EOF;

unitprog:
	'programme' ident ':' declarations corps { System.out.println("succès, arrêt de la compilation "); 
		};

unitmodule:
	'module' ident ':' declarations;

declarations:
	partiedef? partieref? consts? vars? {PtGen.pt(38);} decprocs?;

partiedef:
	'def' ident (',' ident)* ptvg;

partieref:
	'ref' specif (',' specif)* ptvg;

specif:
	ident ('fixe' '(' type ( ',' type)* ')')? (
		'mod' '(' type ( ',' type)* ')'
	)?;

consts:
	'const' (ident '=' valeur {PtGen.pt(26);} ptvg)+;

vars:
	'var' (type ident {PtGen.pt(27);}( ',' ident {PtGen.pt(27);})* ptvg)+ {PtGen.pt(28);};

type:
	'ent'{PtGen.pt(2);}
	| 'bool'{PtGen.pt(3);};

decprocs: (decproc ptvg)+;

decproc:
	'proc' ident{PtGen.pt(42)} parfixe? parmod?{PtGen.pt(45);} consts? vars? corps;

ptvg:
	';'
	|;

corps:
	'debut'{PtGen.pt(39);} instructions 'fin'{PtGen.pt(254);};

parfixe:
	'fixe' '(' pf  (';' pf)* ')';

pf:
	type ident {PtGen.pt(43);}(',' ident{PtGen.pt(43);})*;

parmod:
	'mod' '(' pm  (';' pm)* ')';

pm:
	type ident {PtGen.pt(44);}(',' ident{PtGen.pt(44);})*;

instructions:
	instruction  (';' instruction)*;

instruction:
	inssi
	| inscond
	| boucle
	| lecture
	| ecriture
	| affouappel
	|;

inssi:
	'si' expression {PtGen.pt(30);}'alors' instructions ('sinon'{PtGen.pt(31);} instructions)? 'fsi'{PtGen.pt(32);};

inscond:
	'cond' {PtGen.pt(35);} expression {PtGen.pt(30);}':' instructions (
		','{PtGen.pt(36);} expression {PtGen.pt(30);}':' instructions
	)* ('aut' {PtGen.pt(36);} instructions |) 'fcond'{PtGen.pt(37);};

boucle:
	'ttq'{PtGen.pt(33);} expression {PtGen.pt(30);}'faire' instructions {PtGen.pt(34);}'fait';

lecture:
	'lire' '(' ident {PtGen.pt(24);}(',' ident{PtGen.pt(24);})* ')';

ecriture:
	'ecrire' '(' expression {PtGen.pt(25);}(',' expression{PtGen.pt(25);})* ')';

affouappel:
	ident {PtGen.pt(40);} (':=' expression {PtGen.pt(41);}| (effixes (effmods)?)?);

effixes:
	'(' (expression {PtGen.pt(49);} (',' expression {PtGen.pt(49);})*)? ')';

effmods:
	'(' (ident {PtGen.pt(47);} (',' ident{PtGen.pt(47);})*)? ')';

expression: (exp1) ({PtGen.pt(15);}'ou' exp1 {PtGen.pt(15);} {PtGen.pt(17);})*;

exp1:
	exp2 ({PtGen.pt(15);}'et' exp2{PtGen.pt(15);} {PtGen.pt(18);})*;

exp2:
	'non' exp2 {PtGen.pt(15);} {PtGen.pt(19);}
	| exp3;

exp3:
	exp4 ({PtGen.pt(16);}
		'=' exp4 {PtGen.pt(16);}{PtGen.pt(9);}
		|{PtGen.pt(16);} '<>' exp4 {PtGen.pt(16);}{PtGen.pt(10);}
		|{PtGen.pt(16);} '>' exp4 {PtGen.pt(16);}{PtGen.pt(11);}
		|{PtGen.pt(16);} '>=' exp4 {PtGen.pt(16);}{PtGen.pt(12);}
		|{PtGen.pt(16);} '<' exp4 {PtGen.pt(16);}{PtGen.pt(13);}
		|{PtGen.pt(16);} '<=' exp4 {PtGen.pt(16);}{PtGen.pt(14);}
	)?;

exp4:
	exp5 ({PtGen.pt(16);}'+' exp5 {PtGen.pt(16);}{PtGen.pt(5);}|{PtGen.pt(16);} '-' exp5 {PtGen.pt(16);}{PtGen.pt(6);})*;

exp5:
	primaire ({PtGen.pt(16);}'*' primaire {PtGen.pt(16);} {PtGen.pt(7);}| {PtGen.pt(16);}'div' primaire {PtGen.pt(16);}{PtGen.pt(8);})*;

primaire:
	valeur{PtGen.pt(1);}
	| ident {PtGen.pt(4);}
	| '(' expression ')';

valeur:
	nbentier {PtGen.pt(2);} {PtGen.pt(29);}
	| '+' nbentier {PtGen.pt(2);} {PtGen.pt(20);}
	| '-' nbentier {PtGen.pt(2);} {PtGen.pt(21);}
	| 'vrai' {PtGen.pt(3);} {PtGen.pt(22);}
	| 'faux'{PtGen.pt(3);} {PtGen.pt(23);};

/*
 * Partie lexicale  : cette partie ne doit pas être modifiée
 * Les unités lexicales ANTLR sont en majuscules
 */

// Attention : ANTLR n'autorise pas certains traitements sur les unites lexicales, 
// il est alors ncessaire de passer par un non-terminal intermediaire 
// exemple : pour l'unit lexicale INT, le non-terminal nbentier a du etre introduit

nbentier:
	INT { UtilLex.valEnt = $INT.int; }; // mise à jour de valEnt

ident:
	ID { UtilLex.traiterId($ID.text); }; // mise à jour de numIdCourant

// Les identifiants commencent obligatoirement par une lettre
ID: ('a' ..'z' | 'A' ..'Z') (
		'a' ..'z'
		| 'A' ..'Z'
		| '0' ..'9'
		| '_'
	)*;

INT:
	'0' ..'9'+;

// On ignore les blancs et tabulations
WS: (' ' | '\t' | '\r')+ { skip(); };

// Définition d'un unique "passage à la ligne" et comptage des numéros de lignes
RC: ('\n') {UtilLex.incrementeLigne(); skip() ;};

// Définition des commentaires : 
// Tout ce qui suit un caractère dièse sur une ligne est un commentaire
// Toute suite de caractères entre accolades est un commentaire
COMMENT:
	'#' ~('\r' | '\n')* {skip();}
	| '\{' (.)* '\}' {skip();};