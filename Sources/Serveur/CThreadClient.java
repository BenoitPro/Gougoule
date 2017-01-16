/**** Classe CThreadClient ******
 * Classe qui recoit les requ�tes des clients (un String recus de l'applet Client par socket)
 *      -Elle est reli� au client par l�interm�diaire d�un socket
 *	-Elle lance la recherche sur l'index qui lui renvoie en vrac' les r�sultats
 *	-Elle trie les r�sultats par "poid"
 *	-Les renvoient au client
 *
 *@autor Anthony , 5 mai 2005
 ****************************/
import Annex.* ;
import java.util.*;        
import java.util.regex.*; 
import java.net.* ; // pour le Socket
import java.io.* ;  // pour les flux (PrintWriter & BufferedReader)


public class CThreadClient extends Thread {
    
    /** Variables de classes **/
    private CIndex      _index;                 // r�f�rence vers l'index
    private ArrayList   _listeLiens;            // ArrayList qui contient les ArrayLists de chaque mots demand� par l'utilisateur
    private String      _motsUtilisateur;       // la chaine de caract�re que l'utilisateur � taper dans le champ "rechercher"
    private ArrayList   _motsClePourRecherche;  // le r�sultat du traitement de la chaine taper par l'utilsateur fourni une ArrayList de mot cle sur lesquels ont va lanc� la r�cup�ration de leur Vector de liens (contenu dans l'index)
    private String[][]  _liensClasser;          // tableau final, avec tout les liens class�s correspond au(x) mot(s). (le Tableau doit etre transformer en String pour "pass� par le socket" vers l'applet client)
    private Socket      _socket ;               // Socket connect� au client
    private PrintWriter _out ;                  // flux de sortie vers le client
    private BufferedReader _in ;                // flux d'entr� des requ�tes du client
    private int         _nbLiens ;              // compte le nb de lien trouver pour la requete du client 
    private int         _nbLiensVisiter;        // nombre total de pages aspirer par notre moteur
    
    /** Constructeur **/
    public CThreadClient(Socket sock,CIndex ind,int nbLiensVisiter) {
        this._socket = sock;                    // on d�fini le socket
        this._index = ind;                      // on d�fini la r�f�rence vers l'index
        this._motsUtilisateur = "";             // on initialise a vide
        this._nbLiensVisiter=nbLiensVisiter;
        try {
            _out = new PrintWriter(_socket.getOutputStream());                              // connexion du flux de sortie vers le client
            _in = new BufferedReader( new InputStreamReader(_socket.getInputStream() ) );   // connexion du flux d'entr� des requ�tes du client
        }
        catch (IOException e) { System.out.println("CThreadClient> EXCEPTION, lors de l'envoi/reception de flux : "+e); }
        start();    // lancement du CThreadClient
    }
    
    
    /** Lancement du Thread : lancement effectu� par le Client **/
    public void run() {
        System.out.println("\nCThreadClient> Lancement du Thread...");
        System.out.println("CThreadClient> Infos Socket : "+_socket);
        _listeLiens = new ArrayList();           // cr�ation de l'AL de liens correspondant a tout les mots cl� demand� par l'utilisateur
        _motsClePourRecherche = new ArrayList(); // c'est une ArrayList de String issu du "calcul" de la regex, on aura ici de "bon" mots cl� que dont on pourra chercher les liens correspondants

        /* R�cup�ration de la requ�te du client */
        try {
            char charCur[] = new char[1] ;
            while(_in.read(charCur,0,1)!= -1) {     // on traite la requ�te cliente caract�re par caract�re

                if ( (charCur[0] != '\u0000') && (charCur[0] != '\n')) { // tant qu'on est pas en fin de chaine...
                    _motsUtilisateur+=charCur[0] ;  //... on concat�ne le  caract�re � la chaine d�j� cr��
                }
                else if(!_motsUtilisateur.equalsIgnoreCase("")) {
                    _motsUtilisateur = donnerValeurAttribut("value",_motsUtilisateur);  // on ne prend que les donn�es de l'attribut "value"
                    _motsClePourRecherche = regexMotsUtilisateur(_motsUtilisateur);     // traitement par une regex des mots fournit par l'utilisateur afin d'avoir des mots cl�s exploitable
                    for (int i=0; i<_motsClePourRecherche.size(); i++) {                // parcourt de tout les "bons" mots cl�s et r�cup�ration des urls correspondantes
                        String mot = (String) _motsClePourRecherche.get(i);             // on r�cup�re les mots de l'AL issu du traitement de la regex
                        rechercherLiens(mot);   // methode void, qui recopie tout les objets contenue dans le vector correspondant � un mot cl�
                    }
                    trierListeLiens();          // m�thode permettant de trier par poid toute les url trouv�s (qui correspond donc aux mots donn�s par l'utilisateur)
                    envoyerResultats();         // envoie des r�sulats au client Flash
                    /* R�initialisation */
                    _motsUtilisateur="";
                    _listeLiens.removeAll(_listeLiens);
                    _motsClePourRecherche.removeAll(_motsClePourRecherche);
                    _liensClasser=null;
                    _nbLiens=0;                
                }
            }
        }
        catch(Exception e) { System.out.println("CThreadClient> 01 EXCEPTION, lors du run du threadClient : "+e); }
        /* Le client n'est plus �couter */
        finally {
            try {
                /* (C) Affichage lorsqu'un client se d�connecte */
                System.out.println("CThreadClient> un client s'est deconnecte ");
                _out.close();
                _in.close();
                _socket.close() ;
            }
            catch(IOException e) { System.out.println("CThreadClient> 02 EXCEPTION lors du _socket.close : "+e) ; }
        }
    }
    
    
    /** M�thode cr�ant une ArrayList d'objet CURLPoid correspondant aux mots cl�s donn�s par l'utilisateur puis trait� par la regex **/
    public void rechercherLiens(String motCle){
        Vector vt=_index.rechercher(motCle);        // vt est le Vector du mot courant, dont tout ses objets vont se voir ajouter dans listeLiens
        if (vt!=null) {                             // si le motCle demand� est pr�sent dans l'index 
            
            /* (C) Affichage de tout les liens pour un mot donn� */
            System.out.println("CThreadClient> voici les url trouvez pour le mot :"+motCle);
            
            for (int i=0; i<vt.size() ; i++) {          // on va recopier tout les objets de la liste vt et les ajouter dans listeLiens     
                CURLPoid up= (CURLPoid) vt.elementAt(i);// ici on recupere ce qu'il y dans dans la "valeur" de la hastable, donc un objet CURLPoid
                _listeLiens.add(up);                    // on ajoute tout les objets CURLPoid dans la liste
                System.out.println(up.getUrl()+" (poid :"+up.getPoid()+")");
            }
            
            System.out.println("");
        }
    }
    
