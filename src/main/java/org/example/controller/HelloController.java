package org.example.controller;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.util.UserSession;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class HelloController implements Initializable {

    @FXML private Pane sidebar;
    @FXML private StackPane contentArea;
    @FXML private Button btnOpenSidebar;
    @FXML private Button logoutBtn;
    @FXML private Button settingsBtn;
    @FXML
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        sidebar.setPrefWidth(260);

        logoutBtn.setOnAction(this::handleLogout);
        settingsBtn.setOnAction(this::handleSettings);
    }

    private void handleLogout(ActionEvent event) {
        UserSession.getInstance().cleanUserSession();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

            System.out.println("Đăng xuất thành công!");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Không tìm thấy file login.fxml");
        }
    }

    private void handleSettings(ActionEvent event) {
        try {
            Parent settingsView = FXMLLoader.load(getClass().getResource("/View/settings.fxml"));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(settingsView);

            System.out.println("Đã mở Cài đặt.");
        } catch (IOException e) {
            System.err.println("Chưa có file settings.fxml, đang hiển thị thông báo tạm.");
        }
    }

    @FXML
    public void closeSidebar(ActionEvent event) {
        Timeline timeline = new Timeline();

        KeyValue kvWidth = new KeyValue(sidebar.prefWidthProperty(), 0);
        KeyValue kvMinWidth = new KeyValue(sidebar.minWidthProperty(), 0);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(300), e -> {
            sidebar.setVisible(false);
            sidebar.setManaged(false);

            btnOpenSidebar.setVisible(true);
            btnOpenSidebar.setManaged(true);
        }, kvWidth, kvMinWidth);

        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    @FXML
    public void openSidebar(ActionEvent event) {
        sidebar.setVisible(true);
        sidebar.setManaged(true);
        btnOpenSidebar.setVisible(false);
        btnOpenSidebar.setManaged(false);
        Timeline timeline = new Timeline();
        KeyValue kvWidth = new KeyValue(sidebar.prefWidthProperty(), 260);
        KeyValue kvMinWidth = new KeyValue(sidebar.minWidthProperty(), 260);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(300), kvWidth, kvMinWidth);

        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }
}