/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.sockets;

/**
 *
 * @author VicM
 */

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ivan
 */
public class EnviaFitxerServidor {

    static final int METODE_ENVIO = 1; //1: Se envia tot en un array de bytes; 2: Se envía en blocs d'arrays de bytes
    Socket sk;
    String nomfich;

    static HashMap connectats = new HashMap<String, Long>();

    public static void main(String[] args) { // prova l'enviament de fitxers

        int PORT = 44444;

        try {

            ServerSocket ssk = new ServerSocket(PORT);

            while (true) {
                Socket sk = ssk.accept();
                Enviador ef = new Enviador(sk, "foto.png"); //posar el fitxer a enviar, podem posar-lo amb la ruta al fitxer  
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class Enviador extends Thread {

        Socket sk;
        String nomfich;

        //per llegir el fitxer
        FileInputStream fileInput;
        BufferedInputStream bi;
        File fi;

        //pel Socket
        DataOutputStream dos;
        DataInputStream dis;

        Enviador(Socket sk, String nomfich) {
            this.sk = sk;
            this.nomfich = nomfich;
            start(); //comenzará el run
        }

        @Override
        public void run() {
            enviaEnBlocs(); //envía en bloques
        }

        void enviaEnBlocs() {

            try {
                sk.setSoLinger(true, 60);
                dos = new DataOutputStream(sk.getOutputStream());
                dis = new DataInputStream(sk.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            String desti = sk.getRemoteSocketAddress().toString();
            desti = desti.split(":")[0]; //me quedo solo con la ip
            
            System.out.println(desti + ": connectat");
            String motiu = null;
            synchronized (connectats) { //comprova quan fa que s'ha connectat
                Long quan = (Long) connectats.get(desti);
                long ara = System.currentTimeMillis();
                if (quan == null) { //es nou
                    connectats.put(desti, new Long(ara));
                } else {
                    if (ara - quan < 10000) { //fa molt poc que es va connectar
                        motiu = " Fa molt poc que et vas connectar";
                    } else { //ha passat temps
                        connectats.put(desti, new Long(ara)); //actualitza temps
                    }
                }
            }

            try {
                if (motiu != null) {
                    System.out.println(desti + motiu );
                    dos.writeUTF(motiu);//(dada 1)
                    dos.write(0); //indica que s'ha acabat (dada 2)
                    dos.close();
                    sk.close();
                    return;
                } else { //tot bé
                    dos.writeUTF("S'enviará el fitxer " + nomfich);
                    dos.write(1); //indica que tot bé
                }
            } catch (IOException ex) {
                Logger.getLogger(EnviaFitxerServidor.class.getName()).log(Level.SEVERE, null, ex);
            }

            int lbloc = 1024; //tamany del bloc --> sol ser múltiple de 256 bytes, però pot ser qualsevol valor
            // Aquest tamany no cal que sigui el mateix en el emisor que en el receptor.

            try {
                fi = new File(nomfich);
                fileInput = new FileInputStream(fi);
                bi = new BufferedInputStream(fileInput);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            long lfic = fi.length();

            try {

                //espera 60 segons al tancar el socket en espera que l'altre extrem rebi la solicitut de finalitzar la sesió
                // si no es posa, pot pasar que es tanqui la sesió i en l'altra extrem hi hagi un error per falta de conexió. No seria greu si està gestionat
                dos.writeUTF(nomfich);//
                dos.writeLong(lfic);
                // oos.flush(); //envía el buffer, pero no cal, doncs continuem escrivint en oos

                long veces = lfic / lbloc; //quants blocs s'han d'enviar
                int resto = (int) (lfic % lbloc); //quant querarà al final per enviar

                byte b[] = new byte[lbloc];

                for (long i = 0; i < veces; i++) {
                    bi.read(b); //llegeix un tros del fitxer
                    dos.write(b); // envia el tros del fitxer
                    System.out.println(desti + ": enviat el tros " + i + " portem enviats " + (i + 1) * lbloc + " bytes");
                }
                //envia la resta del fitxer
                if (resto > 0) {
                    bi.read(b, 0, resto); // llgeix la resta del fitxer en b
                    dos.write(b, 0, resto); // l'enviem
                    System.out.println(desti + ": Enviem els " + resto + " bytes restants");
                }
                //oos.flush(); //no cal, es fa un flush al fer el close de oos
                System.out.println(desti + ": Enviat tot el fitxer");
                dos.close();
//                dis.readByte(); //espera a que el destí envïi qualsevol cosa
                sk.close();
                System.out.println(desti + ": Socket tancat");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
