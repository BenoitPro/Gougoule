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
