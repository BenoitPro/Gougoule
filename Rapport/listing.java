/***** Classe CIndex *****
 * CIndex est la hashTable de cl�/valeur contenant toutes les urls en fct de chaque mot cl�:
 *      -On peut ajouter dans l'index
 *      -On peut r�cup�rer la "valeur" d'une cl� (ici il s'agit d'une ArrayList d'objet CURLPoid)
 *
 * @autor Benoit, 18 avril 2005
 *************************************/

/** Importation de package utiles � la classe **/
import Annex.* ;
import java.net.*;      // pour l'ouverture du port 
import java.util.*;     // pour la table de hachage
import java.io.*;       // pour la s�rialisation


/** Classe CIndex **/
public class CIndex implements Serializable{
    
    private Hashtable _hashCourante;
    
    /** Constructeur **/
    public CIndex() {
        boolean initialiser=false;
        _hashCourante = new Hashtable();
        try {
            FileInputStream fis = new FileInputStream("speciaux");
        }
        catch(FileNotFoundException e){
            initialiser=true;                       // l'exception est lever donc lengthfichier sp�ciaux n'existe pas
        }
        if (initialiser){
            
            FileOutputStream fos;                   // Fichier de sortie
            ObjectOutputStream oos;                 // Flux de sortie
            
            /* Cr�ation de la hastable contenant les titre/description selon une url donn� */
            try {
                Hashtable _indexUrl = new Hashtable();  
                fos= new FileOutputStream("hashUrl");;
                oos = new ObjectOutputStream(fos);
                oos.writeObject(_indexUrl);
            }
            catch (Exception e) { }
            /** cr�ation des 27 Hastables (une pour chaque lettre de l'alphabet + une pour les caractere sp�ciaux)**/
            for (int i=0; i<=26 ; i++) {
               Hashtable hash = new Hashtable();
                try {
                 if (i==26)
                    fos = new FileOutputStream("speciaux");
                else 
                    fos = new FileOutputStream(String.valueOf((char)(97+i)));
                 
                oos = new ObjectOutputStream(fos);
                oos.writeObject(hash);
                oos.flush();
                oos.close();
                }
                catch(Exception e) {
                  System.out.println("CIndex> EXCEPTION, de s�rialisation lors de l'initialisation des fichiers : "+e);
                }
            }
        }
    }
    
      
    /** Ajoute une cl� avec sa valeur dans la table **/
    public synchronized void ajouter(String url,Vector listeClePoid) throws Exception {
        System.out.println("CIndex> Ajout de la liste de mot cles");
        /* Initialisation des variables */
        int i=0;                                        // initialisation de i
        char lettre;                                    // d�claration de la lettre courante
        CClePoid cp = (CClePoid) listeClePoid.get(0);   // initialisation de l'ovjet ClePoid courant
        String cle = cp.getCle();                       // r�cup�ration de la cl� de l'objet Cle/Poid courant
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("speciaux")); //** initialisation bidon a cause du compilateur "might not be initialised"
        /* Parcourt de toute la liste de ClePoid */
        while(i<listeClePoid.size()-1) {
            lettre = cle.charAt(0);                    // r�cup�ration de la nouvlle lettre courante
            _hashCourante=new Hashtable();          // Cr�ation d'une hashtable en m�moire
            
            /* R�cup�ration (en m�moire) du fichier correspondant a la lettre courante */
            try {
                FileInputStream fis;
                if ((int)lettre<97 || (int)lettre>122)                  // si la lettre est un caractere sp�cial...
                    fis = new FileInputStream("speciaux");              // ... on ouvre le fichier des caracteres sp�ciaux
                else                                                    // sinon...
                    fis = new FileInputStream(String.valueOf(lettre));  // ...on ouvre le fichier correspondant a la lettre courante
                ois = new ObjectInputStream(fis);                       // on cr�e lengthflux de fichier associer au fichier d'entr�
                _hashCourante = (Hashtable) ois.readObject();                 // on r�cup�re la hashtable de la bonne lettre ds la variable hashDes
             }
            catch(Exception e) {
                System.out.println("CIndex> EXCEPTION, pendant la d�s�rialisation de l'ajout d'un mot cl� : "+e);
                System.exit(0);
            }
            
            /* Conversion de l'objet CClePoid courant en objet CURLPoid puis ajout dans la hashtable courante (coresspondant a la lettre courante) */
            while( (cle.charAt(0) == lettre) && (i<listeClePoid.size()-1) ) {     // Parcourt de tous les mots cl�s commencant par la meme lettre
                Vector vt = rechercherCle(cle);
                if (vt == null) {                                  // si le mot cle n'est pas dans l'index, on creer une nouvelle "case" ds la hashtable; cad le nv mot cle
                    vt = new Vector();                                   // on cree le vector qui contiendra "url" "poid"
                    vt.addElement(new CURLPoid(url,cp.getPoid()));              // on ins�re le 1er lien et son poid au nouveau mot cl�
                    _hashCourante.put(cle,vt);                                        // dans l'index on Hash selon "motcle" et on ajoute le Vector
                 }
                else {  
                    // si on a d�j� ce mot cl� dans l'index on va seulement ajouter l'url dans "valeur" associ� au mot cl�
                    vt = rechercherCle(cle);
                    vt.addElement(new CURLPoid(url,cp.getPoid()));
                    _hashCourante.put(cle,vt);
                }
                i++;
                cp  = (CClePoid)listeClePoid.get(i);                             // on r�cupere le nouveau objet CClePoid courant
                cle = cp.getCle();                                               // on r�cupere la nouvelle cl� courante
            } // fin while
            try {
                ois.close();
            }
            catch(IOException e) { System.out.println("CIndex> ERREUR" +e); }
            /** (Re) S�rialisation **/
            ObjectOutputStream oos;
            try {
                if ((int)lettre <97 || (int)lettre>122){
                    FileOutputStream fos = new FileOutputStream("speciaux");
                    oos = new ObjectOutputStream(fos);
                    oos.writeObject(_hashCourante);
                }
                else {
                    FileOutputStream fos = new FileOutputStream(String.valueOf(lettre));
                    oos = new ObjectOutputStream(fos);
                    oos.writeObject(_hashCourante);
                }
                oos.flush();
                oos.close();
            }
            catch(Exception e) {
                System.out.println("CIndex> EXCEPTION, pendant la serialisation de l'ajout d'une cl� dans l'index : "+e);
                System.exit(0);
            }       //fin catch
        }       // fin while
        System.out.println("CIndex> Mise des mots cle dans l'index -> reussit");
    }
  
    
    /** Recherche la valeur correspondant a la cl� donn� en param�tre dans la hashtable courante **/
    public synchronized Vector rechercherCle(String cle) {
        Vector vt = new Vector();
        vt = (Vector) _hashCourante.get(cle);
        return vt;

    }
    
    /** Recherche la valeur correspondant a la cl� donn� en param�tre **/
    public synchronized Vector rechercher(String cle) {
        char lettre = cle.charAt(0);
        Vector vt = new Vector();
        Hashtable hashDes=new Hashtable();
        
        /** D�s�rialisation **/
        try {
            FileInputStream fis;
            if ((int)lettre <97 || (int)lettre>122){
                fis = new FileInputStream("speciaux");
            }
            else {
                fis = new FileInputStream(String.valueOf(lettre));
            }
            ObjectInputStream ois = new ObjectInputStream(fis);
            hashDes = (Hashtable) ois.readObject();
            vt = (Vector) hashDes.get(cle);
            ois.close();
        }
        catch(Exception e) {
            System.out.println("CIndex> EXCEPTION, pendant la d�s�rialisation de la recherche de mot cle dans l'index : "+e);
            System.exit(0);
        }
        return vt;
         
    }
    
   /** Ajoute une cl� (String correspondant a l'url) et sa valeur (titre et description correspondant) dans la Hashtable **/
    public synchronized void ajouterUrl(String cle,CTitreDesc valeur) {
        Hashtable hashDes=new Hashtable();
        
        /** On d�s�rialise(chargement) la Hashtable correspondant a la 1er lettre du mot cl� en restaurant le fichier correspondant. Celui-ci ira donc en m�moire **/
        try {
            
            FileInputStream fis;
            fis = new FileInputStream("hashUrl");
            ObjectInputStream ois = new ObjectInputStream(fis);
            hashDes = (Hashtable) ois.readObject(); // on a r�cup�r� la hashtable de la bonne lettre ds la variable obj
            hashDes.put(cle,valeur);                // on a effectuer l'ajout on pourra donc s�rialiser l'objet
            ois.close();
        }
        catch(Exception e) {
            System.out.println("CIndex> EXCEPTION, pendant la d�s�rialisation de ajouterUrl : "+e);
            System.exit(0);
        }
        
        /** (Re) S�rialisation ***/
        try {
            FileOutputStream fos = new FileOutputStream("hashUrl");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(hashDes);
            oos.flush();
            oos.close();
        }
        catch(Exception e) {
            System.out.println("CIndex> EXCEPTION, pendant la serialisation de ajouterUrl : "+e);
            System.exit(0);
        }
    }
    
    
    /** Recherche la valeur � partir de plusieurs cl� **/
    public synchronized CTitreDesc rechercherUrl(String cle) {
        CTitreDesc ctd = new CTitreDesc("","");
        Hashtable hashDes=new Hashtable();
        
        /** D�s�rialisation **/
        try {
            FileInputStream fis;
            fis = new FileInputStream("hashUrl");
            ObjectInputStream ois = new ObjectInputStream(fis);
            hashDes = (Hashtable) ois.readObject();
            ctd = (CTitreDesc) hashDes.get(cle);
            ois.close();
        }
        catch(Exception e) {
            System.out.println("CIndex> EXCEPTION, pendant la d�s�rialisation de rechercherUrl: "+e);
            System.exit(0);
        }
        
        /** S�rialisation  **/
        try {
            FileOutputStream fos = new FileOutputStream("hashUrl");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(hashDes);
            oos.flush();
            oos.close();
        }
        catch(Exception e) {
            System.out.println("CIndex> Exception pendant la serialisation de rechercherUrl : "+e);
            System.exit(0);
        }
        return ctd;
    }
    
}


