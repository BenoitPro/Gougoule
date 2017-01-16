/**** Classe CThreadEcouteRobot ******
 * Cette classe nous permet d'écouté le port 
 * destiné aux communications entre le robot (classe CRobot)
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
    
    
    /** Méthode de lancement du Thread **/
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
        catch(Exception e) { System.out.println("\nCThreadEcouteRobot> EXCEPTION, port "+_port+" utilisé par une autre application : "+e); } // on démarre le Thread de requête
    }
}
