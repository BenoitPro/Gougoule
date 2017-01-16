/**** Classe CUrlPoid ******
 * Classe qui crée un objet comprenant une url et son "poid" et qui   
 * sera ajouté dans une Arraylist associé a chaque "clé" de CIndex
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
        this.poid=poid;     // exemple poid = 15 (il s'agit du poid de l'url par rapport a la clé de la hastable (cad le mot)
    }
    
    public int getPoid() { return poid; }
    public String getUrl() { return url; }
}
