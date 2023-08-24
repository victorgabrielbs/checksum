module com.kldv {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.prefs;

    opens com.kldv to javafx.fxml;

    exports com.kldv;
}
