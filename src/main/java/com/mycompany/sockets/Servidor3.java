/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.sockets;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author icop
 */
public class Servidor3 {
    static int puerto = 12345;

    public static void main(String[] args) {
        try {
            ServerSocket ssk = new ServerSocket(puerto);
            while (true) {
                Socket sk = ssk.accept();
                Servidor servidor = new Servidor(sk);
                servidor.start();
            }
        } catch (IOException ex) {
            Logger.getLogger(Servidor3.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    static class Servidor extends Thread {

        Socket sk;

        Servidor(Socket sk) {
            this.sk = sk;
        }

        @Override
        public void run() {
            InputStream is;
            
            try {
                sk.setSoLinger(true, 50); //permet desconnexió "amable"
                is = sk.getInputStream();
                System.out.println("Se ha conectado un cliente: " + sk.getInetAddress().getHostAddress());

                DataOutputStream dos = new DataOutputStream(sk.getOutputStream());
                DataInputStream dis = new DataInputStream(sk.getInputStream());

                String st = dis.readUTF(); //dos.writeUTF( "Hola, soy Iván");
                System.out.println("Se ha conectado: " + st);
                dos.writeUTF("soy un servidor automático");  //String st = dis.readUTF();

                boolean sigue = true;
                while (sigue) {
                    st = dis.readUTF(); //dos.writeUTF( "Hola, soy Iván");
                    System.out.println("me dicen: " + st);
                    dos.writeUTF("me has dicho: " + st);
                    if( st.equals("q") ) break; //amb una "q" es surt
                }
                System.out.println("El client ha demanat desconectar");
                sk.close();
                
            } catch (IOException ex) {
                Logger.getLogger(Servidor3.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }

}