/***** Classe CMoteur*****
 * Cette classe est notre moteur d'indexation :
 *      -Il g�re la file d'attente d'url (ajout et retrait)
 *      -Il poss�de une r�f�rence vers l'index afin d'y effectuer des recherches
 *
 * @autor Benoit, 15 avril 2005
 *************************************/

/** Importation des packages utiles � la classe **/
import Annex.* ;
import java.util.*;
import java.io.*;
import java.net.*;


/** La classe CMoteur **/
public class CMoteur {
    
    
    /** Variables de classe **/
    private ArrayList   _laFile;             // file d'attente d'URL � index�
    private CIndex      _index;              // r�f�rence vers l'index (cf. classe CIndex)
    private Vector      _URLVisiter;         // liste des urls d�ja visit�es
    private int         _nbLiensVisiter;     // taille du Vector URLVisiter, sera transmis au CThreadClient afin qu'il le transmette au client Flash
    
    
    /** Constructeur **/
    public CMoteur(CIndex ind) {
        this._index = ind ;                 // on d�finie la r�f�rence vers l'index
        _laFile     = new ArrayList();      // on cr�e la file d'attente
        _URLVisiter = new Vector();         // on cr�e le Vector d'URLVisiter (Vector et non ArrayList car il est s�rialisable)
        
        boolean initialiser = false;
        try { FileInputStream fis = new FileInputStream("hashUrlVisiter"); }    // on v�rifie si le fichier existe
        catch(Exception e){ initialiser=true; }                                 // si il existe pas on a une exception r�cup�rer par le catch, il va donc falloir initialiser
        if (initialiser){                                                       // donc si le fichier hashUrlVisiter n'existe pas...
            FileOutputStream fos;                                               // ... on va le cr�er
            try {
                fos = new FileOutputStream("hashUrlVisiter");
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(_URLVisiter);
                oos.flush();
                oos.close();
            } catch(Exception e) {
                System.out.println("CMoteur> EXCEPTION, de s�rialisation lors de l'initialisation d'URLVisiter : "+e);
            }
        } else {                                                                  // s'il existe d�ja on le d�s�rialise (et donc r�cup�r� le Vector URLVisiter)
            try {
                FileInputStream fis;
                fis = new FileInputStream("hashUrlVisiter");
                ObjectInputStream ois = new ObjectInputStream(fis);
                _URLVisiter = (Vector) ois.readObject();
                ois.close();
            } catch(Exception e) {
                System.out.println("CMoteur> EXCEPTION, pendant la d�s�rialisation d'URLVisiter : "+e);
                System.exit(0);
            }
            
            _nbLiensVisiter = _URLVisiter.size();
            /* (C) Affichage de la liste des URLVisiter */
            ///*
            System.out.println("\nCMoteur>  Affichage de la liste des URLVisiter :");
            for (int i=0; i<_URLVisiter.size() ; i++) {
                System.out.println("CMoteur> "+_URLVisiter.get(i));
            }
            System.out.println();
            //*/
        }
    }
    
    
    /** Ajoute une cl� avec sa valeur (Vector d'objet CURLPoid) dans l'index) **/
    public synchronized void ajouterIndex(String url,Vector listeClePoid) throws Exception {
        _index.ajouter(url,listeClePoid);
    }
    
    
    /** Recherche une cl� dans l'index et retourne sa "valeur" (donc ici un Vector d'objets CURLPoid)**/
    public synchronized Vector rechercherIndex(String cle) {
        return (Vector) _index.rechercher(cle);
    }
    
    
    /** Ajout d'une nouvelle URL dans la file  **/
    public synchronized void ajouterURL(CURLProfondeur up) {
        int i=0;
        boolean ajout=true;
        while(i<_URLVisiter.size()){
            if(up.getUrl().toExternalForm().equals(((URL)_URLVisiter.get(i)).toExternalForm())) // si il s'agit d'un liens deja visiter
                ajout=false;
            i++;
        }
        if (ajout)
            _laFile.add(up);        // on ajoute dans la file la nouvelle URLProfondeur
        if(_laFile.size()==1)       // si avant l'ajout il n'y avait pas d'url dans la file
            notify();               // on d�bloque les threads en attente d'une url
    }
    
