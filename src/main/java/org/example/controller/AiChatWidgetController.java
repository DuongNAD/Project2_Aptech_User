package org.example.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import org.example.dao.CourseDao;
import org.example.model.Course;
import org.example.util.GeminiApiUtil;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AiChatWidgetController implements Initializable {

    @FXML
    private StackPane widgetRoot;
    @FXML
    private VBox chatPopup;
    @FXML
    private Button fabButton;

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox messagesContainer;

    @FXML
    private TextField txtInput;
    @FXML
    private Button btnSend;

    private CourseDao courseDao = new CourseDao();
    private boolean isFirstTime = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        
        org.example.util.ScrollUtil.applySmoothScrolling(scrollPane);

        
        chatPopup.setVisible(false);
        chatPopup.setManaged(false);
        fabButton.setVisible(true);
        fabButton.setManaged(true);
    }

    @FXML
    void handleOpenChat(ActionEvent event) {
        fabButton.setVisible(false);
        fabButton.setManaged(false);

        chatPopup.setVisible(true);
        chatPopup.setManaged(true);

        if (isFirstTime) {
            addAiMessage("Xin chào! Tôi là Trợ lý AI của EduPath. Bạn muốn tôi tư vấn khóa học nào hôm nay?");
            isFirstTime = false;
        }

        scrollToBottom();
    }

    @FXML
    void handleCloseChat(ActionEvent event) {
        chatPopup.setVisible(false);
        chatPopup.setManaged(false);

        fabButton.setVisible(true);
        fabButton.setManaged(true);
    }

    @FXML
    void handleSend(ActionEvent event) {
        String msg = txtInput.getText().trim();
        if (msg.isEmpty())
            return;

        txtInput.clear();
        btnSend.setDisable(true);

        
        addUserMessage(msg);

        
        javafx.scene.Node loadingNode = addAiMessage("Đang suy nghĩ...");

        
        new Thread(() -> {
            try {
                
                List<Course> availableCourses = courseDao.getAllCourses();
                StringBuilder contextBuilder = new StringBuilder();
                contextBuilder
                        .append("Bạn là nhân viên tư vấn khóa học chuyên nghiệp của nền tảng học trực tuyến EduPath. ");
                contextBuilder.append("Dưới đây là danh sách các khóa học hiện có trong hệ thống:\n");

                for (Course c : availableCourses) {
                    contextBuilder.append("- Khóa: ").append(c.getTitle())
                            .append(" (Giá: ").append(String.format("%,.0f", c.getPrice())).append(" VND").append(")\n")
                            .append("  Danh mục: ").append(c.getCategoryName()).append("\n")
                            .append("  Mô tả: ").append(c.getDescription()).append("\n\n");
                }

                contextBuilder.append("Người dùng hỏi: \"").append(msg).append("\"\n");
                contextBuilder.append(
                        "Hãy trả lời người dùng một cách ngắn gọn, lịch sự và thân thiện bằng tiếng Việt, tư vấn dựa vào danh sách trên.");

                String result = GeminiApiUtil.getGeminiResponse(contextBuilder.toString());

                Platform.runLater(() -> {
                    
                    messagesContainer.getChildren().remove(loadingNode);

                    
                    addAiMessage(result);
                    btnSend.setDisable(false);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    messagesContainer.getChildren().remove(loadingNode);
                    addAiMessage("Xin lỗi, tôi gặp sự cố kỹ thuật. Hãy thử lại!");
                    btnSend.setDisable(false);
                });
            }
        }).start();
    }

    private void addUserMessage(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("msg-user");
        lbl.setWrapText(true);
        lbl.setMaxWidth(260); 

        HBox row = new HBox(lbl);
        row.setAlignment(javafx.geometry.Pos.CENTER_RIGHT); 

        messagesContainer.getChildren().add(row);
        scrollToBottom();
    }

    private javafx.scene.Node addAiMessage(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("msg-ai");
        lbl.setWrapText(true);
        lbl.setMaxWidth(260);

        HBox row = new HBox(lbl);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        messagesContainer.getChildren().add(row);
        scrollToBottom();

        return row;
    }

    private void scrollToBottom() {
        
        Platform.runLater(() -> {
            scrollPane.applyCss();
            scrollPane.layout();
            scrollPane.setVvalue(1.0);
        });
    }
}
