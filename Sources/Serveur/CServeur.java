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

