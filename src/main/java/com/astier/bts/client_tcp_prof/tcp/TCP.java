/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.astier.bts.client_tcp_prof.tcp;


import com.astier.bts.client_tcp_prof.HelloController;
import com.astier.bts.client_tcp_prof.aes.Aes_cbc;
import com.astier.bts.client_tcp_prof.aes.Outils;
import javafx.application.Platform;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;


import static javafx.scene.paint.Color.RED;

/**
 * @author Michael
 */
public class TCP extends Thread {
    int port;
    InetAddress serveur;
    Socket socket;
    boolean marche = false;
    final static String motDePasse = "abcdefgh12345678";
    final static String iv = "hijklmnopqrstuv";
    Aes_cbc aesCbc;
    byte [] motDePasseBytes;
    byte [] ivBytes;
    PrintStream out;
    BufferedReader in;
    HelloController fxmlCont;

    public TCP() {
    }

    public TCP(InetAddress serveur, int port, HelloController fxmlCont) {
        this.port = port;
        this.serveur = serveur;
        this.fxmlCont = fxmlCont;

        this.motDePasseBytes = Outils.normalizeChaine(motDePasse, 16);
        this.ivBytes = Outils.normalizeChaine(iv, 16);
        this.aesCbc = new Aes_cbc(motDePasseBytes, ivBytes);

        System.out.println("@ serveur: " + serveur + " port: " + port);
    }

    public void connection() throws IOException {
        if (!this.isAlive()) {
            socket = new Socket(serveur, port);
            out = new PrintStream(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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

    public void requette(String laRequette) throws IOException, InterruptedException {
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
                byte[] requetteEncrypte = in.readLine().getBytes();
                byte[] decryptedResponse = aesCbc.decryptage(requetteEncrypte);
                message = new String(decryptedResponse);
                updateMessage(message);
            } catch (IOException ignored) {}
        }
    }

    protected void updateMessage(String message) {
        Platform.runLater(() -> fxmlCont.TextAreaReponses.appendText("    MESSAGE SERVEUR >  \n      " + message + "\n"));
    }
}