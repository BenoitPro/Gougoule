/**** Classe CThreadEcouteClient ******
 * Cette classe nous permet d'écouté le port 
 * destiné aux communications entre l'interface cliente
 * et la classe CThreadClient
 *
 *@autor Anthony , 5 mai 2005
 ****************************/

/* Importation des packages */
import java.net.*;


public class CThreadEcouteClient extends Thread {
    
    /** Variables de classe **/
    CIndex  _index;             // référence vers l'index
    CMoteur _moteur;            // référence vers le moteur pour connaitre le nb de liens visiter
    int     _port;              // port à ecouter
    
    /** Constructeur **/
    public CThreadEcouteClient(CIndex index,CMoteur moteur,int port) {
        this._index = index;   // définition de la référence vers l'index
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
        catch(Exception e) { System.out.println("CCThreadEcouteClient> EXCEPTION, port "+_port+" utilisé par une autre application : "+e); } // on démarre le Thread de requête
    }
}
