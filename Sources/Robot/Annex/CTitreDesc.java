/**** Classe CTitreDesc ******
 * Classe qui crée un objet comprenant le titre et la 
 * description d'une url. L'objet crée sera ajouter dans la 
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
