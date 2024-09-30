package com.astier.bts.client_tcp_prof;

import com.astier.bts.client_tcp_prof.tcp.TCP;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.math.BigInteger;
import java.net.*;
import java.util.ResourceBundle;
import static javafx.scene.paint.Color.*;

public class HelloController implements Initializable {
    public Button button;
    public Button connecter;
    public Button deconnecter;
    public TextField TextFieldIP;
    public TextField TextFieldPort;
    public TextField TextFieldRequette;
    public Circle voyant;
    public TextArea TextAreaReponses;
    static public TCP tcp;
    static boolean enRun = false;
    String adresse;
    String port;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        voyant.setFill(RED);

        button.setOnMouseClicked(event -> {
            try {
                envoyer();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        connecter.setOnMouseClicked(event -> {
            try {
                connecter();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        deconnecter.setOnMouseClicked(event -> {
            try {
                deconnecter();
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        });

        try {
            setServeurUDP();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void setServeurUDP() throws IOException, InterruptedException {
        InetAddress inetAddress = InetAddress.getByName("224.0.0.250");
        int portTCP = 5555;
        int portUDP = 5556;
        byte ttl = 60;
        byte[] data = "Tu es qui?".getBytes();
        byte[] bufferResponse = new byte[512];
        MulticastSocket multicastSocket = new MulticastSocket();

        Response response = new Response(inetAddress, portTCP, portUDP);
        DatagramSocket socketUDP = new DatagramSocket(response.port_UDP());
        DatagramPacket datagramPacket1 = new DatagramPacket(bufferResponse, bufferResponse.length);
        System.out.println("Attente reponse...");

        new Thread(() -> {
            try {
                socketUDP.receive(datagramPacket1);
                System.out.println("Reponse UDP");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

        multicastSocket.setTimeToLive(ttl);
        DatagramPacket datagramPacket2 = new DatagramPacket(data, data.length, response.url(), response.port_TCP());
        multicastSocket.send(datagramPacket2);
        Thread.sleep(100);
        multicastSocket.close();

        recupererDesParametres(datagramPacket1);
    }

    private void recupererDesParametres(DatagramPacket dp) {
        String[] paramConfig;
        System.out.println("Traitement reponse");
        String s = new String(dp.getData(), 0, dp.getLength());
        paramConfig = s.split(";");
        System.out.println(
                "URL: " + paramConfig[0]
                        + "; TCP port: " + paramConfig[1]
                        + "; UDP port: " + paramConfig[2]
        );

        TextFieldIP.setText(paramConfig[0]);
        TextFieldPort.setText(paramConfig[1]);
    }

    private void envoyer() throws IOException, InterruptedException {
        tcp.requette(TextFieldRequette.getText());
    }

    private void deconnecter() throws InterruptedException, IOException {
        if (enRun) {
            enRun = false;
            tcp.deconnection();
            voyant.setFill(RED);
        }
    }

    private void connecter() throws IOException, InterruptedException {
        adresse = TextFieldIP.getText();
        port = TextFieldPort.getText();
        if (!adresse.isEmpty() && !port.isEmpty()) {
            tcp = new TCP(InetAddress.getByName(adresse), Integer.parseInt(port), this);
            tcp.connection();
            voyant.setFill(LIME);
            enRun = true;
        }
    }
}