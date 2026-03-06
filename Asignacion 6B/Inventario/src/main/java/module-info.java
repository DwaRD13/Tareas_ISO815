module com.industria.termopac.inventario {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires javafx.base;

    opens com.industria.termopac.inventario to javafx.fxml;
    exports com.industria.termopac.inventario;
}