module com.procode.visor_factura {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;

    opens com.procode.visor_factura to javafx.fxml;
    exports com.procode.visor_factura;
}