    /** Analyse des mots tap�s par l'utilisateur pour en extraire de nouveau afin de mieux exploit� l'index :
     *      -D�limitation des mots gr�ce � la regex
     *      -Mise en minuscule
     *      -Remplacement des caract�res � accents par des caract�res sans accents
     **/
    public ArrayList regexMotsUtilisateur(String motsUtilisateur) {
       char t[][] = {{'�','e'},{ '�','e'},{'�','e'},{'�','u'},{'�','u'},{'�','o'},{'�','o'},{'�','o'},{'�','n'},{'�','c'},{'�','a'},{'�','a'},{'�','a'},{'�','i'} ,{'�','i'} };
       
       Pattern pat= Pattern.compile("[^a-zA-Z_0-9�������������'-]");// tout les caract�res entr�s dans la regex correspondent aux caract�res "non-d�limitateur de mot"
       String [] liste = pat.split(motsUtilisateur.toLowerCase());  // mise en minuscule des chaines dans le tableau liste
       ArrayList alListe= new ArrayList();                  // ArrayList qui contiendra "toutes les Vector" correspondant � chaque mot cl� trouv�
       for (int i = 0 ; i<liste.length ; i++) {             // on parcourt tout les mots fournit par l'utilisateur que nous venons de trait�
           if ( (liste[i].length()>2)) {                    // on ne concid�re que les mots de plus de 2 lettres fournit par l'utilisateur car il n'existe pas ce genre de mot dans l'index
               for (int j=0 ; j<t.length ; j++)             // on parcourt tout le tableau de caract�re � accent
                   for (int k=0 ; k<liste[i].length(); k++) // si on trouve un caract�re � accent on le remplace par le caract�re sans accent
                       if (t[j][0]==liste[i].charAt(k))     
                           liste[i]=liste[i].replace(liste[i].charAt(k),t[j][1]);
               alListe.add(liste[i]);                       // on ajoute le mot trait� dans son ArrayList
           }
       }
        
      
       /* (C) M�thode de controle : Affichage des mots issus du traitement par regex. Ces mots seront ceux
            qui seront utilis� comme "cl�" par la recherche des urls dans l'index, il est donc pr�f�rable 
            qu'ils aient le  m�me "format" (+ de 2 lettres, en minuscule, sans accent, etc.) d'ou nos traitments */
       ///*
       System.out.println("\n\n ------------------------------------------\nCThreadClient> NOUVELLE requete du client");
       System.out.println("CThreadClient> Mots finaux :");
       System.out.print("CThreadClient> ");
       for (int i=0 ; i<alListe.size() ; i++) {
                System.out.print(alListe.get(i));
                if (i<alListe.size()-1)
                    System.out.print("+");
       }
       System.out.println("");
       
       return alListe;
    }
    
    
    /** Trie par poid de la liste de liens r�cup�r� dans l'index **/
    public void trierListeLiens() {
        _liensClasser = new String[_listeLiens.size()][2];
        _nbLiens=1;         // on initialise � 1 car on "ne passe pas" sur le liens que l'on a pris (donc 0 signifirait 0 liens similaire trouv�)
        int tmpPoid=0;      // variable permettant de faire les calculs sur le poid des liens
        int i=0;
        while (i<_listeLiens.size()) {
            _nbLiens = 1;           // on l'initialise � 1 car il y a forc�ment 1 liens identique � lui mm ! (on le comptera pas car la deuxieme boucle fait if(i!=j) 
            CURLPoid up= (CURLPoid) _listeLiens.get(i);
            tmpPoid = up.getPoid(); //on initalise le poid a celui du lien
            int j=0;
            while (j<_listeLiens.size()) {
                if (i!=j) {         // on v�rifie que nous ne sommes pas entrain de regarder le "m�me" lien. et pour ne pas supprimer "le lien" (objet CURLPoid) de d�part ! En effet nous supprimons les doublons.
                    CURLPoid up2= (CURLPoid) _listeLiens.get(j);
                    if (up.getUrl().equals(up2.getUrl())){ 
                        _nbLiens++;             // on incr�ment le nb liens identique trouv�
                        tmpPoid+=up2.getPoid(); // on additionne tout les poids des urls identiques
                        _listeLiens.remove(j);  // on supprime le liens "doublon"
                        j--;                    //on d�cremente j car on va l'incr�ment� apres, il restera donc a la m�me valeur
                    }
                }
                j++;
            }
            _liensClasser[i][0]=up.getUrl(); // ici on a donc fini les traitements pour la "i �me" url on va donc l'ajouter dans le tableau
            int tmp;
            if (_nbLiens<2)             // si il n'y a qu'un lien identique alors le poid est celui du lien
                tmp= tmpPoid;
            else                        // sinon on effectue un cacul permettant en quelque sorte de rang� les liens par "Classe" le score en effet
                                        // est multipli� par 10 � chaque fois que le liens est apparut
                tmp= (int)Math.pow(10,_nbLiens)+tmpPoid;
            
            _liensClasser[i][1]=String.valueOf(tmp);// selon le nb de liens on
            i++;                                    // modifie le poid du mot cl�
        }
        triBulle(i);                    // on lance le tri � bulle sur le tableau afin d'avoir les urls class�es par poid
   }
    
    
    /** Trie � bulle d'un tableau � deux dimension **/
    public void triBulle(int taille) {
	boolean test;
	String tmp1;
        String tmp2;
	do{
            test=true;
            for(int i=0;i<taille-1;i++){
                if(Integer.parseInt(_liensClasser[i][1]) < Integer.parseInt(_liensClasser[i+1][1])){
                    tmp1=_liensClasser[i][0];                 // on inverse les variables
                    tmp2=_liensClasser[i][1];                 // gr�ce a deux variables temporaire
                    _liensClasser[i][0]=_liensClasser[i+1][0];// une pour l'url comme ici
                    _liensClasser[i][1]=_liensClasser[i+1][1];// et une pour le poid comme ici
                    _liensClasser[i+1][0]=tmp1;
                    _liensClasser[i+1][1]=tmp2;
                    test=false;
                }
            }
	}
        while(test!=true);
    }
    
    
    /** M�thode qui renvoie la valeur d'un attribut dans la chaine "message" (�crit en XML) **/
    public String donnerValeurAttribut(String attribut,String message) {
        int tatt= attribut.length();
        int tmess= message.length(); 
        int deb=0;
        int fin;
        while(!message.substring(deb,deb+tatt).equals(attribut) && (deb+tatt<=tmess) ) {
            deb++;
        }
        deb=deb+tatt + 2 ;
        fin=deb;
        while(message.charAt(fin) != '\"') {
            fin++;            
        }
        return message.substring(deb,fin);
    }
    
 
    /** Envoie des liens trouv�s dans l'index selon les mots cl� donn�s par l'utilisateur **/
    public void envoyerResultats() {
        
        /* (C) Affichage des r�sultats d'apr�s les mots donn�s par l'utilisateur */
        System.out.println("CThreadClient> Affichages des resultats (d'apres les mots cles : \""+_motsUtilisateur+"\")");
        
        int i=0;
        while((i<_liensClasser.length) && (_liensClasser[i][1]!=null)) {
            
            CTitreDesc td = (CTitreDesc) _index.rechercherUrl(_liensClasser[i][0]);
            String titre = td.getTitre();
            String desc = td.getDesc();

            /* R�ponse du serveur formater en XML ajouter au flux de sortie vers le client Flash */
            _out.print("<reponse value=\""+_liensClasser[i][0]+"\"  titre=\""+titre+"\" poid=\""+_liensClasser[i][1]+"\" description=\""+desc+"\" />\u0000");
            /* (C) Affichage en console des chaines envoy�es au client Flash ou Applet Java */
            System.out.println("<reponse value=\""+_liensClasser[i][0]+"\"  titre=\""+titre+"\" poid=\""+_liensClasser[i][1]+"\" description=\""+desc+"\" />");
            i++;
        }
        _out.print("<resultat nbLiens=\""+i+"\" totalLiens=\""+_nbLiensVisiter+"\"  />\u0000");
        System.out.println("<resultat nbLiens=\""+i+"\" totalLiens=\""+_nbLiensVisiter+"\"  />\u0000");
        _out.flush();
    }    
}
