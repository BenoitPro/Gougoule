/**** Classe CURLProfondeur ******
 * Classe qui crée un objet comprenant une url et sa profondeur
 * afin de déterminer leur profondeur
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
