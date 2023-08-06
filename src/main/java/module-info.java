module com.kldv {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;

    opens com.kldv to javafx.fxml;

    exports com.kldv;
}