    /** Ajout d'une liste d'URL dans la file **/
    public synchronized void ajouterUrl(String [] liens,int nbLiens,int prof) {
        
        for (int i=0; i<nbLiens ; i++){
            try {
                URL nvURL = new URL(liens[i]) ;
                CURLProfondeur up = new CURLProfondeur(nvURL,prof+1);
                int j=0;
                boolean ajout=true;
                while(j<_URLVisiter.size()){
                    if(up.getUrl().toExternalForm().equals(((URL)_URLVisiter.get(j)).toExternalForm())) // si il s'agit d'un liens deja visiter
                        ajout=false;
                    j++;
                }
                if (ajout)
                    _laFile.add(up);    // on ajoute dans la file la nouvelle URLProfondeur
                if(_laFile.size()==1)   // si avant l'ajout il n'y avait pas d'url dans la file
                    notify();           // on d�bloque les threads en attente d'une url
            } catch (MalformedURLException e) { System.out.println("CMoteur> EXCEPTION, lors de l'ajout des url dans la file d'attente : "+e);}
        }
    }
    
    
    /** Ajoute une cl� avec sa valeur (ArrayList d'objet CTitreDesc) dans l'index **/
    public synchronized void ajouterIndexUrl(String cle,String titre,String desc) {
        if (titre.length()>0)
            titre=titre.substring(1,titre.length());
        int ind;
        if ((ind=desc.indexOf("<")) !=-1 || (ind=desc.indexOf("&gt;"))!=-1)
            desc=desc.substring(0,ind);
        _index.ajouterUrl(cle,new CTitreDesc(titre,desc));
    }
    
    
    /** Recherche une cl� dans l'indexUrl et retourne sa "valeur" (donc ici une ArrayList d'objets CTitreDesc) **/
    public synchronized CTitreDesc rechercherIndexUrl(String cle) {
        return (CTitreDesc) _index.rechercherUrl(cle);
    }
    
    
    /** Donne une URL (m�thode appel� par les CThreadURL)**/
    public synchronized CURLProfondeur avoirURL() {
        CURLProfondeur up ;
        int i=0;
        boolean donner=false;
        if (_laFile.size()==0) {                    // s'il n'y a pas d'url dans la file...
            try {
                System.out.println("\nCMoteur> Plus aucuns liens dans la file d'attente -> Thread arreter ");
                wait();                             // ...on fait attendre les threads
            } catch(InterruptedException e) { System.out.println("CMoteur> EXCEPTION, le serveur a donnee une url alor que la file d'attente est vide : "+e); }
        }
        up = (CURLProfondeur) _laFile.get(0) ;      // prend la premiere url de la file
        _URLVisiter.add(up.getUrl());               // on ajoute dans le Vector des urls d�j� visit�es l'url qui viens d'�tre prise
        _nbLiensVisiter = _URLVisiter.size();       // total du nombre de liens visit� par notre moteur (variable transmis au client Flash par l'interm�diaire du CThreadClient)
        _laFile.remove(0);                          // on la supprime de la file (pour que les autres threads ne l'as visite pas par exemple)
        
        /* On s�rialise le Vector d'URLVisiter */
        try {
            FileOutputStream fos = new FileOutputStream("hashUrlVisiter");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(_URLVisiter);
            oos.flush();
            oos.close();
        } catch(Exception e) {
            System.out.println("CMoteur> EXCEPTION, pendant la s�rialisation d'URLVisiter : "+e);
            System.exit(0);
        }
        return up ;             // on retourne l'url
    }
    
    
    /** Getteur du nombre de liens Visiter **/
    public int getNbLiensVisiter(){
        return _nbLiensVisiter;
    }
}

/****** Classe CServeur ******
 * Il s'agit de la classe principale puisqu'elle poss�de le main.
 * Dans cette classe on va donc :
 *      1/ Cr�e l'index
 *      2/ Cr�e le moteur d'indexation (avec r�f�rence sur l'index)
 *      3/ Initialise la file d'attente d'url du moteur
 *      4/ Lance les threads d'�coute
 *
 * @autor Benoit, 24 mai 2005
 ****************************/


/****************************************************************************************************************/
/***************************************  Classe PRINCIPALE  *******************************************/
/****************************************************************************************************************/

/* Importation des packages*/
import Annex.* ;
import java.net.*;

public class CServeur {
    
    public static void main(String[] args) {
        System.out.println("\nCServeur>             GOUGOULE 2K5 : Moteur De Recherche ");
        System.out.println("CServeur> ________________________________________________");
        System.out.println("CServeur>                                  Anthony & Benoit\n");
        
        /* 1 - Initialisation des variables */
        int portApplet=18001;
        int portRobot=18010;
        int profArret=1;
        int temps=45;  // temp en secondes
        String amorce ;
        
        /* 2 - Configuration des paramet0res du Serveur */
        System.out.println("CServeur> Configuration des parametres du Serveur :");
        int chx;
        do {
            System.out.println("CServeur> \t-> 1 - Configuration par defaut ");
            System.out.println("CServeur> \t-> 2 - Configuration personnalisee ");
            chx = Lire.i();
            if(chx < 1 || chx > 2)
                System.out.println("CServeur> Ce choix n'existe pas.");
        }
        while( chx<1 || chx>2 );
        if (chx == 1) {
            System.out.println("CServeur> <---------------- Configuration choisit ---------------->\n");
            System.out.println("CServeur> Port d'ecoute des Interfaces Clientes . . . . "+portApplet);
            System.out.println("CServeur> Port d'ecoute des Robots de recherche . . . . "+portRobot);
            System.out.println("CServeur> Profondeur d'arret  . . . . . . . . . . . . . "+profArret);
            System.out.println("CServeur> Temp Allouer au Robot . . . . . . . . . . . . "+temps+" sec.");
        }
        if (chx == 2) { // configuration personnalisee
            System.out.print("CServeur> Donnez le port d'ecoute des Interfaces Clientes : ");
            portApplet=Lire.i();
            System.out.print("CServeur> Donnez le port d'ecoute des Robot de recherche : ");
            portRobot=Lire.i();
            System.out.print("CServeur> Donnez la Pronfondeur d'Arret (0 pour un seul niveau) : ");
            profArret = Lire.i();
            System.out.print("CServeur> Donnez le Temp Alloue au Robot de recherche (en sec.) : ");
            temps=Lire.i();
            
            
            System.out.println("CServeur> <---------------- Configuration choisit ---------------->\n");
            System.out.println("CServeur> Port d'ecoute Interfaces Clientes . . . .  "+portApplet);
            System.out.println("CServeur> Port d'ecoute des Robots de recherche . .  "+portRobot);
            System.out.println("CServeur> Profondeur d'arret  . . . . . . . . . . .  "+profArret);
            System.out.println("CServeur> Temp Allouer au Robot . . . . . . . . . .  "+temps+" sec.");
        }
        System.out.println("\nCServeur> Donnez un mot d'amorce (tapez 0 pour passe cette etape): ");
        amorce=Lire.S();
        
        /* 3 - Cr�ation de l'index et du moteur d'indexation */
        CIndex index   = new CIndex() ;            // Cr�ation de l'index ( CIndex )
        CMoteur Moteur = new CMoteur(index);       // Cr�ation du moteur d'indexation avec l'index en r�f�rence
        
        
        /* 4 - Ajout d'urls dans la file d'attente */
        if(!amorce.equals("0")) { // si l'utilisateur veut amorcer la file d'attente avec yahoo.fr
            System.out.println("CServeur> Lancement de l'amorcage de la file d'attente...\n");
            new CThreadAmorce(amorce,Moteur);
        }
        else {
        URL nvURL ;
        System.out.println("CServeur> Ajout de 3 urls predefinies dans la file : ");
        System.out.println("CServeur> \thttp://www.u-bourgogne.fr");
        System.out.println("CServeur> \thttp://www.clubic.com");
        System.out.println("CServeur> \thttp://www.vocanson.com");
        
        try {
            nvURL = new URL("http://www.u-bourgogne.fr/");
            Moteur.ajouterURL(new CURLProfondeur(nvURL,0));
            
            nvURL = new URL("http://www.clubic.com");
            Moteur.ajouterURL(new CURLProfondeur(nvURL,0));
            
            nvURL = new URL("http://www.vocanson.com");
            Moteur.ajouterURL(new CURLProfondeur(nvURL,0));
            } catch( MalformedURLException e) { System.out.println("CServeur> EXCEPION : "+e);}
        }  
       
        
        /* 5 - Cr�ation et Lancement des Thread d'�coute */
        CThreadEcouteRobot  EcouteRobot  = new CThreadEcouteRobot(Moteur, portRobot,profArret,temps);// Cr�ation du thread d'ecoute sur le port choisit
        CThreadEcouteClient EcouteClient = new CThreadEcouteClient(index, Moteur, portApplet);       // Cr�ation du thread d'ecoute sur le port choisit
        
        EcouteRobot.start();     // Lancement de l'ecoute de connexion des Robots
        EcouteClient.start();    // Lancement de l'�coute de conexion des interfaces clientes        
    }
}

