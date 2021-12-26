/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.sockets.exercici1;

import com.mycompany.sockets.RepFitxerClient;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author VicM
 */
public class Client_ex1 {
    static String ip = "localhost";//localhost
    static int puerto = 12345;
    
    static final int METODE_REBRE = 2; //1: tot de cop; 2: en blocs

    Socket sk;
    static FileOutputStream fileOutput;
    static BufferedOutputStream bo;
    static File fo;
    static InputStream in;
    static String nomfich;


    
    public static void main(String[] args) {
        Scanner scan = new Scanner( System.in );
        
        try {
            Socket sk = new Socket(ip, puerto);
            sk.setSoLinger(true, 50);
            DataOutputStream dos = new DataOutputStream( sk.getOutputStream());
            DataInputStream dis = new DataInputStream(sk.getInputStream());
            
            dos.writeUTF(" Hola, soy Vic. ");
            String str = dis.readUTF();
            System.out.println(" > Respuesta del servidor : " + str);
            
            boolean sigue = true;
            while(sigue){ //sigue aceptando Entrada de usuario mientras sea true
                
                System.out.println("Escribe...  ('out' termina la conexion)");
                str = scan.nextLine();
                
                dos.writeUTF(str);//envia entrada del usuario al servidor
                String resp;
                
                if( str.equals("lista") ){ // [ LISTA ]
                    resp = dis.readUTF();
                    System.out.println(" > El servidor responde : "+ resp);
                   do{ 
                       resp = dis.readUTF();
                       if(resp.equals("")) break;
                       System.out.println(" > "+ resp);   
                       
                   } while(!resp.equals(""));
                   
                }else if( str.equals("help") ){  // [ HELP ]
                    do{ 
                       resp = dis.readUTF();
                       if(resp.equals("")) break;
                       System.out.println(" > "+ resp); 
                       
                   } while(resp.equals(""));
                    
                }else if( str.startsWith("demana")){ // [ DEMANA..N]
                    dos.writeUTF(str);
    
                    int lbloc = 512; //no cal que sigui el mateix tamany en el emisor i receptor

                    in = sk.getInputStream();
                    DataInputStream dism = new DataInputStream(in);

                    String missatge = dis.readUTF();//(dada 1)
                    Byte segueix = dis.readByte();//dada 2
                    System.out.println("Rebut: " +  missatge );
                    if( segueix == 0 ){ //es tallarà
                        sk.close();
                        return;
                    }    
                    
                    nomfich = dis.readUTF();

                    String s[] = nomfich.split("[\\\\/]"); //per si acàs, treiem la ruta del nom del fitxer, per si s'ha posat
                    nomfich = s[s.length - 1];

                    String nomfichPrevi = "rebrent_" + nomfich; //El nom es canvia per saber que el fitxer encara no s'ha baixat del tot
                    long lfic = dis.readLong();

                    fo = new File(nomfichPrevi);
                    fo.delete(); //Eliminem el fitxer per si ja existia d'abans
                    fileOutput = new FileOutputStream(fo);
                    bo = new BufferedOutputStream(fileOutput);
                    System.out.println("El fitxer ocuparà " + lfic + " bytes");
                    
                    byte b[] = new byte[(int) lbloc];

                    long lleva = 0;
                    while (lleva < lfic) {
                        int leido;
                        if (lfic - lleva > lbloc) {
                            leido = in.read(b, 0, lbloc); //llegeix com al molt lbloc bytes, però pot ser que sigui altra quantitat menor
                        } else {//falten menys bytes que lbloc
                            leido = in.read(b, 0, (int) (lfic - lleva)); //llegeix com a molt tants bytes com falten
                        }
                        bo.write(b, 0, leido);
                        lleva = lleva + leido; //per saber quants es porten llegits
                        System.out.println("Bytes rebuts: " + leido + " portem: " + lleva + " bytes");
                    }

                    bo.close();
                    //reanomena el fitxer
                    File nufile = new File("rec_" + nomfich); //El fitxer ja està baixat. Se li ha de posar el nom final correcte. No li posem el que s'envia per si s'està provant al mateix ordinador
                    nufile.delete();
                    fo.renameTo(nufile);
                    
                }else if( str.equals("out") ) break; // [ OUT ] 
                
                resp = dis.readUTF();// espera respuesta del servidor
                System.out.println(resp);

            }
            
            sk.close();
            
        } catch (IOException ex) {
            Logger.getLogger(Client_ex1.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

}
