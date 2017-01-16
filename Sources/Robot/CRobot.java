/****** Classe CRobot ******
 * Il s'agit de la classe d'analyse des pages webs.
 * On va donc :
 *      1/ Récupère une url du serveur
 *      2/ Extraire les mots clés et les liens de la page
 *      3/ Envoie les résultats au serveur
 *
 * @autor Anthony, 24 mai 2005
 */


/** Importation des packages utiles   **/
import Annex. *;
import java.net.*;          // pour les objets URL et la connexion par Socket 
import java.io.*;           // pour les flux d'entrée/sortie
import java.util.*;         // pour la convertion de la date de l'url en objet Date


/** Classe CThreadURL **/
public class CRobot   {
    
    /** Variables de classe **/
    private URL             _url;           // url sur laquel on travail
    private Vector          _listeClePoid;  // liste contenant tout les mots clés (objet CClePoid) de la page
    private String          _liens[] ;      // tableau de liens
    private String          _desc="";       // description donner dans le "meta" de description
    private String          _titre="";      // titre du site (trouver entre les balises <title>)
    private int             _nbLiens=0;     // nombre de liens
    private int             _prof;          // profondeur de l'url en cour d'analyse
    private Socket            _socket;
    private ObjectInputStream _ois ;        // pour la reception d'objet (URL)
    private ObjectOutputStream _oos ;       // pour l'envoie d'objet
     
 
    /** Lancement du Robot de recherche **/
    public CRobot(Socket sock) throws IOException,UnknownHostException,ClassNotFoundException {
        _socket = sock ;
        System.out.println("\nCRobot> **** Lancement du Robot de Recherche... ***");
        System.out.println("CRobot> *** Infos Socket  :" + _socket);
       try {
        _ois = new ObjectInputStream( _socket.getInputStream() );
        _oos = new ObjectOutputStream( _socket.getOutputStream() );
       }
       catch(Exception e) { System.out.println("CRobot> EXCEPTION dans le constructeur : "+e); }
     
        while(_prof<100) {    
            _desc ="";
            _titre = "";
            _nbLiens=0;                 // nombre de liens dans le tableau _liens[]
            _liens = new String[250];   // tableau contenant tout les liens trouver dans la page (limite : 250)
            _listeClePoid = new Vector();// ArrayList contenant tout les objet CClePoid cad tout les mot clé retenu avec le poid qui leur a été associé
            recupererURL();             // méthode qui va récupérer une url dans la file d'attente du serveur
            afficherInfoURL();          // méthode d'information sur l'url
            enregistrerURL();           // parcourt les caractères de la page et la stock dans une chaine en mémoire, puis lance les traitements sur cette chaine (cad sur la page web)
            traiterCle();               // traite les mots clé trouvé dans la page (suppression des caractères spéciaux, des mots clé en double etc...) 
            etatFinal();                // envoie toute les données trouver (liens et mots clé) au serveur.
        }
        _oos.close();                   // fermeture du flux de sortie
        _ois.close();                   // fermeture du flux d'entre
        _socket.close();                // fermeture du socket
        System.out.println("\nCRobot> *** Le Robot a terminer son travail. *** ");
    }
    
    
    /** Recupération d'une url dans la file d'attente du serveur **/
    public void recupererURL() throws IOException,ClassNotFoundException {
        CURLProfondeur up =(CURLProfondeur)  _ois.readObject();   //On get une url qui est en attente sur le Moteur d'indexation
        _url = up.getUrl();
        _prof = up.getProfondeur();
        System.out.println("\n_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _\n");
        System.out.println("CRobot> 1/8 Url a analyser : "+_url.toExternalForm());
        //try {_urlCon = _url.openConnection();} catch(IOException e) { System.out.println("Erreur urlCon ="+e); }
    }
    
