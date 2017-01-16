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
