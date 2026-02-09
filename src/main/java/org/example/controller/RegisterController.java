package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.dao.UserDao;
import org.example.model.User;
import org.example.service.EmailService;
import org.example.util.SecurityUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {

    @FXML
    private Label appNameLabel;

    @FXML
    private TextField fullNameTextField;
    @FXML
    private TextField userNameTextField;
    @FXML
    private TextField emailTextField;
    @FXML
    private TextField phoneTextField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button registerButton;
    @FXML
    private Button backToLoginButton;

    @FXML
    private StackPane avatarContainer;
    @FXML
    private ImageView avatarImageView;
    @FXML
    private StackPane imageCropContainer;

    private double startX;
    private double startY;

    private UserDao userDao = new UserDao();
    private EmailService emailService = new EmailService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fullNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                appNameLabel.setText("Nguyễn Văn A");
            } else {
                appNameLabel.setText(newValue.trim());
            }
        });

        setupAvatar();
    }

    private void setupAvatar() {
        Circle clip = new Circle();
        clip.setRadius(65);
        clip.centerXProperty().bind(imageCropContainer.widthProperty().divide(2));
        clip.centerYProperty().bind(imageCropContainer.heightProperty().divide(2));

        imageCropContainer.setClip(clip);

        try {
            Image image = new Image(getClass().getResourceAsStream("/avatar.jpg"));
            if (!image.isError()) {
                avatarImageView.setImage(image);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        avatarImageView.setOnMousePressed(e -> {
            startX = e.getSceneX() - avatarImageView.getTranslateX();
            startY = e.getSceneY() - avatarImageView.getTranslateY();
            avatarImageView.setCursor(javafx.scene.Cursor.CLOSED_HAND);
        });

        avatarImageView.setOnMouseDragged(e -> {
            avatarImageView.setTranslateX(e.getSceneX() - startX);
            avatarImageView.setTranslateY(e.getSceneY() - startY);
        });

        avatarImageView.setOnMouseReleased(e -> {
            avatarImageView.setCursor(javafx.scene.Cursor.MOVE);
        });
    }

    @FXML
    public void handleAvatarClick(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh đại diện");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(avatarContainer.getScene().getWindow());

        if (selectedFile != null) {
            Image newImage = new Image(selectedFile.toURI().toString());
            avatarImageView.setImage(newImage);
            avatarImageView.setTranslateX(0);
            avatarImageView.setTranslateY(0);
        }
    }

    @FXML
    public void onRegisterButtonClick() {

        String fullName = fullNameTextField.getText();
        String username = userNameTextField.getText();
        String email = emailTextField.getText();
        String phone = phoneTextField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", "Vui lòng điền đầy đủ thông tin!");
            return;
        }

        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Email", "Email không đúng định dạng (VD: abc@gmail.com)!");
            return;
        }

        if(!isValidPhone(phone)) {
            showAlert(Alert.AlertType.ERROR,"Lỗi số điện thoại", "Số điện thoại phải bắt đầu bằng số 0 và có 10 chữ số!");
            return;
        }

        if (!isValidPassword(password)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi mật khẩu", "Mật khẩu cần ít nhất 6 ký tự, bao gồm chữ và số!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Sai mật khẩu", "Mật khẩu nhập lại không khớp!");
            confirmPasswordField.clear();
            return;
        }

        if (userDao.isEmailExists(email)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Email này đã được sử dụng!");
            return;
        }
        if (userDao.isUsernameExists(username)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Tên đăng nhập này đã được sử dụng!");
            return;
        }

        String hashedPassword = SecurityUtil.hashPassword(password);
        User newUser = new User(0, username, fullName, email, hashedPassword, "student", "default.png", false);

        String otp = UserDao.generateOTP();

        new Thread(() -> {
            String emailContent = emailService.getOtpEmailTemplate(newUser.getUsername(),otp);
            emailService.sendEmail(email,"Xác thực tài khoản EduPath", emailContent);
        }).start();
        switchScene(newUser,otp);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        userNameTextField.clear();
        emailTextField.clear();
        phoneTextField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        fullNameTextField.clear();
        avatarImageView.setTranslateX(0);
        avatarImageView.setTranslateY(0);
    }

    private void switchScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy file: " + fxmlPath);
        }
    }
    private void switchScene(User user, String otp) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OTP.xml.fxml"));
            Parent root = loader.load();

            OtpController otpController = loader.getController();
            otpController.setData(user, otp);

            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Xác thực OTP");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e){
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,"Lỗi", "Không thể tải màn hình OTP.");
        }
    }

    @FXML
    public void onLoginLinkClick(ActionEvent actionEvent) {
        switchScene("/login.fxml", "Đăng Nhập");
    }
    @FXML
    public void onBackToLoginClick() {
        switchScene("/login.fxml", "Đăng Nhập");
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return email.matches(emailRegex);
    }

    private boolean isValidPhone(String phone) {
        String phoneRegex = "^0\\d{9}$";
        return phone.matches(phoneRegex);
    }

    private boolean isValidPassword(String password) {
        String passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d).{6,}$";
        return password.matches(passwordRegex);
    }
}