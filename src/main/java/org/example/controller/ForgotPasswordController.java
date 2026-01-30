package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.dao.UserDao;
import org.example.dao.Validator;
import org.example.model.User;
import org.example.service.EmailService;

import java.io.IOException;
import java.util.UUID;

public class ForgotPasswordController {
    @FXML
    private TextField emailTextField;
    @FXML
    private Button sendCodeButton;

    private UserDao userDao = new UserDao();
    private EmailService emailService = new EmailService();

    public void onSendCodeClick(ActionEvent actionEvent) {
        String email = emailTextField.getText().trim();

        if (email.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Cảnh báo", "Vui lòng nhập đầy đủ thông tin");
        }
        if (!Validator.isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi định dạng", "Email không hợp lệ! (VD: abc@gmail.com)");
            return;
        }
        if (!userDao.isEmailExists(email)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Email này chưa được đăng ký trong hệ thống!");
            return;
        }
        User user = userDao.getUserByEmail(email);

        if(user != null){
            String otp = UserDao.generateOTP();

            new Thread(() -> {
                String emailContent = emailService.getOtpEmailTemplate(user.getFullname(), otp);
                boolean sent = emailService.sendEmail(email, "Khôi phục mật khẩu - EduPath", emailContent);
                if (!sent) {
                    System.err.println("Gửi mail thất bại!");
                }
            }).start();

            switchToOtpScreen(user, otp);
        }
        else {
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không tìm thấy thông tin người dùng.");
        }
    }

    public void onBackToLoginClick(ActionEvent actionEvent) {
        switchScene("/login.fxml", "Đăng Nhập");
    }

    private void switchToOtpScreen(User user, String otp) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OTP.xml.fxml"));
            Parent root = loader.load();

            OtpController otpController = loader.getController();
            otpController.setForgotPasswordData(user, otp);

            Stage stage = (Stage) sendCodeButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Xác Thực OTP");
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải màn hình OTP: " + e.getMessage());
        }
    }

    private void switchScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) emailTextField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy file: " + fxmlPath);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();

    }
}
