package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class CourseDetailController {

    @FXML
    private StackPane contentPane; // StackPane nơi nội dung các tab sẽ được hiển thị

    @FXML
    private Button overviewTabButton;
    @FXML
    private Button curriculumTabButton;
    @FXML
    private Button reviewsTabButton;

    // Các biến @FXML cho các nút trong Navbar
    private Button currentSelectedTab; // Nút tab đang được chọn

    @FXML
    public void initialize() {
        // Khởi tạo, đảm bảo tab Overview được chọn mặc định
        selectTab(overviewTabButton, "course-overview.fxml");
    }

    @FXML
    private void handleTabSelection(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String fxmlFileName = "";

        if (clickedButton == overviewTabButton) {
            fxmlFileName = "course-overview.fxml";
        } else if (clickedButton == curriculumTabButton) {
            fxmlFileName = "course-curriculum.fxml"; // Tạo file FXML này sau
        } else if (clickedButton == reviewsTabButton) {
            fxmlFileName = "course-reviews.fxml"; // Tạo file FXML này sau
        }

        selectTab(clickedButton, fxmlFileName);
    }

    private void selectTab(Button button, String fxmlFileName) {
        // Bỏ chọn tab cũ
        if (currentSelectedTab != null) {
            currentSelectedTab.getStyleClass().remove("selected-tab");
        }

        // Chọn tab mới
        button.getStyleClass().add("selected-tab");
        currentSelectedTab = button;

        // Load nội dung FXML tương ứng vào contentPane
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/courseecom/courseecom/view/" + fxmlFileName));
            Pane content = loader.load();
            contentPane.getChildren().setAll(content); // Thay thế toàn bộ nội dung cũ
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi khi tải nội dung tab: " + fxmlFileName);
        }
    }
}