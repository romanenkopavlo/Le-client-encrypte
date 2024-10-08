/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.astier.bts.client_tcp_prof.tcp;


import com.astier.bts.client_tcp_prof.HelloController;
import com.astier.bts.client_tcp_prof.aes.Aes_cbc;
import com.astier.bts.client_tcp_prof.diffieHellman.DiffieHellman;
import javafx.application.Platform;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


import static javafx.scene.paint.Color.RED;

/**
 * @author Michael
 */
public class TCP extends Thread {
    int port;
    InetAddress serveur;
    Socket socket;
    boolean marche = false;
    static String motDePasse;
    static String iv;
    Aes_cbc aesCbc;
    byte [] motDePasseBytes;
    byte [] ivBytes;
    byte [] tempTable = new byte[65535];
    public OutputStream out;
    public InputStream in;
    DiffieHellman diffieHellman;
    public byte[] cles;
    HelloController fxmlCont;

    public TCP() {
    }

    public TCP(InetAddress serveur, int port, HelloController fxmlCont) {
        this.port = port;
        this.serveur = serveur;
        this.fxmlCont = fxmlCont;
        System.out.println("@ serveur: " + serveur + " port: " + port);
    }

    public void connection() throws IOException, InterruptedException {
        if (!this.isAlive()) {
            socket = new Socket(serveur, port);
            out = socket.getOutputStream();
            in = socket.getInputStream();


            diffieHellman = new DiffieHellman(this, 1024);
            cles = diffieHellman.recuperation();
            motDePasseBytes = Arrays.copyOfRange(cles, 1, 17);
            ivBytes = Arrays.copyOfRange(cles, 17, 33);

            aesCbc = new Aes_cbc(motDePasseBytes, ivBytes);
            this.start();
            marche = true;
        }
    }

    public void deconnection() throws IOException {
        updateMessage("EXIT");
        marche = false;
        out.close();
        in.close();
        socket.close();
    }

    public void requette(String laRequette) throws IOException {
        if (laRequette.equalsIgnoreCase("exit")) {
            laRequette = laRequette.trim();
            byte [] requetteEncrypte = aesCbc.cryptage(laRequette.getBytes());
            out.write(requetteEncrypte);
            deconnection();
            fxmlCont.voyant.setFill(RED);
        } else {
            laRequette = laRequette + "\n";
            byte [] requetteEncrypte = aesCbc.cryptage(laRequette.getBytes());
            out.write(requetteEncrypte);
            System.out.println("La requette encryptee " + laRequette);
        }
    }

    public void run() {
        while (marche) {
            try {
                String message;
                int temp = in.read(tempTable);
                byte[] tableADecrypter = Arrays.copyOfRange(tempTable, 0, temp);
                byte[] decryptedResponse = aesCbc.decryptage(tableADecrypter);
                message = new String(decryptedResponse, StandardCharsets.UTF_8);
                updateMessage(message);
            } catch (IOException ignored) {}
        }
    }

    protected void updateMessage(String message) {
        Platform.runLater(() -> fxmlCont.TextAreaReponses.appendText("    MESSAGE SERVEUR >  \n      " + message + "\n"));
    }
}