/* * CAmorce.java * * Created on 4 juin 2005, 16:09
 * @author  Fildz */
import Annex.*;
import java.io.*;
import java.net.*;

public class CThreadAmorce extends Thread {
    
    private String _mot; 
    private URL _url ;
    private CMoteur _moteur; 
    
    public CThreadAmorce(String mot, CMoteur moteur) {
        this._mot = mot;
        try {
        this._url = new URL(construitUrl()) ;
        }
        catch( MalformedURLException e) { System.out.println("EXCEPTION, url mal former : "+e); }
        this._moteur = moteur;  
        start();
    }
    
    
    
    public void run() {
        System.out.println("CThreadAmorce> Recherche des mots "+_mot+" sur Yahoo.fr");
        String chaine="" ;  // variable dans laquel on va stocker la page web
        
        /* Ouverture puis fermeture du flux provenant de la page web et se stockant dans la variable chaine */
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(_url.openStream()));
            String ligne;
            System.out.println("\nCThreadAmorce> Recuperation des resultats de Yahoo.fr");
            while((ligne = in.readLine()) != null) 
                chaine+=ligne;
            in.close() ;
        } catch (Exception e) { System.out.println("CThreadAmorce> EXCEPTION, lors de l'enregistrement de l'url d'amorce : "+e);};
        
        System.out.println("CThreadAmorce> Analyse des resultats");
        
        while( (chaine.length()>1) && (chaine.indexOf("<a class=yschttl  href=") != -1 )) {
        chaine = chaine.substring(chaine.indexOf("<a class=yschttl  href=")+23);
        String lien = chaine.substring(0,chaine.indexOf("\">"));
        lien = "http://"+lien.substring(lien.indexOf("/*-http%3A")+12);
        
        System.out.println("CThreadAmorce> Ajout de "+lien);
       try {
            _moteur.ajouterURL(new CURLProfondeur(new URL(lien),0));
        }
        catch( MalformedURLException e) { System.out.println("EXCEPTION, lien trouve mal forme : \nlien = "+lien+" \nErreur = "+e); }
        }
        System.out.println("CThreadAmorce> Amorcage Fini.");
    }
    
    
    public String construitUrl() {
      return "http://fr.search.yahoo.com/search?fr=fp-pull-web-t-2&p="+_mot+"&ei=ISO-8859-1";
    }
}


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

/**** Classe CThreadEcouteClient ******
 * Cette classe nous permet d'�cout� le port 
 * destin� aux communications entre l'interface cliente
 * et la classe CThreadClient
 *
 *@autor Anthony , 5 mai 2005
 ****************************/

/* Importation des packages */
import java.net.*;


public class CThreadEcouteClient extends Thread {
    
    /** Variables de classe **/
    CIndex  _index;             // r�f�rence vers l'index
    CMoteur _moteur;            // r�f�rence vers le moteur pour connaitre le nb de liens visiter
    int     _port;              // port � ecouter
    
    /** Constructeur **/
    public CThreadEcouteClient(CIndex index,CMoteur moteur,int port) {
        this._index = index;   // d�finition de la r�f�rence vers l'index
        this._port = port;
        this._moteur = moteur;
    }
    
    /** Lancement du Thread **/
    public void run() {
        /* Ecoute du port de communication entre l'interface cliente et le serveur.   
        Gestion du port de connexion entre l'applet client et le serveur java    */
          try  {
            System.out.println("CThreadEcouteClient> Ecoute du port "+_port+" pour les interfaces clientes ");
            ServerSocket serversocket = new ServerSocket(_port) ;
            while(true) 
                new CThreadClient(serversocket.accept(),_index,_moteur.getNbLiensVisiter());
        }
        catch(Exception e) { System.out.println("CCThreadEcouteClient> EXCEPTION, port "+_port+" utilis� par une autre application : "+e); } // on d�marre le Thread de requ�te
    }
}

/**** Classe CThreadEcouteRobot ******
 * Cette classe nous permet d'�cout� le port 
 * destin� aux communications entre le robot (classe CRobot)
 * et la classe CThreadRobot
 *
 *@autor Benoit, 5 mai 2005
 ****************************/

/* Importation des packages */
import java.net.*;


public class CThreadEcouteRobot extends Thread {
    
    /* Variable de classe */
    CMoteur _moteur ;
    int     _port ;
    int     _profArret;
    int     _temp;
    
    
    /** Constructeur **/
    public CThreadEcouteRobot(CMoteur moteur,int port,int prof,int temp) {
        this._moteur = moteur ;
        this._port   = port ; 
        this._profArret= prof;
        this._temp = temp;
    }
    
    
    /** M�thode de lancement du Thread **/
    public void run() {
        /* Ecoute du port de Communication entre les robots de recherche et le moteur d'indexation.   
         Gestion du port de connexion entre l'applet client et le serveur java */
       try  {
            System.out.println("CThreadEcouteRobot> Ecoute du port "+_port+" pour les Robots de Recherche ");
            ServerSocket serversocket = new ServerSocket(_port) ;
            while(true) {
                new CThreadRobot(serversocket.accept(),_moteur,_profArret,_temp);
            }
        }
        catch(Exception e) { System.out.println("\nCThreadEcouteRobot> EXCEPTION, port "+_port+" utilis� par une autre application : "+e); } // on d�marre le Thread de requ�te
    }
}


/**** Classe CThreadRobot ******
 * Classe qui recoit les requ�tes des Robots de recherche
 *      - Elle est reli� aux Robots de recherche par l�interm�diaire d�un socket
 *      - Elle re�oit les objets envoy� par le classe CRobot
 *
 *@autor Benoit, 24 mai 2005
 */

// Importation des packages
import Annex.*;
import CChrono.*;
import java.util.*;
import java.net.* ; // pour le Socket
import java.io.* ; // pour les flux (PrintWriter & BufferedReader)

public class CThreadRobot extends Thread {
    /* Variables de classe */
    private Socket  _socket;
    private CMoteur _moteur;
    private int     _profArret ;
    private CChrono _chrono;
    
    private BufferedReader      _br;
    private ObjectInputStream   _ois;
    private ObjectOutputStream  _oos;
    
    /** Constructeur **/
    public CThreadRobot(Socket sock,CMoteur moteur,int prof,int temp) {
        this._profArret = prof;
        _chrono = new CChrono(temp);
        _chrono.start();
        System.out.println("\nC"+getName() +"-Robot> Creation Du thread d'ecoute...");
        this._socket = sock;                    // on d�fini le socket
        System.out.println("C"+getName() +"-Robot> Infos socket : "+ _socket);
        this._moteur = moteur;                   // on d�finie la r�f�rence vers le Serveur
        
        try {
            // _pw = new PrintWriter( new BufferedWriter( new OutputStreamWriter( _socket.getOutputStream() ) ) , true );                             // connexion du flux de sortie vers le client
            // _br = new BufferedReader( new InputStreamReader(_socket.getInputStream() ) );   // connexion du flux d'entr� des requ�tes du client
            _oos = new ObjectOutputStream( _socket.getOutputStream() );
            _ois = new ObjectInputStream( _socket.getInputStream() );
        }
        catch (IOException e) { System.out.println("\nC"+getName() +"-Robot> EXCEPTION, lors de la connexion des flux vers le Robot : "+e); }
        start();    // lancement du CThreadClient
    }
    
