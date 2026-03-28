package com.procode.visor_factura;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.File;

public class VisorFacturaApp extends Application {
    private String noFacturaParametro;

    @Override
    public void init() {
        var params = getParameters().getRaw();
        this.noFacturaParametro = params.isEmpty() ? null : params.get(0);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Visor de Digitalización - ProCode");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setStyle("-fx-background-color: #f4f4f4;");

        Label lblTitulo = new Label("Consulta de Documento Digital");
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label lblInfo = new Label();
        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(750);

        if (noFacturaParametro != null) {
            lblInfo.setText("Mostrando factura No: " + noFacturaParametro);

            File file = new File("digitalizaciones/" + noFacturaParametro + ".png");
            String rutaImagen = file.toURI().toString();

            try {
                Image img = new Image(rutaImagen);
                if (img.isError()) throw new Exception();
                imageView.setImage(img);
            } catch (Exception e) {
                lblInfo.setText("No se encontró el archivo en: " + file.getAbsolutePath());
                lblInfo.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        } else {
            lblInfo.setText("⚠️ No se recibió parámetro de factura.");
        }

        ScrollPane scrollPane = new ScrollPane(imageView);
        layout.getChildren().addAll(lblTitulo, lblInfo, scrollPane);

        primaryStage.setScene(new Scene(layout, 820, 650));
        primaryStage.show();
    }

    public static void main(String[] args) { launch(args); }
}