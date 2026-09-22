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

import org.example.util.FirebaseAuthUtil;
import org.example.util.UserSession;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import javafx.application.Platform;

import org.example.util.Config;

public class RegisterController implements Initializable {

    private static final String CLIENT_ID = Config.get("google.client_id");
    private static final String CLIENT_SECRET = Config.get("google.client_secret");
    private static final String REDIRECT_URI = "http://127.0.0.1:8888/callback";
    private static final String SCOPE = "email profile openid";
    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    private static final String FB_APP_ID = Config.get("facebook.app_id");
    private static final String FB_APP_SECRET = Config.get("facebook.app_secret");
    private static final String FB_REDIRECT_URI = "http://localhost:8889/fb-callback";
    private static final String FB_SCOPE = "public_profile";
    private static final String FB_AUTH_URL = "https://www.facebook.com/v19.0/dialog/oauth";
    private static final String FB_TOKEN_URL = "https://graph.facebook.com/v19.0/oauth/access_token";
    private static final String FB_USER_INFO_URL = "https://graph.facebook.com/me";

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
    private Button googleRegisterButton;
    @FXML
    private Button facebookRegisterButton;
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

    private File selectedAvatarFile;

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
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        File selectedFile = fileChooser.showOpenDialog(avatarContainer.getScene().getWindow());

        if (selectedFile != null) {
            this.selectedAvatarFile = selectedFile;
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

        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()
                || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", "Vui lòng điền đầy đủ thông tin!");
            return;
        }

        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Email", "Email không đúng định dạng (VD: abc@gmail.com)!");
            return;
        }

        if (!isValidPhone(phone)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi số điện thoại",
                    "Số điện thoại phải bắt đầu bằng số 0 và có 10 chữ số!");
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

