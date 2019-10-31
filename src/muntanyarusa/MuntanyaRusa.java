/*
 * Miquel Vidal Coll i Toni Rotger López
 * Primer lliurament de programació concurrent
 * El problema de la muntanya rusa
 */
package muntanyarusa;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Miguel Vidal i Toni Rotger
 * Enllaç al video: https://youtu.be/uN8nYMJv_M4
 */
public class MuntanyaRusa implements Runnable {

    static Semaphore mutexSubida = new Semaphore(1);
    static Semaphore mutexBajada = new Semaphore(0);
    static Semaphore mutexTren = new Semaphore(0);

    static Random r = new Random();

    static int numpasajeros;
    static int numthreads;
    
    static int MAX_Pasajeros = 21;
    static int MIN_Pasajeros = 5;
    
    int id;
    static int numviajes;
    String nombre;
    
    static volatile int viajes = 0;     //Viatges duits a terme
    static volatile int vansubiendo = 0;
    static volatile int vanbajando = 0;
    
    static Thread[] threads;
    static String[] nombres = {"Pedro", "Josemi", "Eusebio", "Martín", "Aurora",
        "Pau", "Elisa", "Toni", "Javier", "Miguel", "Sara", "Isabella",
        "Manel", "Patricia", "Adrián", "Aina", "Fidel", "Aarón", "Ismael"};

    


    public MuntanyaRusa(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    /*
    Si el thread te id == 0 es tracta del tren
    En cas contrari, el thread es un passatger
    */
    
    @Override
    public void run() {

        if (id == 0) {
            try {
                viajar();
            } catch (InterruptedException ex) {
                Logger.getLogger(MuntanyaRusa.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                embarcar();
                desembarcar();

            } catch (InterruptedException ex) {
                Logger.getLogger(MuntanyaRusa.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    
    public void embarcar() throws InterruptedException {

        mutexSubida.acquire();

        if (viajes < numviajes) {
            System.out.println("    " + nombre + " está subiendo");
            Thread.sleep(500);
            vansubiendo++;
            if (vansubiendo == 4) {
                System.out.println("    El vagón está lleno");
                vansubiendo = 0;
                mutexTren.release();
            } else {
                mutexSubida.release();
            }
        } else {
            System.out.println("    " + nombre + " dice: Bueno, volveré otro dia");
            mutexSubida.release();
        }
    }

    public void viajar() throws InterruptedException {

        while (viajes < numviajes) {
            mutexTren.acquire();
            System.out.println("");
            System.out.println("Sale el tren");
            Thread.sleep(1000);
            System.out.println("Fin del trayecto");
            System.out.println("");
            viajes++;
            System.out.println("numero de viajes: " + viajes);
            mutexBajada.release();
        }
    }

    public void desembarcar() throws InterruptedException {

        if (viajes < numviajes) {
            mutexBajada.acquire();
            System.out.println(nombre + " se despide");
            Thread.sleep(500);
            vanbajando++;
            if (vanbajando == 4) {
                vanbajando = 0;
                mutexSubida.release();
            } else {
                mutexBajada.release();
            }
        }
    }
    
    /*
    Algoritme de shuffle de Fisher-Yates
    vist a programació 2
    */
    public static void desordenar(String[] nom) {
        
       int n = nom.length;
       
        for (int i = 0; i < n ; i++) {
            int random = i + r.nextInt(n-i);
            String aux = nom[random];
            nom[random] = nom[i];
            nom[i] = aux;
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        //Calculam el nombre de passatgers, es farà un random entre 4 - 20
        numthreads = r.nextInt((MAX_Pasajeros - MIN_Pasajeros) + 1) + MIN_Pasajeros;
        numpasajeros = numthreads - 1;
        System.out.println("Tenemos " + numpasajeros + " pasajeros.");
        threads = new Thread[numthreads];

        numviajes = numpasajeros / 4;
        desordenar(nombres);

        System.out.println("Numero de viajes que habrá que hacer: " + numviajes);

        for (int i = 0; i < numthreads; i++) {
            threads[i] = new Thread(new MuntanyaRusa(i, nombres[i]));
            threads[i].start();
        }

        for (int i = 0; i < numthreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}
