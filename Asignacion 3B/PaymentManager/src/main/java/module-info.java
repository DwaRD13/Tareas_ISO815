module com.fundapec.payment.management.paymentmanager {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;

    opens com.fundapec.payment.management to javafx.fxml;
    exports com.fundapec.payment.management;
}