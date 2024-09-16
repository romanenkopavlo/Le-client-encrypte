package com.astier.bts.client_tcp_prof;

import com.astier.bts.client_tcp_prof.tcp.TCP;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
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

    // ip: 10.0.0.175
    // port: 7297

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

            } catch (IOException e) {
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

    private void connecter() throws IOException {
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