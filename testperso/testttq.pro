programme exempleChapIV :
    const val = 10 ; b=faux ;
    var ent u, v ;
        bool x ;
        ent k, a;
debut
    lire(a,k,x) ;
    ecrire (5+a*k-val>val et x ) ;
    u:=3 ;
    si x alors
        v:=u+1
    sinon
        v:=u-1
    fsi ;
    ttq v<100 faire
        v:=v*v
    fait;
    ecrire(v) ;
fin