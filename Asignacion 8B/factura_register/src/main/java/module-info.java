module com.procode.factura_register {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires java.desktop;
    requires javafx.swing;

    opens com.procode.factura_register to javafx.fxml;
    exports com.procode.factura_register;
}