    /** Affiche les infos relatives à l'url **/
    public void afficherInfoURL(){
        System.out.println("CRobot> 2/8 Information sur l'url :");
        System.out.println("CRobot> \tProtocole: "+_url.getProtocol());
        System.out.println("CRobot> \tPort: "+_url.getPort());
        System.out.println("CRobot> \tMachine: "+_url.getHost());
   //     System.out.println("CRobot> \tFichier: "+_url.getFile());
   //     System.out.println("CRobot> \tComplet: "+_url.toExternalForm());
    }
    
    
    /** Stock la page web en mémoire et lance la méthode pour effectuer les traitements sur cette chaine **/
    public void enregistrerURL() {
        String chaine="" ;  // variable dans laquel on va stocker la page web
        
        /* Ouverture puis fermeture du flux provenant de la page web et se stockant dans la variable chaine */
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(_url.openStream()));
            String ligne;
            while((ligne = in.readLine()) != null) chaine+=ligne;
            in.close() ;
        } catch (Exception e) {System.out.println("CRobot> EXCEPTION, lors de l'enregistrement de l'url : "+e);};
        System.out.println("CRobot> 3/8 URL enregistrer, Recherche des liens & Mots Cle...");
        chercherURLCle(chaine); // méthode de traitement sur la page web pour trouver les mots clés et les liens
    }
    
    
    /** Méthode de traitement de la page web enfin de trouver les mots clé, leur attribuer un poid, et trouver le liens de la page **/
    public void chercherURLCle(String ch) {
        String mot="";                      // mot courant 
        String balise="";                   // nom de la balise (pour savoir si c'est la balise <script> (alors balise = "script")
        int taille = ch.length();           // taille de la chaine
        char tmp ;                          // caractère courant pour ne pas faire ch.charAt(i) plein de fois (optimisation powa)
        boolean script              = false;// pour savoir si on est entre les balises <script> et </script>
        boolean boolEntreBaliseH1   = false;// pour savoir si nous sommes entre les balises <h1> et </h1>
        boolean boolEntreBaliseTitle= false;//pour savoir si nous sommes entre les balises <title> et </title>
        boolean boolBaliseDeDeb     = false;// pour savoir si nous venos juste de passer sur le caractère "<"
        boolean boolBalise          = false;// pour savoir si on est entre <  >
        
        for(int i=0;i<taille;i++) {         // parcourt de toute la page web, caractère par caractère
            tmp=ch.charAt(i);               // récupération du caractère courant
            
            /** Gestion d'une balise **/
            if (tmp == '<'){                // si on entre dans une balise
                boolBalise=true ;           // on l'indique en passant boolBalise a true
                boolEntreBaliseH1=false;    // dès qu'on rencontre une balise ouvrante on (re)met ce booléen a false
                boolEntreBaliseTitle=false; // dès qu'on rencontre une balise ouvrante on (re)met ce booléen a false
                boolBaliseDeDeb = true;
            }
            if (tmp == '>') {               // si on rencontre une fermeture de balise
                balise+=tmp ;               // on continue de stocker ce qu'il y avait dans balise afin de connaitre le nom de la balise
                balise=balise.toLowerCase();// on passe la balise en minuscule pour etre sur d'effectuer les traitements même si on avait des balise en majuscule
                boolBalise=false;           // on va interrompre la concaténation du nom de la balise grace a ce booléen
                
                if (balise.startsWith("<script") || balise.startsWith("<style"))  // il s'agissait de la balise script ou style
                    script=true;                    // on passe le booléen a true (car on ne traite pas les mots qui sont entre ces balises)
                else if (balise.startsWith("<a ") && !script) {// il s'agissait de la balise <a..> marche pour <A oci normalement (AVERIFIER sur jeuxvideo.com)
                    traiterLien(recupererValeurAttribut("href",balise));// on lance une fonction de récupération des valeurs d'attribut de balise
                }
                else if (balise.startsWith("<h1"))      // si la balise est h1
                    boolEntreBaliseH1=true;             // on passe le booléen à true
                else if (balise.startsWith("<title"))   // si la balise est title
                    boolEntreBaliseTitle=true;          // on passe le booléen à true
                else if (balise.startsWith("<meta name=\"keywords\" "))
                     traiterKeywords(recupererValeurAttribut("content",balise));
                else if (balise.startsWith("<meta name=\"description\" "))
                     _desc = remplacementAccentsParHTML(recupererValeurAttribut("content",balise));
                else if (balise.startsWith("</script") || balise.startsWith("</style")) // si on a les balises de fin de script ou style
                    script=false;                       // on passe le booléen a true (pour reprendre "l'écoute" des mots clés
                balise="";                              // une fois les booléens mis a leur valeur selon la balise, on purge la variable
            }
            
            /** Gestion d'un mot **/
            if ((!boolBalise) && (!script)) {       // si on n'est pas dans une balise, ni un script ou un style
                if ((tmp!=' ') && (tmp!='\n') && (tmp!='\t') && (tmp!=',') && (tmp!= '/') && (tmp!='.') && (tmp!='|') && (tmp!='-') && (tmp!='>') && (tmp!='<')){ // si on a pas un caractère délimitatoire de mot
                        mot+=tmp;                   // alors on continue de concaténé les lettres du mot
                }
                else  {                             // si on rencontre un caractère délimitatoire alor le mot est fini
                    if (mot.length() > 2) {         // si le mot fait plus de deux lettres on le garde en l'ajoutant dans l'ArrayList
                        if(!motInterdit(mot)) {     // on vérifie qu'il ne fait pas parti des mots de plus de deux lettres trop courant (comme "les")
                            if (boolEntreBaliseH1)  // si c'est un mot entre les balises <h1> et </h1> alors on l'ajoute ds l'AL et on lui attribut un poid de 5
                                _listeClePoid.add(new CClePoid(suppressionEspaces(suppressionAccents(suppressionCaracteresSpeciaux(mot))),5));
                            else if (boolEntreBaliseTitle)// si c'est un mot entre les balises <title> et </title> alors on l'ajoute ds l'AL et on lui attribut un poid de 20
                                _listeClePoid.add(new CClePoid(suppressionEspaces(suppressionAccents(suppressionCaracteresSpeciaux(mot))),20));
                            else                    // si il s'agit d'un simple mot dans la page alors on l'ajoute dans l'AL avec un poid de 1
                                _listeClePoid.add(new CClePoid(suppressionEspaces(suppressionAccents(suppressionCaracteresSpeciaux(mot))),1));
                        }
                    }
                    mot="";             // on vide la variable car on repart a la recherche du mot suivant
                }
            }
            else if(boolBalise)         // si on est dans une balise cad entre "<" et ">"
                balise+=tmp ;           // alors on concatène le caractère suivant afin de construire la balise ces attribut et leurs valeurs
            if (boolEntreBaliseTitle)   // récupération du titre de la page
                _titre+=tmp;
        }
        System.out.println("CRobot> 4/8 Mot Cle et Liens trouver");
        
    }
     
    /** Méthode permettant de ne pas indexer les mots trop récurrent de plus de deux lettres **/
    public boolean motInterdit(String mot) {
        String lesInterdits[]= {"des", "les", "est", "dans" ,"that", "thus","que","pour","sur","mon","son","ces","mes","tes" }; // tableaux de mots interdits
        boolean interdit = false;                   // booléen de rencontre de mot interdit
        for(int i=0;i<lesInterdits.length;i++)      // parcourt du tableau de mots interdit
               if (lesInterdits[i].equals(mot))     // si mot est un mot appartenant au tableau
                   interdit = true ;                // on passe le booléen a true
        return interdit ;                           // on retourne le booléen
    }
    
    
    /** Récupération de la valeur d'un attribut d'une balise html **/
    public String recupererValeurAttribut(String attribut,String balise) { // ex  : ("href",balise)
        String valeur="";
        int tatt= attribut.length();
        int tbal= balise.length();
        int deb=0;
        int fin;
        boolean guillemet=true;
        while(!balise.substring(deb,deb+tatt).equals(attribut) && (deb+tatt<tbal))
            deb++;
        
        if ((deb+tatt) < tbal) {
        /* ici il faut savoir si il y a un " ou pas pour savoir si c deb+tatt+1 ou deb+tatt+2 */
            if (balise.charAt(deb+tatt+1)!='\"') {
                deb=deb+tatt+1;
                guillemet=false;
            }
            else deb=deb+tatt + 2 ;
            fin=deb;
            /* ici prendre en compte que s'il n'y a pas de " il faut s'arret tant ke on a pas " " ou ">" */
            if(guillemet) {
                while(fin<balise.length() && balise.charAt(fin) != '\"' )
                    fin++; 
            }
            else {
                while(((balise.charAt(fin) != '>') & (balise.charAt(fin)!=' ')) & (fin < tbal))
                    fin++;
            }
            valeur = balise.substring(deb,fin);   // on a le contenu de l'attribut dans la variable chaine
        }
        return valeur;
    }
    
    
    /** Traitements des liens trouver dans la page, vérifie leur validité et reconstruit les liens relatifs **/
    public void traiterLien(String lien) {
        boolean lienValide  = false;                // on vérifie si le lien est valide
        boolean lienRelatif = false ;               // ou s'il est relatif
        boolean lienAbsolu  = false;                // ou absolu
        String url = _url.toExternalForm();         // on récupere le l'url de la page sur laquel on se trouve pour reconstruire les liens relatif
        String [] tExt = { ".html" , ".php" , "/" , ".com" , ".net" , ".org" , ".st" , ".fr" , ".de" , "htm" ,".ca" , ".be" ,"." };
        for (int i=0 ; i< tExt.length ; i++)        // parcourt du tableau d'extension concidéré comme valide
           if (lien.endsWith(tExt[i])) lienValide = true;
        if(lien.startsWith("/"))        // si le lien commence par un "/" il s'agit d'un lien relatif
        {lienRelatif = true;
        }
       
        if(lien.startsWith("http://"))  // si le lien commence par http:// il s'agit d'un lien absolu
            lienAbsolu = true;
        if(lien.indexOf("mailto")!=-1)    // si le lien renvoie vers une adresse mail
            lienValide = false;
        if((lienRelatif && !lienValide))// s'il est relatif mais pas valide alors on met a false
            lienRelatif = false;
        if(!lienAbsolu && lienRelatif && lienValide){// reconstruction des liens relatifs
            int i = url.length()-1;     // exemple : si l'url courante est le liens "http://www.fds.fr/index.html" et qu'on a href="index3.html" alors on va construire "http://www.fds.fr/index3.html")   
            while(url.charAt(i)!='/' && i>-1)             
                i--;
            if (url.charAt(i-1) != '/')
                url = url.substring(0,i) + "/";
        }
        if(lienValide)
            lienRelatif = true;
        
        if(lien.endsWith(".xml")) {
            lienRelatif = false;
            lienAbsolu = false;
        }
        if (lienAbsolu && _nbLiens<_liens.length) { // si c'est un lien absolu on ajoute sans problème ds la liste
            _liens[_nbLiens++]=lien;
       }
        else if (lienRelatif && !lienAbsolu  && _nbLiens<_liens.length) {
          _liens[_nbLiens++]= url + lien ;
      }
    }
   
    
    /** Méthode utilisant les StringTokenizer pour extraire les mots du "content" de la balise <meta name="keyword"> **/
    public void traiterKeywords(String keys) {
        String cle="";
        StringTokenizer st = new StringTokenizer(keys,", "); //la virgule et l'espace sont les délimitateurs de mots
        while(st.hasMoreTokens()){
            cle = st.nextToken();
            _listeClePoid.add(new CClePoid(suppressionEspaces(suppressionAccents(suppressionCaracteresSpeciaux(cle))),10)); // on affecte un poid de 10 aux mots trouvés dans keywords
         }
    }    
    
    /** Méthode traitant les caractères spéciaux à l'intériéur des chaines du titre et/ou de la description **/
    public String suppressionCaracteresSpeciaux(String ch){
        String caracSpe="";         // caractère spéciale trouver dans la chaine
        String[][] tConv = {{"&nbsp;"," "},{"&amp;"," "},{"&hellip;"," "},{"&gt;"," "},{"&lt;"," "},{"&quot;"," "}, {"&copy;"," "} , {"&Ecirc;","E"}, {"&Euml;","E"}, {"&Egrave;","E"} , {"&Ocirc;","O"} , {"&Ugrave;","U"} , {"&ugrave;","ù"}, {"&ucirc;","û"} , {"&Agrave;","A"} ,{"&Acirc;","A"} , {"&agrave;","à"} , {"&auml;","ä"},{"&acirc;","â"} , {"&egrave;","è"} , {"&eacute;","é"} , {"&ecirc;","ê"} , {"&euml;","ë"}, {"&icirc;","î"}, {"&iuml;","ï"}, {"&ocirc;","ô"},{"&ouml;","ö"}, {"&otilde;","õ"},{"&#339;","oe"},{"&ccedil;","ç"},{"&ntilde;","ñ"}, {"&#8217;","'"}, {"&#160;"," "}, {"&#187;"," "} } ;
        boolean concat=false;       // si on a vu un '&' dans le mot alors on lance la concaténation de caracSpe.
        boolean effectuer=false;    // si on a effectuer un remplacement
        int indDeb=0;               // indice du '&' dans le mot
        int indFin=0;               // indice du ';' dans le mot
        int j=0;
        do {
            if (j<ch.length() && ch.charAt(j)=='&') {
                caracSpe += ch.charAt(j);   // création du caractère spéciaux dès qu'on voit un '&'
                indDeb=j;                   // on retien l'indice sur lequel on avait le '&'
                concat=true;
            }
            else if (concat) {
                caracSpe += ch.charAt(j);   // on entamme la concaténation jusqu'à ce qu'on trouve un ';'
                if (ch.charAt(j)==';'){     // dès qu'on tombe sur le ';' on arrête de concaténé notre caracSpe
                    concat=false;
                    indFin=j;               // on retient l'indice ou il y a le ';'
                    if (caracSpe!=""){      // on vérifie si on a trouver un caractère spéciale
                        for (j=0; j<tConv.length ; j++){            // renvoi la taille du premier indice
                             if (caracSpe.equals(tConv[j][0])) {    // si le caractere spéciale trouver fait parti du tableau de correspondance
                                ch = ch.substring(0,indDeb)+tConv[j][1]+ch.substring(indFin+1,ch.length());
                                effectuer=true;
                            }
                        }
                    }
                    if(effectuer){  // si on a effectué un remplacement dans la chaine
                        caracSpe="";// on reinitialise les variables ici car on revérifie le meme mot afin de voir s'il n'as pas un deuxieme caractere spéciale
                        j=0;        // on va donc recommencer la vérification du mot a partir de son debut
                        effectuer=false;
                    }
                }
            }
            j++;
        }
        while (j<ch.length());
        return ch;
    }
    
    
    /** suppression des accents et caractères non-mots **/
    public String suppressionAccents(String ch) {
        char t[][] =       {{'é','e'},{'è','e'},{'ê','e'},{'ô','o'},{'ö','o'},{'õ','o'},{'û','u'},{'ü','u'},{'ñ','n'},{'ç','c'},{'à','a'},{'â','a'},{'ä','a'},{'ï','i'},{'î','i'}};
        char tEspace[][] = {{'[',' '},{']',' '},{'"',' '},{'!',' '},{'(',' '},{'$',' '},{'*',' '},{'+',' '},{')',' '},{'}',' '},{'{',' '},{':',' '},{'|',' '},{'§',' '},{'?',' '}};
        for(int i=0; i<_listeClePoid.size(); i++) {
            for (int j=0 ; j<t.length ; j++)
               for (int k=0 ; k<ch.length(); k++) {
                   if (t[j][0]==ch.charAt(k))
                       ch=ch.replace(ch.charAt(k),t[j][1]);
                   else if (tEspace[j][0]==ch.charAt(k))                  // attenion: tEspace et t doivent etre des tableaux de même taille !
                       ch=ch.substring(0,k)+ch.substring(k+1,ch.length());//on ne remplace pas par ' ' mais on supprime l'espace !
               }
        }
        return ch;
    }
    
    
    /** Méthode permettant de remplacé les 'é' 'è' etc... par leur equivalent en html **/
    public String remplacementAccentsParHTML(String ch) {
        String[][] tConv = {{"&ugrave;","ù"}, {"&ucirc;","û"} , {"&agrave;","à"} , {"&auml;","ä"},{"&acirc;","â"} , {"&egrave;","è"} , {"&eacute;","é"} , {"&ecirc;","ê"} , {"&euml;","ë"}, {"&icirc;","î"}, {"&iuml;","ï"}, {"&ocirc;","ô"},{"&ouml;","ö"}, {"&otilde;","õ"},{"&ccedil;","ç"},{"&ntilde;","ñ"}, {"&#8217;","'"} } ;
        for (int j=0 ; j<tConv.length ; j++){
            for (int k=0 ; k< ch.length() ; k++)
               if (tConv[j][1].equals(String.valueOf(ch.charAt(k))))
                   ch=ch.substring(0,k)+tConv[j][0]+ch.substring(k+1,ch.length());
        }
        return ch;
    }
    
    
    /** Suppression des espaces à l'intérieur des mots**/
    public String suppressionEspaces(String ch) {
        for(int i=0; i<_listeClePoid.size(); i++) {
            int k=0;
            do {
               if(k<ch.length() && k>=0) {
                    if (ch.charAt(k)==' '){
                        ch=ch.substring(0,k)+ch.substring(k+1,ch.length());
                        k--;
                    }
                }
               k++;
            }
            while(k<ch.length());
        }
        return ch;
    }
   
    
    /** Traitements des mots clés trouvés dans la page :
     *      - mise en minuscule
     *      - suppression des doublons (et modification leur poid en conséquence)
     *      - suppression des mots de moins de 3 lettres 
     **/
    public void traiterCle(){
        if(_listeClePoid.size()>=2){     // il faut obligatoirement qu'il y ai au moins deux mots pour verifier s'il y a des doublons !... 
            int i=0;
            do{
                int cpt=0;
                CClePoid cp1 = (CClePoid)_listeClePoid.get(i);
                String str = cp1.getCle().toLowerCase();
                cp1.setCle(str);
                String mot1 = cp1.getCle().toLowerCase();
                int j=0;
                do {
                    CClePoid cp2 = (CClePoid)_listeClePoid.get(j);
                    cp2.setCle((cp2.getCle()).toLowerCase());
                    String mot2 = cp2.getCle();
                    if (mot1.equals(mot2)) {
                        cpt++;                      // on a le nombre de foi que le mot est apparut (nb est ds cpt) sert pas grand chose... ca devait servir pour le systeme de poid mais en fait nan...
                        if (cpt>1) {
                            
                            cp1.setPoid(cp1.getPoid() + cp2.getPoid());   // on recupere son poid, on l'ajoute a son copain doublons
                            _listeClePoid.remove(j);// on supprime le doublon
                            j--;
                        }
                    }
                    j++;
                }
                while (j<_listeClePoid.size());
                i++;
            }
            while (i<_listeClePoid.size());
        }
        int i=0;
        while(i<_listeClePoid.size()) {             // suppression des nouveaux mots de moins de 3 lettres
            CClePoid cp1 = (CClePoid)_listeClePoid.get(i);
            String mot1 = cp1.getCle();
            if (cp1.getCle().length()<3) { 
                _listeClePoid.remove(i);
                i--;
            }
            i++;
        }
       
    }
    
    /** Quand on a fini les traitements sur la page et donc qu'on dispose
     *      -D'un tableau de liens (qui ont été recupéré dans la page)
     *      -Une liste de mots clés traités (les mots clés de la page sont prêt à être ajoutés dans l'index)
     * Alors on va ajouter les mots clé dans l'hashtable de l'index et la liste d'url dans la file d'attente du serveur
     **/
    public void etatFinal()  throws IOException , UnknownHostException{   
      /* Affichage */
      System.out.println("CRobot> 5/8 Nombre de cle a envoyer au Serveur ajouter : "+_listeClePoid.size());
      System.out.println("CRobot> 6/8 Nombre de Liens a envoyer au Serveur : "+_nbLiens);
      System.out.println("CRobot> 7/8 Envoie des objets...");
       
      String url = _url.toExternalForm();               // le nom de l'url en cours
      _titre= remplacementAccentsParHTML(_titre);       // on transforme les 'é' 'è' en caractère spéciaux html car il seront interprété par le client flash
        
       trierListeClePoid();                             // tri alphabetique des cle pour un ajout + rapide dans l'index
      /* Envoie des objets */
      _oos.writeObject(_listeClePoid);                  // envoie de la liste de Cle/Poid
      _oos.writeObject(_liens);                         // ...
      _oos.writeObject(url);
      _oos.writeObject(_titre);
      _oos.writeObject(_desc);
      _oos.writeObject(new Integer(_nbLiens));
      _oos.writeObject(new Integer(_prof));                 //... Fin de l'envoie des objets au Serveur
      
      System.out.println("CRobot> 8/8 Envoie reussit.");
    }

    
    /** Tri par ordre alphabetique de la liste de cle/poid (pour un ajout + rapide ds l'index) **/
    public void trierListeClePoid() {
        boolean fin=false;
        while(!fin) {
            fin=true;
            for(int i=0; i<_listeClePoid.size()-1 ; i++) {
                CClePoid cp1 =(CClePoid) _listeClePoid.get(i);
                CClePoid cp2 =(CClePoid) _listeClePoid.get(i+1);
                if(cp1.getCle().compareTo(cp2.getCle()) > 0) {
                    _listeClePoid.setElementAt(cp2,i);
                    _listeClePoid.setElementAt(cp1,i+1);
                    fin=false;
                }
            }
        }
    }
    
    /*******************************************************************/
    /*******************  Fonction PRINCIPALE **************************/
    /*******************************************************************/
    
    public static void main(String args[]) throws UnknownHostException,IOException,ClassNotFoundException {
               
       System.out.println("\nCRobot>             GOUGOULE 2K5 : Moteur De Recherche ");
       System.out.println("CRobot> ________________________________________________");
       System.out.println("CRobot>                                  Anthony & Benoit\n");
       
       String adresse = "localhost";
       int portRobot = 18010;
       
        /* Configuration des parametres du Robot */
       System.out.println("CRobot> Configuration des parametres du Robot :");
       int chx;
       do {
           System.out.println("CRobot> \t-> 1 - Configuration par defaut ");
           System.out.println("CRobot> \t-> 2 - Configuration personnalisee ");
           chx = Lire.i();
           if(chx < 1 || chx > 2)
               System.out.println("CRobot> Ce choix n'existe pas.");
       }
       while( chx<1 || chx>2 );
       if (chx == 1) {
       System.out.println("CRobot> <---------------- Configuration choisit ---------------->\n");
       System.out.println("CRobot> Adresse du Serveur . . . . . . . . . . . . . . "+adresse);;
       System.out.println("CRobot> Port de connexion au Serveur . . . . . . . . . "+portRobot);
       }
       if (chx == 2) { // configuration personnalisee
           System.out.print("CRobot> Donnez l'adresse du Serveur : ");
           adresse=Lire.S();
           System.out.print("CRobot> Donnez le port de connexon au Serveur : ");
           portRobot=Lire.i();
           
           System.out.println("\nCRobot> <---------------- Configuration choisit ---------------->");
           System.out.println("CRobot> Adresse du Serveur . . . . . . . . . . . . . . "+adresse);;
           System.out.println("CRobot> Port de connexion au Serveur . . . . . . . . . "+portRobot);
       }
        try {
         Socket socket = new Socket(adresse,portRobot); // param 1: ip ; param 2:port
         CRobot robot = new CRobot(socket);
       }
       catch(Exception e) { System.out.println("CRobot> Le temp qui m'etait alloue est ecoule");}    
    }
}