    // M�thode de lancement du Thread
    public void run()  {
        
        System.out.println("\nC"+getName() +"-Robot> Lancement du Thread ");
        try {
            while(_chrono.getTemp()>0) {
                System.out.println("C"+getName() +"-Robot> Demande d'une Url pour le Robot");
                CURLProfondeur u = _moteur.avoirURL();
                _oos.writeObject(u);                       // Envoie d'une URLProfondeur
                System.out.println("C"+getName() +"-Robot> Envoie de l'URL : "+u.getUrl().toExternalForm());
                System.out.println("C"+getName() +"-Robot> Attente des resultats du Robots");
                Vector listeClePoid = (Vector)   _ois.readObject();        // Reception du Vector de ClePoid
                String[] liens      = (String[]) _ois.readObject();        // Reception du Tableau de liens
                String url          = (String)   _ois.readObject();        // Reception de l'url
                String titre        = (String)   _ois.readObject();        // Reception du titre de l'url
                String desc         = (String)   _ois.readObject();        // Reception de la description de l'url
                Integer nbLiens     = (Integer)  _ois.readObject();        // Reception du nb de liens
                Integer prof        = (Integer)  _ois.readObject();        // Reception de la profondeur de l'url
                
                System.out.println("C"+getName() +"-Robot> Recuperation de l'analyse du Robot reussit");
                
                /* Ajout dans les hashtable de mots cl�s, du couple mot cl�-> url/Poid */
                System.out.println("C"+getName() +"-Robot> Ajout dans l'index des mots cles");
                
                _moteur.ajouterIndex(url, listeClePoid ) ;
                
                /* Ajout dans la hastable d'url, du titre et de la description correspondant */
                _moteur.ajouterIndexUrl(url, titre, desc);
                
                /* Ajout dans la file d'attente du serveur des urls trouv�es dans la page */
                if(prof.intValue()<_profArret) {                                  // si la profondeur du site analyse est bonne
                    _moteur.ajouterUrl(liens,nbLiens.intValue(),prof.intValue()); // alors on envoie les liens vers le Moteur
                    System.out.println("C"+getName() +"-Robot> Envoie des liens vers le moteur d'indexation -> Reussit");
                }
                else                                                              // sinon on ne le fait pas
                    System.out.println("C"+getName() +"-Robot> Profondeur limite, aucun envoie des liens vers le serveur");
            } // fin while
            if(_chrono.getTemp()<=0)                                              // on v�rifie si l'arret du thread provient de la fin du compte a rebourt
                System.out.println("C"+getName()+"-Robot> Temp allouer au Robot ecouler"); // ...si c'est le cas on affiche un message
        }
        catch( Exception e) {  } // System.out.println("CThreadRobot> EXCEPTION : " +e);
        
        /* Fin d'execution du Robot */
        try {
            System.out.println("C"+getName()+"-Robot> Le Robot s'est deconnecte.");
            _ois.close();   // fermeture du flux d'entre
            _oos.close();   // fermeture du flux de sortie
            _socket.close();// fermeture du socket s'il ne l'a pas d�j� �t� (� cause de l'exception lev�e plus haut)
        }
        catch (IOException e){ }
    }                       // fin du run
}                           // fin du Thread

/**** Classe CClePoid ******
 * Classe qui cr�e un objet CClePoid, a chaque mot de la page on lui associe un
 * poid afin de lui donner ou non de l'importance. Les objets CClePoid cr�� seront
 * stocker dans une ArrayList
 *
 * @autor Anthony , 4 mai 2005
 *****************************/
package Annex;

import java.io.* ;

public class CClePoid implements Serializable {
    
    private int poid;
    private String cle;
   
    /** Constructeur **/
    public CClePoid(String cle, int poid) {
        this.cle=cle;       // exemple mot = "maison"
        this.poid=poid;     // exemple poid = 15 (il s'agit du poid du mot dans la page) plus important s'il est entre <title> qu'en pleine page par exemple
    }
    
    public int getPoid() { return poid ; }
    public String getCle() { return cle ; }   
    
    public void setPoid(int p) { poid = p; }
    public void setCle(String c) { cle = c; }
}

/**** Classe CTitreDesc ******
 * Classe qui cr�e un objet comprenant le titre et la 
 * description d'une url. L'objet cr�e sera ajouter dans la 
 * valeur l'indexUrl qui met en correspondance les urls et 
 * leur titre/description
 *
 * @autor Anthony, 4 mai 2005
 */
package Annex;

import java.io.*;


public class CTitreDesc implements Serializable {
    
    private String desc;
    private String titre;
   
    /** Constructeur **/
    public CTitreDesc(String titre, String desc) {
        this.desc=desc;   
        this.titre=titre;
    }
    
    public String getDesc() { return desc ; }
    public String getTitre() { return titre ; }
    
}

/**** Classe CUrlPoid ******
 * Classe qui cr�e un objet comprenant une url et son "poid" et qui   
 * sera ajout� dans une Arraylist associ� a chaque "cl�" de CIndex
 *
 * @autor Anthony, 4 mai 2005
 */
package Annex;

import java.io.*;

public class CURLPoid implements Serializable{
    
    private int poid;
    private String url;
   
    /** Constructeur **/
    public CURLPoid(String url, int poid) {
        this.url=url;       // exemple url = "http://www.belles-maisons.fr"
        this.poid=poid;     // exemple poid = 15 (il s'agit du poid de l'url par rapport a la cl� de la hastable (cad le mot)
    }
    
    public int getPoid() { return poid; }
    public String getUrl() { return url; }
}


/**** Classe CURLProfondeur ******
 * Classe qui cr�e un objet comprenant une url et sa profondeur
 * afin de d�terminer leur profondeur
 *
 * @autor Anthony, 4 mai 2005
 */
package Annex;

import java.net.*;
import java.io.*;

public class CURLProfondeur implements Serializable { // serializable pour permettre l'envoi par socket au CRobot
    
    private int profondeur;
    private URL url;
   
    /** Constructeur **/
    public CURLProfondeur(URL url, int profondeur) {
        this.url=url;       
        this.profondeur=profondeur; 
    }
    
    public int getProfondeur() { return profondeur ; }
    public URL getUrl() { return url ; }
}


package Annex;
import java.io.*;
public class Lire
{
public static String S() // Lire un String
{
String tmp = "";
char C='\0';
try {
         while ((C=(char) System.in.read()) !='\n')
         {
          if (C != '\r')  tmp = tmp+C;
 
         }
   }
 catch (IOException e)
        {
          System.out.println("Erreur de frappe");
          System.exit(0);
        }
 return tmp;
} // fin de S()

 public static byte b()  // Lire un entier byte
 {
  	 byte x=0;
 	 	try {
			 x=Byte.parseByte(S());
	  	 	}
 	 	catch (NumberFormatException e) {
          System.out.println("Format numerique incorrect");
          System.exit(0);
    }	
	  return x ;
 }

 public static short s()  // Lire un entier short
 {
   	 short x=0;
 	 	try {
			 x=Short.parseShort(S());
	  	 	}
 	 	catch (NumberFormatException e) {
          System.out.println("Format numerique incorrect");
          System.exit(0);
    }	
	  return x ;
 }

 public static int i()  // Lire un entier
 {
 	 int x=0;
 	 	try {
			 x=Integer.parseInt(S());
  	 	}
 	 	catch (NumberFormatException e) {
          System.out.println("Format numerique incorrect");
          System.exit(0);
    }	
	  return x ;
 }

 public static long l()  // Lire un entier long
 {
 	 long x=0;
 	 	try {
			 x=Integer.parseInt(S());
  	 	}
 	 	catch (NumberFormatException e) {
          System.out.println("Format numerique incorrect");
          System.exit(0);
    }	
	  return x ;
 }

 public  static double d()  // Lire un double
 {
  	double x=0.0;
 	 	try {
	 		x=Double.valueOf(S()).doubleValue();
 	 	}
 	 	catch (NumberFormatException e) {
          System.out.println("Format numerique incorrect");
          System.exit(0);
    }	
	  return x ;
 }

 public  static float f()  // Lire un float
 {
   float x=0.0f;
 	try {
 		x=Double.valueOf(S()).floatValue();
    	}
 	catch (NumberFormatException e) 
 	{
          System.out.println("Format numerique incorrect");
          System.exit(0);
    }	
	  return x ;
 }

