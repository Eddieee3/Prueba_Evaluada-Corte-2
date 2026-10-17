module com.papeleria {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;

    opens com.papeleria to javafx.fxml, javafx.base;
    exports com.papeleria;
}
