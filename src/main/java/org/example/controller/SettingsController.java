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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML
    private TextField txtFullName;
    @FXML
    private TextField txtEmail;
    @FXML
    private ImageView avatarPreview;
    @FXML
    private Button btnUploadAvatar;
    @FXML
    private ScrollPane settingsScrollPane;

    @FXML
    private PasswordField txtCurrentPass;
    @FXML
    private PasswordField txtNewPass;
    @FXML
    private PasswordField txtConfirmPass;

    @FXML
    private Label lblProfileMessage;
    @FXML
    private Label lblPasswordMessage;

    private UserDao userDao = new UserDao();
    private String currentAvatarUrl = "";
    private File selectedAvatarFile = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadUserProfile();
        org.example.util.ScrollUtil.applySmoothScrolling(settingsScrollPane);

        if (btnUploadAvatar != null) {
            btnUploadAvatar.setOnAction(e -> handleUploadAvatar());
        }
    }

    private void loadUserProfile() {
        User user = UserSession.getInstance().getUser();
        if (user != null) {
            // Cập nhật tên biến mới
            if (txtFullName != null)
                txtFullName.setText(user.getFullname());
            if (txtEmail != null)
                txtEmail.setText(user.getEmail());

            currentAvatarUrl = user.getAvatarUrl();

            try {
                if (currentAvatarUrl != null && !currentAvatarUrl.isEmpty()) {
                    String loadedUrl = currentAvatarUrl;
                    if (loadedUrl.startsWith("/userAvatar/")) {
                        loadedUrl = new File("src/main/resources" + loadedUrl).toURI().toString();
                    }
                    avatarPreview.setImage(new Image(loadedUrl, 120, 120, true, true));
                } else {
                    // Ảnh mặc định nếu chưa có
                    avatarPreview.setImage(new Image("https://i.pravatar.cc/150?img=12", 120, 120, true, true));
                }
            } catch (Exception e) {
                System.err.println("Lỗi load ảnh avatar: " + e.getMessage());
            }
        }
    }

    private void handleUploadAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh đại diện");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        Stage stage = (Stage) txtFullName.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            selectedAvatarFile = selectedFile;
            currentAvatarUrl = selectedFile.toURI().toString();
            avatarPreview.setImage(new Image(currentAvatarUrl, 120, 120, true, true));
        }
    }

    @FXML
    void handleSaveProfile(ActionEvent event) {
        String newName = txtFullName.getText(); // Dùng biến mới

        if (newName.isEmpty()) {
            setProfileMessage("Vui lòng nhập họ tên.", true);
            return;
        }

        User currentUser = UserSession.getInstance().getUser();

        // Xử lý lưu file ảnh nếu có thay đổi
        if (selectedAvatarFile != null) {
            String uploadDir = "src/main/resources/userAvatar";
            try {
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                String fileName = currentUser.getId() + ".jpg";
                Path targetPath = uploadPath.resolve(fileName);

                Files.copy(selectedAvatarFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                currentAvatarUrl = "/userAvatar/" + fileName;
            } catch (Exception e) {
                e.printStackTrace();
                setProfileMessage("Lỗi khi lưu ảnh đại diện.", true);
                return;
            }
        }

        // Giả sử hàm updateProfile của anh nhận (id, name, avatar)
        boolean success = userDao.updateProfile(currentUser.getId(), newName, currentAvatarUrl);

        if (success) {
            currentUser.setFullname(newName);
            currentUser.setAvatarUrl(currentAvatarUrl);
            selectedAvatarFile = null; // Reset form trạng thái file
            setProfileMessage("Cập nhật thông tin thành công!", false);
        } else {
            setProfileMessage("Lỗi kết nối CSDL, không thể lưu.", true);
        }
    }

    @FXML
    void handleChangePassword(ActionEvent event) {
        // Dùng biến mới
        String current = txtCurrentPass.getText();
        String newVal = txtNewPass.getText();
        String confirm = txtConfirmPass.getText();

        if (current.isEmpty() || newVal.isEmpty() || confirm.isEmpty()) {
            setPasswordMessage("Vui lòng nhập đầy đủ thông tin.", true);
            return;
        }

        if (!Validator.isValidPassword(newVal)) {
            setPasswordMessage("Mật khẩu yếu! (Cần >6 ký tự, có chữ hoa, số...)", true);
            return;
        }

        if (!newVal.equals(confirm)) {
            setPasswordMessage("Mật khẩu xác nhận không khớp.", true);
            return;
        }

        User currentUser = UserSession.getInstance().getUser();

        if (userDao.checkPassword(currentUser.getId(), current)) {
            boolean success = userDao.changePassword(currentUser.getId(), newVal);

            if (success) {
                setPasswordMessage("Đổi mật khẩu thành công!", false);
                // Clear ô nhập
                txtCurrentPass.clear();
                txtNewPass.clear();
                txtConfirmPass.clear();
            } else {
                setPasswordMessage("Lỗi hệ thống, vui lòng thử lại.", true);
            }
        } else {
            setPasswordMessage("Mật khẩu hiện tại không đúng.", true);
        }
    }

    // --- Hàm hỗ trợ hiển thị thông báo đẹp ---
    private void setProfileMessage(String msg, boolean isError) {
        lblProfileMessage.setText(msg);
        lblProfileMessage.setStyle(isError ? "-fx-text-fill: #EF4444;" : "-fx-text-fill: #10B981;");
    }

    private void setPasswordMessage(String msg, boolean isError) {
        lblPasswordMessage.setText(msg);
        lblPasswordMessage.setStyle(isError ? "-fx-text-fill: #EF4444;" : "-fx-text-fill: #10B981;");
    }
}