package com.astier.bts.client_tcp_prof.diffieHellman;

import com.astier.bts.client_tcp_prof.tcp.TCP;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

public class DiffieHellman {
    BigInteger a, p, g, B, A, K;
    TCP tcp;
    int taille;

    public DiffieHellman(TCP tcp, int taille) {
        this.tcp = tcp;
        this.taille = taille;
        a = new BigInteger(taille, new SecureRandom());
        p = BigInteger.probablePrime(taille, new SecureRandom());
        do {
            g = new BigInteger(taille, new SecureRandom());
        } while (g.compareTo(p) > 0);
    }

    public byte[] recuperation() throws IOException, InterruptedException {
        byte[] bytes_B = new byte[65535];
        tcp.out.write(p.toByteArray());
        Thread.sleep(100);
        tcp.out.write(g.toByteArray());
        Thread.sleep(100);
        A = g.modPow(a, p);
        tcp.out.write(A.toByteArray());

        int taille_B = tcp.in.read(bytes_B);
        B = new BigInteger(Arrays.copyOfRange(bytes_B, 0, taille_B));
        K = B.modPow(a, p);
        System.out.println(K);
        return K.toByteArray();
    }
}