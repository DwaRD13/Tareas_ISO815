package com.fundapec.payment.management;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("aprobacionPayment-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 750);
        stage.setTitle("FUNDAPEC - Aprobacion pagos");
        stage.setScene(scene);
        stage.show();
    }
}
