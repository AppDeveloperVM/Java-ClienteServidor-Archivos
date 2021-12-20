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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author icop
 */
public class Cliente3 {
    static String ip = "192.168.3.2"; //localhost
    static int puerto = 12345;
    
    public static void main(String[] args) {
        Scanner scan = new Scanner( System.in );
        try {
            Socket sk = new Socket( ip, puerto );
            sk.setSoLinger(true, 50); //permet desconnexió "amable"
            DataOutputStream dos = new DataOutputStream( sk.getOutputStream() );
            DataInputStream dis = new DataInputStream( sk.getInputStream() );

            dos.writeUTF( "Hola, soc Vic");
            String st = dis.readUTF();
            System.out.println("El servidor és: " + st );
            
            boolean sigue = true;
            while(sigue){
                System.out.println("Escriu alguna cosa (només \"q\" per sortir): ");
                st = scan.nextLine();
                dos.writeUTF(st);
                String resp = dis.readUTF();
                System.out.println("em respon: " + resp );
                if( st.equals("q") ) break;
            }
            
            sk.close();
        } catch (IOException ex) {
            Logger.getLogger(Cliente3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