 public  static char c()  // Lire un caractere
 {
  String tmp=S();
  if (tmp.length()==0)
	  return '\n';
  else 
		{
		return tmp.charAt(0);
		}
 }
}

/****** Classe CChrono ******
 * Classe simulant un compte � rebour
 * (n�cessaire pour attribuer un temps
 * au CRobot) 
 *
 * @autor Benoit, 24 mai 2005
 ***************************/
package CChrono ;
public class CChrono extends Thread {
    private int temp;
    
    public CChrono(int temp) {
        this.temp = temp;
    }
    
    public void run() {
        while(temp>0) {
            try {
                sleep(1000);
            } catch(InterruptedException e){ System.out.println("CChrono> EXCEPTION : "+e);}
            temp--;
        }
    }
    
    public int getTemp() { return temp; }
    
}

/****** Classe CRobot ******
 * Il s'agit de la classe d'analyse des pages webs.
 * On va donc :
 *      1/ R�cup�re une url du serveur
 *      2/ Extraire les mots cl�s et les liens de la page
 *      3/ Envoie les r�sultats au serveur
 *
 * @autor Anthony, 24 mai 2005
 */


/** Importation des packages utiles   **/
import Annex. *;
import java.net.*;          // pour les objets URL et la connexion par Socket 
import java.io.*;           // pour les flux d'entr�e/sortie
import java.util.*;         // pour la convertion de la date de l'url en objet Date


/** Classe CThreadURL **/
public class CRobot   {
    
