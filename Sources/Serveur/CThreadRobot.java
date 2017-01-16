/**** Classe CThreadRobot ******
 * Classe qui recoit les requêtes des Robots de recherche
 *      - Elle est relié aux Robots de recherche par l’intermédiaire d’un socket
 *      - Elle reçoit les objets envoyé par le classe CRobot
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
        this._socket = sock;                    // on défini le socket
        System.out.println("C"+getName() +"-Robot> Infos socket : "+ _socket);
        this._moteur = moteur;                   // on définie la référence vers le Serveur
        
        try {
            // _pw = new PrintWriter( new BufferedWriter( new OutputStreamWriter( _socket.getOutputStream() ) ) , true );                             // connexion du flux de sortie vers le client
            // _br = new BufferedReader( new InputStreamReader(_socket.getInputStream() ) );   // connexion du flux d'entré des requêtes du client
            _oos = new ObjectOutputStream( _socket.getOutputStream() );
            _ois = new ObjectInputStream( _socket.getInputStream() );
        }
        catch (IOException e) { System.out.println("\nC"+getName() +"-Robot> EXCEPTION, lors de la connexion des flux vers le Robot : "+e); }
        start();    // lancement du CThreadClient
    }
    
    // Méthode de lancement du Thread
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
                
                /* Ajout dans les hashtable de mots clés, du couple mot clé-> url/Poid */
                System.out.println("C"+getName() +"-Robot> Ajout dans l'index des mots cles");
                
                _moteur.ajouterIndex(url, listeClePoid ) ;
                
                /* Ajout dans la hastable d'url, du titre et de la description correspondant */
                _moteur.ajouterIndexUrl(url, titre, desc);
                
                /* Ajout dans la file d'attente du serveur des urls trouvées dans la page */
                if(prof.intValue()<_profArret) {                                  // si la profondeur du site analyse est bonne
                    _moteur.ajouterUrl(liens,nbLiens.intValue(),prof.intValue()); // alors on envoie les liens vers le Moteur
                    System.out.println("C"+getName() +"-Robot> Envoie des liens vers le moteur d'indexation -> Reussit");
                }
                else                                                              // sinon on ne le fait pas
                    System.out.println("C"+getName() +"-Robot> Profondeur limite, aucun envoie des liens vers le serveur");
            } // fin while
            if(_chrono.getTemp()<=0)                                              // on vérifie si l'arret du thread provient de la fin du compte a rebourt
                System.out.println("C"+getName()+"-Robot> Temp allouer au Robot ecouler"); // ...si c'est le cas on affiche un message
        }
        catch( Exception e) {  } // System.out.println("CThreadRobot> EXCEPTION : " +e);
        
        /* Fin d'execution du Robot */
        try {
            System.out.println("C"+getName()+"-Robot> Le Robot s'est deconnecte.");
            _ois.close();   // fermeture du flux d'entre
            _oos.close();   // fermeture du flux de sortie
            _socket.close();// fermeture du socket s'il ne l'a pas déjà été (à cause de l'exception levée plus haut)
        }
        catch (IOException e){ }
    }                       // fin du run
}                           // fin du Thread
