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
