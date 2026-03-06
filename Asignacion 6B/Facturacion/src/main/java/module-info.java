module com.industria.termopac.facturacion {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires javafx.base;

    opens com.industria.termopac.facturacion to javafx.fxml;
    exports com.industria.termopac.facturacion;
}