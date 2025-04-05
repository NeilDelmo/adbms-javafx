module com.mycompany.karaoke_rental_system {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires java.sql;

    opens com.mycompany.karaoke_rental_system to javafx.fxml;
    exports com.mycompany.karaoke_rental_system;
    requires jbcrypt;
    requires fontawesomefx;
    
    
}