    /** Variables de classe **/
    private URL             _url;           // url sur laquel on travail
    private Vector          _listeClePoid;  // liste contenant tout les mots cl�s (objet CClePoid) de la page
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
            _listeClePoid = new Vector();// ArrayList contenant tout les objet CClePoid cad tout les mot cl� retenu avec le poid qui leur a �t� associ�
            recupererURL();             // m�thode qui va r�cup�rer une url dans la file d'attente du serveur
            afficherInfoURL();          // m�thode d'information sur l'url
            enregistrerURL();           // parcourt les caract�res de la page et la stock dans une chaine en m�moire, puis lance les traitements sur cette chaine (cad sur la page web)
            traiterCle();               // traite les mots cl� trouv� dans la page (suppression des caract�res sp�ciaux, des mots cl� en double etc...) 
            etatFinal();                // envoie toute les donn�es trouver (liens et mots cl�) au serveur.
        }
        _oos.close();                   // fermeture du flux de sortie
        _ois.close();                   // fermeture du flux d'entre
        _socket.close();                // fermeture du socket
        System.out.println("\nCRobot> *** Le Robot a terminer son travail. *** ");
    }
    
    
    /** Recup�ration d'une url dans la file d'attente du serveur **/
    public void recupererURL() throws IOException,ClassNotFoundException {
        CURLProfondeur up =(CURLProfondeur)  _ois.readObject();   //On get une url qui est en attente sur le Moteur d'indexation
        _url = up.getUrl();
        _prof = up.getProfondeur();
        System.out.println("\n_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _\n");
        System.out.println("CRobot> 1/8 Url a analyser : "+_url.toExternalForm());
        //try {_urlCon = _url.openConnection();} catch(IOException e) { System.out.println("Erreur urlCon ="+e); }
    }
    
    /** Affiche les infos relatives � l'url **/
    public void afficherInfoURL(){
        System.out.println("CRobot> 2/8 Information sur l'url :");
        System.out.println("CRobot> \tProtocole: "+_url.getProtocol());
        System.out.println("CRobot> \tPort: "+_url.getPort());
        System.out.println("CRobot> \tMachine: "+_url.getHost());
   //     System.out.println("CRobot> \tFichier: "+_url.getFile());
   //     System.out.println("CRobot> \tComplet: "+_url.toExternalForm());
    }
    
    
    /** Stock la page web en m�moire et lance la m�thode pour effectuer les traitements sur cette chaine **/
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
        chercherURLCle(chaine); // m�thode de traitement sur la page web pour trouver les mots cl�s et les liens
    }
    
    
    /** M�thode de traitement de la page web enfin de trouver les mots cl�, leur attribuer un poid, et trouver le liens de la page **/
    public void chercherURLCle(String ch) {
        String mot="";                      // mot courant 
        String balise="";                   // nom de la balise (pour savoir si c'est la balise <script> (alors balise = "script")
        int taille = ch.length();           // taille de la chaine
        char tmp ;                          // caract�re courant pour ne pas faire ch.charAt(i) plein de fois (optimisation powa)
        boolean script              = false;// pour savoir si on est entre les balises <script> et </script>
        boolean boolEntreBaliseH1   = false;// pour savoir si nous sommes entre les balises <h1> et </h1>
        boolean boolEntreBaliseTitle= false;//pour savoir si nous sommes entre les balises <title> et </title>
        boolean boolBaliseDeDeb     = false;// pour savoir si nous venos juste de passer sur le caract�re "<"
        boolean boolBalise          = false;// pour savoir si on est entre <  >
        
        for(int i=0;i<taille;i++) {         // parcourt de toute la page web, caract�re par caract�re
            tmp=ch.charAt(i);               // r�cup�ration du caract�re courant
            
            /** Gestion d'une balise **/
            if (tmp == '<'){                // si on entre dans une balise
                boolBalise=true ;           // on l'indique en passant boolBalise a true
                boolEntreBaliseH1=false;    // d�s qu'on rencontre une balise ouvrante on (re)met ce bool�en a false
                boolEntreBaliseTitle=false; // d�s qu'on rencontre une balise ouvrante on (re)met ce bool�en a false
                boolBaliseDeDeb = true;
            }
            if (tmp == '>') {               // si on rencontre une fermeture de balise
                balise+=tmp ;               // on continue de stocker ce qu'il y avait dans balise afin de connaitre le nom de la balise
                balise=balise.toLowerCase();// on passe la balise en minuscule pour etre sur d'effectuer les traitements m�me si on avait des balise en majuscule
                boolBalise=false;           // on va interrompre la concat�nation du nom de la balise grace a ce bool�en
                
                if (balise.startsWith("<script") || balise.startsWith("<style"))  // il s'agissait de la balise script ou style
                    script=true;                    // on passe le bool�en a true (car on ne traite pas les mots qui sont entre ces balises)
                else if (balise.startsWith("<a ") && !script) {// il s'agissait de la balise <a..> marche pour <A oci normalement (AVERIFIER sur jeuxvideo.com)
                    traiterLien(recupererValeurAttribut("href",balise));// on lance une fonction de r�cup�ration des valeurs d'attribut de balise
                }
                else if (balise.startsWith("<h1"))      // si la balise est h1
                    boolEntreBaliseH1=true;             // on passe le bool�en � true
                else if (balise.startsWith("<title"))   // si la balise est title
                    boolEntreBaliseTitle=true;          // on passe le bool�en � true
                else if (balise.startsWith("<meta name=\"keywords\" "))
                     traiterKeywords(recupererValeurAttribut("content",balise));
                else if (balise.startsWith("<meta name=\"description\" "))
                     _desc = remplacementAccentsParHTML(recupererValeurAttribut("content",balise));
                else if (balise.startsWith("</script") || balise.startsWith("</style")) // si on a les balises de fin de script ou style
                    script=false;                       // on passe le bool�en a true (pour reprendre "l'�coute" des mots cl�s
                balise="";                              // une fois les bool�ens mis a leur valeur selon la balise, on purge la variable
            }
            
            /** Gestion d'un mot **/
            if ((!boolBalise) && (!script)) {       // si on n'est pas dans une balise, ni un script ou un style
                if ((tmp!=' ') && (tmp!='\n') && (tmp!='\t') && (tmp!=',') && (tmp!= '/') && (tmp!='.') && (tmp!='|') && (tmp!='-') && (tmp!='>') && (tmp!='<')){ // si on a pas un caract�re d�limitatoire de mot
                        mot+=tmp;                   // alors on continue de concat�n� les lettres du mot
                }
                else  {                             // si on rencontre un caract�re d�limitatoire alor le mot est fini
                    if (mot.length() > 2) {         // si le mot fait plus de deux lettres on le garde en l'ajoutant dans l'ArrayList
                        if(!motInterdit(mot)) {     // on v�rifie qu'il ne fait pas parti des mots de plus de deux lettres trop courant (comme "les")
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
                balise+=tmp ;           // alors on concat�ne le caract�re suivant afin de construire la balise ces attribut et leurs valeurs
            if (boolEntreBaliseTitle)   // r�cup�ration du titre de la page
                _titre+=tmp;
        }
        System.out.println("CRobot> 4/8 Mot Cle et Liens trouver");
        
    }
     
    /** M�thode permettant de ne pas indexer les mots trop r�current de plus de deux lettres **/
    public boolean motInterdit(String mot) {
        String lesInterdits[]= {"des", "les", "est", "dans" ,"that", "thus","que","pour","sur","mon","son","ces","mes","tes" }; // tableaux de mots interdits
        boolean interdit = false;                   // bool�en de rencontre de mot interdit
        for(int i=0;i<lesInterdits.length;i++)      // parcourt du tableau de mots interdit
               if (lesInterdits[i].equals(mot))     // si mot est un mot appartenant au tableau
                   interdit = true ;                // on passe le bool�en a true
        return interdit ;                           // on retourne le bool�en
    }
    
    
    /** R�cup�ration de la valeur d'un attribut d'une balise html **/
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
    
    
    /** Traitements des liens trouver dans la page, v�rifie leur validit� et reconstruit les liens relatifs **/
    public void traiterLien(String lien) {
        boolean lienValide  = false;                // on v�rifie si le lien est valide
        boolean lienRelatif = false ;               // ou s'il est relatif
        boolean lienAbsolu  = false;                // ou absolu
        String url = _url.toExternalForm();         // on r�cupere le l'url de la page sur laquel on se trouve pour reconstruire les liens relatif
        String [] tExt = { ".html" , ".php" , "/" , ".com" , ".net" , ".org" , ".st" , ".fr" , ".de" , "htm" ,".ca" , ".be" ,"." };
        for (int i=0 ; i< tExt.length ; i++)        // parcourt du tableau d'extension concid�r� comme valide
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
        if (lienAbsolu && _nbLiens<_liens.length) { // si c'est un lien absolu on ajoute sans probl�me ds la liste
            _liens[_nbLiens++]=lien;
       }
        else if (lienRelatif && !lienAbsolu  && _nbLiens<_liens.length) {
          _liens[_nbLiens++]= url + lien ;
      }
    }
   
    
    /** M�thode utilisant les StringTokenizer pour extraire les mots du "content" de la balise <meta name="keyword"> **/
    public void traiterKeywords(String keys) {
        String cle="";
        StringTokenizer st = new StringTokenizer(keys,", "); //la virgule et l'espace sont les d�limitateurs de mots
        while(st.hasMoreTokens()){
            cle = st.nextToken();
            _listeClePoid.add(new CClePoid(suppressionEspaces(suppressionAccents(suppressionCaracteresSpeciaux(cle))),10)); // on affecte un poid de 10 aux mots trouv�s dans keywords
         }
    }    
    
    /** M�thode traitant les caract�res sp�ciaux � l'int�ri�ur des chaines du titre et/ou de la description **/
    public String suppressionCaracteresSpeciaux(String ch){
        String caracSpe="";         // caract�re sp�ciale trouver dans la chaine
        String[][] tConv = {{"&nbsp;"," "},{"&amp;"," "},{"&hellip;"," "},{"&gt;"," "},{"&lt;"," "},{"&quot;"," "}, {"&copy;"," "} , {"&Ecirc;","E"}, {"&Euml;","E"}, {"&Egrave;","E"} , {"&Ocirc;","O"} , {"&Ugrave;","U"} , {"&ugrave;","�"}, {"&ucirc;","�"} , {"&Agrave;","A"} ,{"&Acirc;","A"} , {"&agrave;","�"} , {"&auml;","�"},{"&acirc;","�"} , {"&egrave;","�"} , {"&eacute;","�"} , {"&ecirc;","�"} , {"&euml;","�"}, {"&icirc;","�"}, {"&iuml;","�"}, {"&ocirc;","�"},{"&ouml;","�"}, {"&otilde;","�"},{"&#339;","oe"},{"&ccedil;","�"},{"&ntilde;","�"}, {"&#8217;","'"}, {"&#160;"," "}, {"&#187;"," "} } ;
        boolean concat=false;       // si on a vu un '&' dans le mot alors on lance la concat�nation de caracSpe.
        boolean effectuer=false;    // si on a effectuer un remplacement
        int indDeb=0;               // indice du '&' dans le mot
        int indFin=0;               // indice du ';' dans le mot
        int j=0;
        do {
            if (j<ch.length() && ch.charAt(j)=='&') {
                caracSpe += ch.charAt(j);   // cr�ation du caract�re sp�ciaux d�s qu'on voit un '&'
                indDeb=j;                   // on retien l'indice sur lequel on avait le '&'
                concat=true;
            }
            else if (concat) {
                caracSpe += ch.charAt(j);   // on entamme la concat�nation jusqu'� ce qu'on trouve un ';'
                if (ch.charAt(j)==';'){     // d�s qu'on tombe sur le ';' on arr�te de concat�n� notre caracSpe
                    concat=false;
                    indFin=j;               // on retient l'indice ou il y a le ';'
                    if (caracSpe!=""){      // on v�rifie si on a trouver un caract�re sp�ciale
                        for (j=0; j<tConv.length ; j++){            // renvoi la taille du premier indice
                             if (caracSpe.equals(tConv[j][0])) {    // si le caractere sp�ciale trouver fait parti du tableau de correspondance
                                ch = ch.substring(0,indDeb)+tConv[j][1]+ch.substring(indFin+1,ch.length());
                                effectuer=true;
                            }
                        }
                    }
                    if(effectuer){  // si on a effectu� un remplacement dans la chaine
                        caracSpe="";// on reinitialise les variables ici car on rev�rifie le meme mot afin de voir s'il n'as pas un deuxieme caractere sp�ciale
                        j=0;        // on va donc recommencer la v�rification du mot a partir de son debut
                        effectuer=false;
                    }
                }
            }
            j++;
        }
        while (j<ch.length());
        return ch;
    }
    
    
    /** suppression des accents et caract�res non-mots **/
    public String suppressionAccents(String ch) {
        char t[][] =       {{'�','e'},{'�','e'},{'�','e'},{'�','o'},{'�','o'},{'�','o'},{'�','u'},{'�','u'},{'�','n'},{'�','c'},{'�','a'},{'�','a'},{'�','a'},{'�','i'},{'�','i'}};
        char tEspace[][] = {{'[',' '},{']',' '},{'"',' '},{'!',' '},{'(',' '},{'$',' '},{'*',' '},{'+',' '},{')',' '},{'}',' '},{'{',' '},{':',' '},{'|',' '},{'�',' '},{'?',' '}};
        for(int i=0; i<_listeClePoid.size(); i++) {
            for (int j=0 ; j<t.length ; j++)
               for (int k=0 ; k<ch.length(); k++) {
                   if (t[j][0]==ch.charAt(k))
                       ch=ch.replace(ch.charAt(k),t[j][1]);
                   else if (tEspace[j][0]==ch.charAt(k))                  // attenion: tEspace et t doivent etre des tableaux de m�me taille !
                       ch=ch.substring(0,k)+ch.substring(k+1,ch.length());//on ne remplace pas par ' ' mais on supprime l'espace !
               }
        }
        return ch;
    }
    
    
    /** M�thode permettant de remplac� les '�' '�' etc... par leur equivalent en html **/
    public String remplacementAccentsParHTML(String ch) {
        String[][] tConv = {{"&ugrave;","�"}, {"&ucirc;","�"} , {"&agrave;","�"} , {"&auml;","�"},{"&acirc;","�"} , {"&egrave;","�"} , {"&eacute;","�"} , {"&ecirc;","�"} , {"&euml;","�"}, {"&icirc;","�"}, {"&iuml;","�"}, {"&ocirc;","�"},{"&ouml;","�"}, {"&otilde;","�"},{"&ccedil;","�"},{"&ntilde;","�"}, {"&#8217;","'"} } ;
        for (int j=0 ; j<tConv.length ; j++){
            for (int k=0 ; k< ch.length() ; k++)
               if (tConv[j][1].equals(String.valueOf(ch.charAt(k))))
                   ch=ch.substring(0,k)+tConv[j][0]+ch.substring(k+1,ch.length());
        }
        return ch;
    }
    
    
    /** Suppression des espaces � l'int�rieur des mots**/
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
   
    
    /** Traitements des mots cl�s trouv�s dans la page :
     *      - mise en minuscule
     *      - suppression des doublons (et modification leur poid en cons�quence)
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
     *      -D'un tableau de liens (qui ont �t� recup�r� dans la page)
     *      -Une liste de mots cl�s trait�s (les mots cl�s de la page sont pr�t � �tre ajout�s dans l'index)
     * Alors on va ajouter les mots cl� dans l'hashtable de l'index et la liste d'url dans la file d'attente du serveur
     **/
    public void etatFinal()  throws IOException , UnknownHostException{   
      /* Affichage */
      System.out.println("CRobot> 5/8 Nombre de cle a envoyer au Serveur ajouter : "+_listeClePoid.size());
      System.out.println("CRobot> 6/8 Nombre de Liens a envoyer au Serveur : "+_nbLiens);
      System.out.println("CRobot> 7/8 Envoie des objets...");
       
      String url = _url.toExternalForm();               // le nom de l'url en cours
      _titre= remplacementAccentsParHTML(_titre);       // on transforme les '�' '�' en caract�re sp�ciaux html car il seront interpr�t� par le client flash
        
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

/*******************************************************************************************
 Annexe - Code Source de l'application Flash (ActionScript 2.0) 
 *******************************************************************************************/

/********************************
* Connexion sur Gougoule Serveur
*********************************/
	// nouvelle instance de XMLSocket()
	socket = new XMLSocket();
	// � la reception d'un message du serveur, reception(chaine) sera ex�cut�e
	socket.onXML = reception;
	//socket.connect(adresseIpDuServeur,port);
	socket.connect(adresse, port );
	// d�s que la connexion a lieu, connexion() est ex�cut�e
	socket.onConnect = connexion;
	// la scene a �t� charg�e une premi�re fois donc true (bool�en de s�curit�)
	socket.onClose = fermeture ;

/**************
* Boutons
**************/
/** Bouton "Envoyer" **/
btn_envoyer.onRelease = envoyerRecherche;
btn_retour.onRelease = function() {
	_root.gotoAndStop(1);
}
/************
* Fonctions 
************/
// Envoie la requete du client vers le serveur java
function envoyerRecherche() {
	// si le champs de recherche n'est pas vide
	if (_root.txt_mots.text != "") {
		txtMots= _root.txt_mots.text ;
		txtMots = traiterRecherche(txtMots);
		texte = new XML('<requete value="'+txtMots+'" />');
		}
		//... et on l'envoi au serveur :
		_root.socket.send(texte); // Envoi de la requete
		_root.txt_area.text =""; // Suppression des anciens r�sultats affich�s dans le txt_area
}

function connexion(isOk) {
	if (!isOk) // Si la connexion s'est mal pass�
		_root.txt_area.text += "<i>Echec de la connexion...</i>";
}

// Fonction de reception[[
function reception(chaine) {
	// on regarde le premier fils (il est seul donc pas de pb)
	chaine = chaine.firstChild; 
		
	if (chaine.nodeName == "resultat") { 
		txt_nbLiens.text=chaine.attributes.nbLiens+" lien(s) trouv�(s)";
		txt_totalLiens.text=chaine.attributes.totalLiens+" pages r�f�renc�es";
		if (chaine.attributes.nbLiens == 0)
			_root.txt_area.text+="<b>Aucun liens n'a �t� trouv� pour \""+txt_mots.text+"\" .</b>";	
	}
	else {
		var valeur=supprimerCaractereSpeciaux(chaine.attributes.value);
		var titre=supprimerCaractereSpeciaux(chaine.attributes.titre);
		var description=supprimerCaractereSpeciaux(chaine.attributes.description);
		if(description.length>1)
			_root.txt_area.text += '<a href="'+valeur+'" target="_blank"><font size="14" color="#0000FF"><u>'+ titre +'</u></font></a><br />Description : '+description+'<br /><font color="#009900">'+chaine.attributes.value+'<b> [ score : '+chaine.attributes.poid+' ]</b></font><br />&nbsp;<br />';
		else
			_root.txt_area.text += '<a href="'+valeur+'" target="_blank"><font size="12" color="#0000FF"><u>'+ titre +'</u></font></a><br />Description : pas de description disponible.<br /><font color="#009900">'+chaine.attributes.value+'<b> [ score : '+chaine.attributes.poid+' ]</b></font><br />&nbsp;<br />';
	}
}

function fermeture() {
	socket.close();
}

function supprimerCaractereSpeciaux(ch:String):String {
		var caracSpe="";         // caract�re sp�ciale trouver dans la chaine
        tConv = [["&nbsp;"," "],["&amp;"," "],["&hellip;"," "],["&gt;"," "],["&lt;"," "],["&quot;"," "], ["&copy;"," "] , ["&Ecirc;","E"], ["&Euml;","E"], ["&Egrave;","E"] , ["&Ocirc;","O"] , ["&Ugrave;","U"] , ["&ugrave;","�"], ["&ucirc;","�"] , ["&Agrave;","A"] ,["&Acirc;","A"] , ["&agrave;","�"] , ["&auml;","�"],["&acirc;","�"] , ["&egrave;","�"] , ["&eacute;","�"] , ["&ecirc;","�"] , ["&euml;","�"], ["&icirc;","�"], ["&iuml;","�"], ["&ocirc;","�"],["&ouml;","�"], ["&otilde;","�"],["&#339;","oe"],["&ccedil;","�"],["&ntilde;","�"], ["&#8217;","\'"], ["&#160;","\'"], ["&#187;","\'"] ] ;
		changement=false;
		do {
			changement=false;
			debut = ch.indexOf('&');
			fin = ch.indexOf(';');		
			if(fin!=-1) {
				motif = ch.substring(debut,fin+1);
				i=0;			
				while( i<tConv.length && !(motif == tConv[i][0]) )
					i++;
				if(i<tConv.length){
					ch = ch.substring(0,debut)+ tConv[i][1] + ch.substring(fin+1);
					changement=true;
				}
			}
		}
		while(changement);
return ch;	
}

function traiterRecherche(texte:String):String {
	  tConv = [['�','e'],['�','e'],['�','e'],['�','o'],['�','o'],['�','o'],['�','u'],['�','u'],['�','n'],['�','c'],['�','a'],['�','a'],['�','a'],['�','i'],['�','i']] ;
	  
	  for(i=0;i<tConv.length;i++) {
	  changement=false;
	  do {
		  changement = false;
		  indice = texte.indexOf(tConv[i][0]);
			if (indice>=0) {
			texte = texte.substring(0,indice) + tConv[i][1] + texte.substring(indice+1);
			changement = true;
			}
	  }
	  while(changement);
	  }
return texte;	  
}
