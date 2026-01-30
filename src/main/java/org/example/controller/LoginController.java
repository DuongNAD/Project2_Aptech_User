package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.dao.UserDao;
import org.example.model.User;

import java.io.IOException;

public class LoginController {
    @FXML
    private Button googleLoginButton;
    @FXML
    private TextField usernameTextField;
    @FXML
    private TextField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Hyperlink forgotPasswordHyperlink;
    @FXML
    private Hyperlink registerLink;

    private UserDao userDao = new UserDao();

    public void onGoogleLoginClick(ActionEvent actionEvent) {
        googleLoginButton.setDisable(true);
    }

    public void onLoginButtonClick(ActionEvent actionEvent) {
        String loginKey = usernameTextField.getText();
        String password = passwordField.getText();

        if(loginKey.trim().isEmpty() || password.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR,"Cảnh báo","Vui lòng nhập đầy đủ thông tin");
            return;
        }

        User user = userDao.login(loginKey, password);
        if(user != null) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xin chào, " + user.getFullname() + "!");
        }
        else {
            showAlert(Alert.AlertType.ERROR, "Thất bại", "Sai thông tin hoặc tài khoản bị khóa!");
        }

    }

    public void onRegisterLinkClick(ActionEvent actionEvent) {
        switchScene("/register.fxml", "Đăng Ký Tài Khoản");
    }

    private void switchScene(String fxmlPath, String title) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();

        }
        catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,"Lỗi", "Không tìm thấy file: " + fxmlPath);
        }
    }

    public void onForgotPasswordClick(ActionEvent actionEvent) {
        switchScene("/forgotPassword.fxml", "Khôi Phục Mật Khẩu");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();

    }


}