//        try {
//
//            FirebaseAuthUtil.registerUserWithEmailPassword(email, password);
//        } catch (Exception e) {
//            e.printStackTrace();
//            showAlert(Alert.AlertType.ERROR, "Lỗi Firebase",
//                    "Không thể tạo tài khoản xác thực qua Firebase. Chi tiết: " + e.getMessage());
//            return;
//        }

        String hashedPassword = SecurityUtil.hashPassword(password);
        User newUser = new User(0, username, fullName, email, hashedPassword, "student", "default.png", false);

        String otp = UserDao.generateOTP();
        System.out.println("OTP: " + otp);
        try {
            new Thread(() -> {
                String emailContent = emailService.getOtpEmailTemplate(newUser.getUsername(), otp);
                emailService.sendEmail(email, "Xác thực tài khoản EduPath", emailContent);
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi gửi email", "Không thể gửi email xác thực. Vui lòng thử lại!");
            return;
        }
        switchScene(newUser, otp);
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OTP.xml.fxml"));
            Parent root = loader.load();

            OtpController otpController = loader.getController();
            otpController.setData(user, otp);
            otpController.setAvatarFile(selectedAvatarFile);

            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Xác thực OTP");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải màn hình OTP.");
        }
    }

    private void switchToHome() {
        try {
            java.net.URL url = getClass().getResource("/View/hello-view.fxml");
            if (url == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi Nghiêm Trọng",
                        "Không tìm thấy file FXML!\nHãy Rebuild lại dự án.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Stage stage = (Stage) registerButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("CườngLearn - Dashboard Học Tập");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Lỗi khi nạp giao diện: " + e.getMessage());
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

    @FXML
    public void onGoogleRegisterClick(ActionEvent actionEvent) {
        googleRegisterButton.setDisable(true);
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8888), 0);
            server.createContext("/callback", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                String code = null;
                if (query != null && query.contains("code=")) {
                    code = query.split("code=")[1].split("&")[0];
                }
                String response = "<html><body><h1 style='text-align:center;'>Dang nhap thanh cong! Ban co the tat tab nay.</h1></body></html>";
                exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
                if (code != null) {
                    String finalCode = code;
                    new Thread(() -> {
                        handleGoogleOAuthCode(finalCode);
                        server.stop(0);
                    }).start();
                } else {
                    server.stop(0);
                }
            });
            server.start();

            String encodedRedirectUri = URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8);
            String encodedScope = URLEncoder.encode(SCOPE, StandardCharsets.UTF_8);
            String loginUrl = AUTH_URL + "?client_id=" + CLIENT_ID +
                    "&redirect_uri=" + encodedRedirectUri +
                    "&response_type=code&scope=" + encodedScope +
                    "&access_type=offline";

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create(loginUrl));
            }
        } catch (Exception e) {
            e.printStackTrace();
            googleRegisterButton.setDisable(false);
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở trình duyệt: " + e.getMessage());
        }
    }

    private void handleGoogleOAuthCode(String code) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String params = "client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET +
                    "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8) +
                    "&grant_type=authorization_code&redirect_uri="
                    + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8);

            HttpRequest tokenRequest = HttpRequest.newBuilder().uri(URI.create(TOKEN_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(params)).build();

            HttpResponse<String> tokenResponse = client.send(tokenRequest, HttpResponse.BodyHandlers.ofString());
            JsonObject tokenJson = JsonParser.parseString(tokenResponse.body()).getAsJsonObject();

            if (tokenJson.has("id_token")) {
                String idToken = tokenJson.get("id_token").getAsString();
                String accessToken = tokenJson.has("access_token") ? tokenJson.get("access_token").getAsString() : "";

                HttpRequest infoRequest = HttpRequest.newBuilder().uri(URI.create(USER_INFO_URL))
                        .header("Authorization", "Bearer " + accessToken).GET().build();

                HttpResponse<String> infoResponse = client.send(infoRequest, HttpResponse.BodyHandlers.ofString());
                JsonObject userInfo = JsonParser.parseString(infoResponse.body()).getAsJsonObject();

                String googleId = userInfo.has("sub") ? userInfo.get("sub").getAsString() : "";
                String name = userInfo.has("name") ? userInfo.get("name").getAsString() : "Google User";
                String email = userInfo.has("email") ? userInfo.get("email").getAsString() : "";
                String pictureUrl = userInfo.has("picture") ? userInfo.get("picture").getAsString() : "";

                FirebaseAuthUtil.signInWithGoogleIdToken(idToken);

                Platform.runLater(() -> {
                    User user = userDao.getUserByProvider("GOOGLE", googleId);
                    if (user == null) {
                        User newUser = new User();
                        newUser.setUsername("gg_" + googleId);
                        newUser.setFullname(name);
                        newUser.setEmail(email);
                        newUser.setPasswordHash(null);
                        newUser.setRole("Student");
                        newUser.setAvatarUrl(pictureUrl);
                        newUser.setAuthProvider("GOOGLE");
                        newUser.setProviderId(googleId);

                        boolean isRegistered = userDao.register(newUser);
                        if (isRegistered) {
                            user = userDao.getUserByProvider("GOOGLE", googleId);
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tạo tài khoản mới từ Google!");
                            return;
                        }
                    }

                    if (user != null) {
                        if (Boolean.FALSE.equals(user.getIsActive())) {
                            showAlert(Alert.AlertType.ERROR, "Lỗi", "Tài khoản này đã bị khóa!");
                            return;
                        }
                        UserSession.getInstance().setUser(user);
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xin chào: " + user.getFullname());
                        switchToHome();
                    }
                });
            } else {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Lỗi Đăng Ký",
                            "Google từ chối xác thực.\n" + tokenJson.toString());
                    googleRegisterButton.setDisable(false);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Exception: " + e.getMessage());
                googleRegisterButton.setDisable(false);
            });
        }
    }

    @FXML
    public void onFacebookRegisterClick(ActionEvent actionEvent) {
        facebookRegisterButton.setDisable(true);
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 8889), 0);
            server.createContext("/fb-callback", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                String code = null;
                if (query != null && query.contains("code=")) {
                    code = query.split("code=")[1].split("&")[0];
                }
                String response = "<html><body><h1 style='text-align:center;'>Dang nhap Facebook thanh cong! Ban co the tat tab nay.</h1></body></html>";
                exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
                if (code != null) {
                    String finalCode = code;
                    new Thread(() -> {
                        handleFacebookOAuthCode(finalCode);
                        server.stop(0);
                    }).start();
                } else {
                    server.stop(0);
                }
            });
            server.start();

            String encodedRedirectUri = URLEncoder.encode(FB_REDIRECT_URI, StandardCharsets.UTF_8);
            String encodedScope = URLEncoder.encode(FB_SCOPE, StandardCharsets.UTF_8);
            String loginUrl = FB_AUTH_URL + "?client_id=" + FB_APP_ID +
                    "&redirect_uri=" + encodedRedirectUri +
                    "&response_type=code&scope=" + encodedScope;

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create(loginUrl));
            }
        } catch (Exception e) {
            e.printStackTrace();
            facebookRegisterButton.setDisable(false);
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở trình duyệt: " + e.getMessage());
        }
    }

    private void handleFacebookOAuthCode(String code) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String tokenUrl = FB_TOKEN_URL + "?client_id=" + FB_APP_ID +
                    "&redirect_uri=" + URLEncoder.encode(FB_REDIRECT_URI, StandardCharsets.UTF_8) +
                    "&client_secret=" + FB_APP_SECRET +
                    "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8);

            HttpRequest tokenRequest = HttpRequest.newBuilder().uri(URI.create(tokenUrl)).GET().build();
            HttpResponse<String> tokenResponse = client.send(tokenRequest, HttpResponse.BodyHandlers.ofString());
            JsonObject tokenJson = JsonParser.parseString(tokenResponse.body()).getAsJsonObject();

            if (tokenJson.has("access_token")) {
                String accessToken = tokenJson.get("access_token").getAsString();
                String infoUrl = FB_USER_INFO_URL + "?fields=id,name,email,picture.type(large)&access_token="
                        + accessToken;
                HttpRequest infoRequest = HttpRequest.newBuilder().uri(URI.create(infoUrl)).GET().build();

                HttpResponse<String> infoResponse = client.send(infoRequest, HttpResponse.BodyHandlers.ofString());
                JsonObject userInfo = JsonParser.parseString(infoResponse.body()).getAsJsonObject();

                String fbId = userInfo.has("id") ? userInfo.get("id").getAsString() : "";
                String name = userInfo.has("name") ? userInfo.get("name").getAsString() : "Facebook User";
                String email = userInfo.has("email") ? userInfo.get("email").getAsString() : null;
                String pictureUrl = "";
                if (userInfo.has("picture")) {
                    JsonObject pictureObj = userInfo.getAsJsonObject("picture");
                    if (pictureObj.has("data")) {
                        pictureUrl = pictureObj.getAsJsonObject("data").get("url").getAsString();
                    }
                }

                FirebaseAuthUtil.signInWithFacebookToken(accessToken);
                String finalPictureUrl = pictureUrl;

                Platform.runLater(() -> {
                    User user = userDao.getUserByProvider("FACEBOOK", fbId);
                    if (user == null) {
                        User newUser = new User();
                        newUser.setUsername("fb_" + fbId);
                        newUser.setFullname(name);
                        newUser.setEmail(email);
                        newUser.setPasswordHash(null);
                        newUser.setRole("Student");
                        newUser.setAvatarUrl(finalPictureUrl);
                        newUser.setAuthProvider("FACEBOOK");
                        newUser.setProviderId(fbId);

                        boolean isRegistered = userDao.register(newUser);
                        if (isRegistered) {
                            user = userDao.getUserByProvider("FACEBOOK", fbId);
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tạo tài khoản mới từ Facebook!");
                            return;
                        }
                    }

                    if (user != null) {
                        if (Boolean.FALSE.equals(user.getIsActive())) {
                            showAlert(Alert.AlertType.ERROR, "Lỗi", "Tài khoản này đã bị khóa!");
                            return;
                        }
                        UserSession.getInstance().setUser(user);
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xin chào: " + user.getFullname());
                        switchToHome();
                    }
                });
            } else {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Lỗi Đăng Ký",
                            "Facebook từ chối xác thực.\n" + tokenJson.toString());
                    facebookRegisterButton.setDisable(false);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Exception: " + e.getMessage());
                facebookRegisterButton.setDisable(false);
            });
        }
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