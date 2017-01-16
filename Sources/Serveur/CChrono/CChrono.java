/****** Classe CChrono ******
 * Classe simulant un compte à rebour
 * (nécessaire pour attribuer un temps
 * au CRobot) 
 *
 * @autor Benoit, 24 mai 2005
 ***************************/
package CChrono ;
public class CChrono extends Thread {
    private int temp;
    
    public CChrono(int temp) {
        this.temp = temp;
    }
    
    public void run() {
        while(temp>0) {
            try {
                sleep(1000);
            } catch(InterruptedException e){ System.out.println("CChrono> EXCEPTION : "+e);}
            temp--;
        }
    }
    
    public int getTemp() { return temp; }
    
}
