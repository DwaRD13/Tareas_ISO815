module com.unapec.nominagenerator {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires javafx.graphics;
    requires java.desktop;
    requires java.sql;
    requires javafx.base;
    requires java.net.http;
    requires com.google.gson;

    opens com.unapec.entity to com.google.gson;
    opens com.unapec.nominagenerator to javafx.fxml, com.google.gson;

    exports com.unapec.nominagenerator;
}