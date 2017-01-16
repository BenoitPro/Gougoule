/**** Classe CClePoid ******
 * Classe qui crée un objet CClePoid, a chaque mot de la page on lui associe un
 * poid afin de lui donner ou non de l'importance. Les objets CClePoid créé seront
 * stocker dans une ArrayList
 *
 * @autor Anthony , 4 mai 2005
 *****************************/
package Annex;

import java.io.* ;

public class CClePoid implements Serializable {
    
    private int poid;
    private String cle;
   
    /** Constructeur **/
    public CClePoid(String cle, int poid) {
        this.cle=cle;       // exemple mot = "maison"
        this.poid=poid;     // exemple poid = 15 (il s'agit du poid du mot dans la page) plus important s'il est entre <title> qu'en pleine page par exemple
    }
    
    public int getPoid() { return poid ; }
    public String getCle() { return cle ; }   
    
    public void setPoid(int p) { poid = p; }
    public void setCle(String c) { cle = c; }
}
