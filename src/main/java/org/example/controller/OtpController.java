package org.example.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.dao.UserDao;
import org.example.model.User;
import org.example.service.EmailService;

import java.io.IOException;

public class OtpController {
    @FXML
    private TextField otpTextField;
    @FXML
    private Button confirmButton;
    @FXML
    private Label messageLabel;
    @FXML
    private Hyperlink resendLink;
    @FXML
    private Button backButton;

    private User pendingUser;
    private String serverOtp;

    private int countdownTime = 60;
    private Timeline timeline;

    private UserDao userDao = new UserDao();
    private EmailService emailService = new EmailService();
    private boolean isForgotPassword = false;

    public void setData(User user, String otp){
        this.pendingUser = user;
        this.serverOtp = otp;
        this.isForgotPassword = false;
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);
        messageLabel.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        messageLabel.setText("Chúng tôi đã gửi mã xác thực gồm 6 chữ số đến\n" + user.getEmail());
        startCountdown();
    }

    public void setForgotPasswordData(User user, String otp) {
        this.pendingUser = user;
        this.serverOtp = otp;
        this.isForgotPassword = true;
        if(backButton != null) {
            backButton.setText("Quay lại Đăng nhập");
            backButton.setUnderline(true);
        }

        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);
        messageLabel.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        messageLabel.setText("Mã xác thực khôi phục mật khẩu đã gửi đến:\n" + user.getEmail());
        startCountdown();
    }

    private void  startCountdown(){
        resendLink.setDisable(true);
        countdownTime = 60;
        resendLink.setText("Gửi lại (" + countdownTime + "s)");

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            countdownTime--;
            resendLink.setText("Gửi lại (" + countdownTime + "s)");
            if (countdownTime <= 0) {
                stopCountdown();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    private void stopCountdown() {
        if (timeline != null) {
            timeline.stop();
        }
        resendLink.setText("Gửi lại");
        resendLink.setDisable(false);
    }

    @FXML
    public void onConfirmClick() {
        String inputOtp = otpTextField.getText().trim();

        if (inputOtp.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập mã OTP!");
            return;
        }

        if (inputOtp.equals(serverOtp)) {
            if (timeline != null) timeline.stop();
            if (isForgotPassword) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xác thực thành công!");
                switchToResetPassword();
            }
            else {
                boolean isSuccess = userDao.register(pendingUser);

                if (isSuccess) {

                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xác thực thành công! Tài khoản đã được tạo.");
                    switchToLogin();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Lỗi lưu database (Check User Name trùng?).");
                }
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Mã OTP không chính xác!");
        }
    }

    private void switchToResetPassword() {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resetPassword.fxml"));
            Parent root =loader.load();

            ResetPasswordController controller = loader.getController();
            controller.setTargetUser(pendingUser);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Reset Password");
            stage.setResizable(false);
            stage.show();
        }
        catch (IOException e){
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải màn hình đặt lại mật khẩu " + e.getMessage());
        }
    }

    @FXML
    public void onResendClick() {
        String newOtp = UserDao.generateOTP();
        this.serverOtp = newOtp;

        new Thread(() -> {
            String emailContent = emailService.getOtpEmailTemplate(pendingUser.getUsername(), newOtp);
            emailService.sendEmail(pendingUser.getEmail(), "Gửi lại mã xác thực", emailContent);
        }).start();

        showAlert(Alert.AlertType.INFORMATION, "Đã gửi", "Mã OTP mới đã được gửi!");

        startCountdown();
    }

    @FXML
    public void onBackClick() {
        if (timeline != null) timeline.stop();

        if(isForgotPassword) {
            switchToLogin();
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) otpTextField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void switchToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) otpTextField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Đăng Nhập");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
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

