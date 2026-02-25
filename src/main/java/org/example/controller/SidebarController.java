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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.util.Navigation; // Import class Navigation "thần thánh"
import org.example.util.UserSession;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SidebarController implements Initializable {

    @FXML private StackPane sidebarRoot;
    @FXML private VBox sidebarPane;
    @FXML private Button toggleBtn;

    // Các nút Menu
    @FXML private Button homeBtn;
    @FXML private Button myCoursesBtn;
    @FXML private Button shopBtn;
    @FXML private Button statisticsBtn;
    @FXML private Button settingsBtn;
    @FXML private Button newsBtn;
    @FXML private Button logoutBtn;

    private boolean isSidebarOpen = true;


    private StackPane mainContentArea;

    private final double OPEN_WIDTH = 260;
    private final double CLOSED_WIDTH = 50;

    public void setMainContentArea(StackPane contentArea) {
        this.mainContentArea = contentArea;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Setup giao diện Sidebar (Clip, Size...)
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(sidebarRoot.widthProperty());
        clip.heightProperty().bind(sidebarRoot.heightProperty());
        sidebarRoot.setClip(clip);

        sidebarRoot.setPrefWidth(OPEN_WIDTH);
        sidebarRoot.setMinWidth(OPEN_WIDTH);
        sidebarRoot.setMaxWidth(OPEN_WIDTH);
        toggleBtn.setStyle("-fx-background-radius: 5 0 0 5;");
    }

    @FXML
    public void onToggleSidebar(ActionEvent event) {
        Timeline timeline = new Timeline();

        if (isSidebarOpen) {
            // Đóng Sidebar
            KeyFrame keyFrame = new KeyFrame(Duration.millis(250),
                    new KeyValue(sidebarRoot.prefWidthProperty(), CLOSED_WIDTH),
                    new KeyValue(sidebarRoot.minWidthProperty(), CLOSED_WIDTH),
                    new KeyValue(sidebarRoot.maxWidthProperty(), CLOSED_WIDTH)
            );
            timeline.getKeyFrames().add(keyFrame);
            timeline.setOnFinished(e -> {
                sidebarPane.setVisible(false);
                sidebarPane.setManaged(false);
                toggleBtn.setText("❯");
                toggleBtn.setStyle("-fx-background-radius: 0 5 5 0;");
            });
        } else {
            // Mở Sidebar
            sidebarPane.setVisible(true);
            sidebarPane.setManaged(true);
            KeyFrame keyFrame = new KeyFrame(Duration.millis(250),
                    new KeyValue(sidebarRoot.prefWidthProperty(), OPEN_WIDTH),
                    new KeyValue(sidebarRoot.minWidthProperty(), OPEN_WIDTH),
                    new KeyValue(sidebarRoot.maxWidthProperty(), OPEN_WIDTH)
            );
            timeline.getKeyFrames().add(keyFrame);
            timeline.setOnFinished(e -> {
                toggleBtn.setText("❮");
                toggleBtn.setStyle("-fx-background-radius: 5 0 0 5;");
            });
        }
        timeline.play();
        isSidebarOpen = !isSidebarOpen;
    }

    // --- CÁC HÀM CHUYỂN TRANG DÙNG NAVIGATION ---

    @FXML
    public void onHomeClick(ActionEvent event) {
        // Gọi Navigation: Tự động hiện Loading -> Fade -> Hiện trang Home
        Navigation.to(event, Navigation.HOME_VIEW);
        updateActiveButton(homeBtn);
    }

    @FXML
    public void onShopClick(ActionEvent event) {
        Navigation.to(event, Navigation.COURSES_VIEW);
        updateActiveButton(shopBtn);
    }

    @FXML
    public void onNewsClick(ActionEvent event) {
        Navigation.to(event, Navigation.NEWS_VIEW);
        updateActiveButton(newsBtn);
    }

    @FXML
    public void onMyCoursesClick(ActionEvent event) {
        Navigation.to(event, Navigation.MY_COURSES_VIEW);
        updateActiveButton(myCoursesBtn);
    }

    @FXML
    public void onSettingsClick(ActionEvent event) {
        Navigation.to(event, Navigation.SETTINGS_VIEW);
        updateActiveButton(settingsBtn);
    }

    @FXML
    public void onLogoutClick(ActionEvent event) {

        UserSession.getInstance().cleanUserSession();

        try {
            // Lưu ý: File login.fxml của anh nằm ngay thư mục gốc resources, nên dùng "/login.fxml"
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
            System.out.println(">>> Đã đăng xuất thành công!");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Không tìm thấy file login.fxml");
        }
    }

    // --- HÀM STYLE CHO NÚT ---
    private void updateActiveButton(Button activeBtn) {
        resetButtonStyle(homeBtn);
        resetButtonStyle(newsBtn);
        resetButtonStyle(myCoursesBtn);
        resetButtonStyle(shopBtn);
        resetButtonStyle(statisticsBtn);
        resetButtonStyle(settingsBtn);

        if (activeBtn != null) {
            activeBtn.getStyleClass().add("nav-btn-active");
        }
    }

    private void resetButtonStyle(Button btn) {
        if (btn != null) {
            btn.getStyleClass().remove("nav-btn-active");
        }
    }

    @FXML
    public void onStatisticsClick(ActionEvent event) {
        Navigation.to(event, Navigation.STATISTICS_VIEW);
        updateActiveButton(statisticsBtn);
    }
}