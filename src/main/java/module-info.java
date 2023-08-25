module com.kldv {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.prefs;
    requires java.logging;

    opens com.kldv to javafx.fxml;

    exports com.kldv;
}
