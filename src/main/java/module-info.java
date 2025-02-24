module com.genomicslab {
    requires javafx.fxml;
    requires java.sql;
    requires java.smartcardio;
    requires de.jensd.fx.glyphs.fontawesome;
    requires com.jfoenix;
    requires itextpdf;
    requires animatefx;


//    opens com.genomicslab to javafx.fxml;
//    exports com.genomicslab;
    exports com.genomicslab.launcher;
    opens com.genomicslab.launcher to javafx.fxml;
    exports com.genomicslab.controllers;
    opens com.genomicslab.controllers to javafx.fxml;
}