package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.dao.UserDao;
import org.example.dao.Validator;
import org.example.model.User;
import org.example.util.UserSession;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML private TextField tfFullName;
    @FXML private TextField tfEmail;
    @FXML private ImageView avatarPreview;
    @FXML private Button btnUploadAvatar;

    @FXML private PasswordField pfCurrentPass;
    @FXML private PasswordField pfNewPass;
    @FXML private PasswordField pfConfirmPass;
    @FXML private Label lblMessage;

    private UserDao userDao = new UserDao();
    private String currentAvatarUrl = "";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadUserProfile();

        if (btnUploadAvatar != null) {
            btnUploadAvatar.setOnAction(e -> handleUploadAvatar());
        }
    }

    private void loadUserProfile() {
        User user = UserSession.getInstance().getUser();
        if (user != null) {
            tfFullName.setText(user.getFullname());
            tfEmail.setText(user.getEmail());
            currentAvatarUrl = user.getAvatarUrl();

            try {
                if (currentAvatarUrl != null && !currentAvatarUrl.isEmpty()) {
                    avatarPreview.setImage(new Image(currentAvatarUrl, 100, 100, true, true));
                }
            } catch (Exception e) {
            }
        }
    }

    private void handleUploadAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh đại diện");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) tfFullName.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            currentAvatarUrl = selectedFile.toURI().toString();
            avatarPreview.setImage(new Image(currentAvatarUrl, 100, 100, true, true));
        }
    }

    @FXML
    void handleSaveProfile(ActionEvent event) {
        String newName = tfFullName.getText();

        if (newName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng nhập họ tên.");
            return;
        }

        User currentUser = UserSession.getInstance().getUser();

        boolean success = userDao.updateProfile(currentUser.getId(), newName, currentAvatarUrl);

        if (success) {
            currentUser.setFullname(newName);
            currentUser.setAvatarUrl(currentAvatarUrl);

            showAlert(Alert.AlertType.INFORMATION, "Cập nhật thông tin thành công!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi kết nối CSDL, không thể lưu.");
        }
    }

    @FXML
    void handleChangePassword(ActionEvent event) {
        String current = pfCurrentPass.getText();
        String newVal = pfNewPass.getText();
        String confirm = pfConfirmPass.getText();

        if (current.isEmpty() || newVal.isEmpty() || confirm.isEmpty()) {
            setMessage("Vui lòng nhập đầy đủ thông tin.", true);
            return;
        }

        if (!Validator.isValidPassword(newVal)) {
            setMessage("Mật khẩu mới quá yếu! (Cần >6 ký tự, có chữ hoa, số...)", true);
            return;
        }

        if (!newVal.equals(confirm)) {
            setMessage("Mật khẩu xác nhận không khớp.", true);
            return;
        }

        User currentUser = UserSession.getInstance().getUser();

        if (userDao.checkPassword(currentUser.getId(), current)) {

            boolean success = userDao.changePassword(currentUser.getId(), newVal);

            if (success) {
                setMessage("Đổi mật khẩu thành công!", false);
                // Clear ô nhập
                pfCurrentPass.clear();
                pfNewPass.clear();
                pfConfirmPass.clear();
            } else {
                setMessage("Lỗi hệ thống, vui lòng thử lại.", true);
            }
        } else {
            setMessage("Mật khẩu hiện tại không đúng.", true);
        }
    }

    private void setMessage(String msg, boolean isError) {
        lblMessage.setText(msg);
        lblMessage.setStyle(isError ? "-fx-text-fill: #E53E3E;" : "-fx-text-fill: #38A169;");
    }

    private void showAlert(Alert.AlertType type, String content) {
        Alert alert = new Alert(type);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}