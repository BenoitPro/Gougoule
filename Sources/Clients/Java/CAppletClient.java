/* * CAppletClient.java * Created on 12 mai 2005, 21:22 *
 * @author  Fildz */ 
import java.net.* ;
import java.io.* ;
import javax.swing.*;

public class CAppletClient extends javax.swing.JApplet {
    
    // Variable de classe \\    
    private Socket _socket ;
    private PrintWriter _out ;
    private BufferedReader _in ;
    
    // Méthodes \\

    
    // Constructeur \\    
    public void init() {
         System.out.println( "Lancement de l'applet client");
         initComponents();
    }
        
    /** Envoie de la requete Et Reception de la reponse de la recherche **/ 
    public void envoyerEtRecevoir() {
       jPanel2.removeAll();
       validate();
       String contenu = jTextField1.getText() ;
      jLabel1.setText("Status : Recherche Lancée") ;
      System.out.println(contenu);
      _out.print("<requete value=\""+contenu+"\" />\u0000") ; // ecriture dans le flux du contenu
      _out.flush();
      
      
       try {
           JLabel jLabel ;
           String reponse ="";
            char charCur[] = new char[1] ;
            while(_in.read(charCur,0,1)!= -1) {     // on traite la requête cliente caractère par caractère
                if ( reponse.indexOf("  />") == -1) { // tant qu'on est pas en fin de chaine...
                    reponse+=charCur[0];  //... on concatène le  caractère à la chaine déjà créé
                }
                else {
                    String tmp = reponse.substring(0,reponse.indexOf("<resultat"));
                    if (tmp.length()<1) {
                    jLabel = new JLabel();
                    jLabel.setText("Aucuns résultats trouvé");
                    jPanel2.add(jLabel);
                    }
                    while( tmp.length()>1) {
                    
                    String value =      donnerValeurAttribut("value",tmp);
                    String titre =      donnerValeurAttribut("titre",tmp);
                    String desc =       donnerValeurAttribut("description",tmp);
                    if(desc.length()>110)
                        desc = desc.substring(0,110) + "<br>" + desc.substring(70);
                    if(desc.length()>220)
                        desc = desc.substring(0,220) + "...";
                    jLabel = new JLabel();
                    jLabel.setText("<html><font color=\"blue\">"+ titre + "</font><br>Description : " + desc + "<br><i><font color=\"green\">" + value + "</font></i><br></html>" );
                    jPanel2.add(jLabel);
                    tmp=tmp.substring(tmp.indexOf("/>")+2);
                    validate();
                    } 
                    
                    
                    String nbLiens =    donnerValeurAttribut("nbLiens",reponse);
                    String totalLiens = donnerValeurAttribut("totalLiens",reponse);
                    
                 jLabel1.setText("Status : Affichage des résultats") ; 
                    break;
                }
                validate();
           }
           }
           catch(IOException e) { System.out.println("ERREUR : "+e); }   
        
    }
    
     /** Méthode qui renvoie la valeur d'un attribut dans la chaine "message" (écrit en XML) **/
    public String donnerValeurAttribut(String attribut,String message) {
        int tatt= attribut.length();
        int tmess= message.length(); 
        int deb=0;
        int fin;
        while(!message.substring(deb,deb+tatt).equals(attribut) && (deb+tatt<=tmess) ) {
            deb++;
        }
        deb=deb+tatt + 2 ;
        fin=deb;
        while(message.charAt(fin) != '\"') {
            fin++;            
        }
        return message.substring(deb,fin);
    }
    
    private void initComponents() {//GEN-BEGIN:initComponents
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();

        jScrollPane1.setBackground(new java.awt.Color(255, 255, 255));
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.Y_AXIS));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jScrollPane1.setViewportView(jPanel2);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.X_AXIS));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setPreferredSize(new java.awt.Dimension(30, 30));
        jPanel3.add(jLabel1);

        getContentPane().add(jPanel3, java.awt.BorderLayout.SOUTH);

        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.Y_AXIS));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel7.setBackground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Adresse du serveur");
        jPanel7.add(jLabel3);

        jTextField2.setText("localhost");
        jTextField2.setPreferredSize(new java.awt.Dimension(120, 20));
        jPanel7.add(jTextField2);

        jLabel4.setText("Port");
        jPanel7.add(jLabel4);

        jTextField3.setText("18001");
        jTextField3.setPreferredSize(new java.awt.Dimension(100, 20));
        jPanel7.add(jTextField3);

        jButton2.setText("Connecter");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jPanel7.add(jButton2);

        jPanel4.add(jPanel7);

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jTextField1.setColumns(16);
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jPanel6.add(jTextField1);

        jButton1.setText("Rechercher");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jPanel6.add(jButton1);

        jPanel4.add(jPanel6);

        getContentPane().add(jPanel4, java.awt.BorderLayout.NORTH);

    }//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
       
        try {
           System.out.println("Tentative de connexion sur "+jTextField2.getText()+ " - "+Integer.parseInt(jTextField3.getText()));
         _socket = new Socket(jTextField2.getText(), Integer.parseInt(jTextField3.getText()) ) ; // Connexion sur le port 18001 
       
         // Récupération des flux d'entre et de sortie
         _out = new PrintWriter(_socket.getOutputStream() );
         _in = new BufferedReader( new InputStreamReader( _socket.getInputStream() ) );
        }
        catch(Exception e) {  jLabel1.setText("Status : La connexion a échoué :"+e) ;  }
        if(_socket.isConnected())
            jLabel1.setText("Status : Connecté") ;
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    envoyerEtRecevoir();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
    envoyerEtRecevoir();
    }//GEN-LAST:event_jTextField1ActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    // End of variables declaration//GEN-END:variables
    
}
