package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.example.dao.UserDao;
import org.example.model.User;

import java.awt.event.ActionEvent;
import java.io.IOException;

public class ResetPasswordController {
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button saveButton;
    @FXML
    private Hyperlink backToLoginButton;

    private UserDao userDao = new UserDao();
    private User targetUser;
    @FXML

    public void setTargetUser(User user) {
        this.targetUser = user;
    }

    @FXML
    public void onSaveButtonClick(ActionEvent event) {
        String newPass = newPasswordField.getText().trim();
        String confirmPass = confirmPasswordField.getText().trim();

        // Kiểm tra dữ liệu đầu vào
        if (newPass.isEmpty() || confirmPass.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        if (newPass.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Mật khẩu yếu", "Mật khẩu phải có ít nhất 6 ký tự!");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu xác nhận không khớp!");
            return;
        }

        if (targetUser != null) {
            boolean isUpdated = userDao.updatePassword(targetUser.getEmail(), newPass);

            if (isUpdated) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.");
                switchToLogin();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể cập nhật mật khẩu. Vui lòng thử lại.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy thông tin người dùng!");
        }
    }

    private void switchToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Đăng Nhập");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải màn hình đăng nhập: " + e.getMessage());
        }
    }

    @FXML
    public void onBackToLoginClick(ActionEvent event) {
        switchToLogin();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
