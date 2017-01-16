/* * CAmorce.java * * Created on 4 juin 2005, 16:09
 * @author  Fildz */
import Annex.*;
import java.io.*;
import java.net.*;

public class CThreadAmorce extends Thread {
    
    private String _mot; 
    private URL _url ;
    private CMoteur _moteur; 
    
    public CThreadAmorce(String mot, CMoteur moteur) {
        this._mot = mot;
        try {
        this._url = new URL(construitUrl()) ;
        }
        catch( MalformedURLException e) { System.out.println("EXCEPTION, url mal former : "+e); }
        this._moteur = moteur;  
        start();
    }
    
    
    
    public void run() {
        System.out.println("CThreadAmorce> Recherche des mots "+_mot+" sur Yahoo.fr");
        String chaine="" ;  // variable dans laquel on va stocker la page web
        
        /* Ouverture puis fermeture du flux provenant de la page web et se stockant dans la variable chaine */
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(_url.openStream()));
            String ligne;
            System.out.println("\nCThreadAmorce> Recuperation des resultats de Yahoo.fr");
            while((ligne = in.readLine()) != null) 
                chaine+=ligne;
            in.close() ;
        } catch (Exception e) { System.out.println("CThreadAmorce> EXCEPTION, lors de l'enregistrement de l'url d'amorce : "+e);};
        
        System.out.println("CThreadAmorce> Analyse des resultats");
        
        while( (chaine.length()>1) && (chaine.indexOf("<a class=yschttl  href=") != -1 )) {
        chaine = chaine.substring(chaine.indexOf("<a class=yschttl  href=")+23);
        String lien = chaine.substring(0,chaine.indexOf("\">"));
        lien = "http://"+lien.substring(lien.indexOf("/*-http%3A")+12);
        
        System.out.println("CThreadAmorce> Ajout de "+lien);
       try {
            _moteur.ajouterURL(new CURLProfondeur(new URL(lien),0));
        }
        catch( MalformedURLException e) { System.out.println("EXCEPTION, lien trouve mal forme : \nlien = "+lien+" \nErreur = "+e); }
        }
        System.out.println("CThreadAmorce> Amorcage Fini.");
    }
    
    
    public String construitUrl() {
      return "http://fr.search.yahoo.com/search?fr=fp-pull-web-t-2&p="+_mot+"&ei=ISO-8859-1";
    }
    
    
    
}
