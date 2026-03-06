package com.industria.termopac.facturacion;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("facturacion-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        stage.setScene(scene);
        stage.show();

        try {

            Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png")));
            stage.getIcons().add(icon);
        } catch (NullPointerException e) {
            System.err.println("No se encontró el archivo logo.png en la carpeta resources.");

        } catch (Exception e) {
            System.err.println("Error al cargar el logo de la ventana: " + e.getMessage());
        }

        stage.setTitle("Termopac - Sistema Integral de Facturación e Inventario");
        stage.setScene(scene);

        stage.centerOnScreen();

        stage.show();
    }
}
