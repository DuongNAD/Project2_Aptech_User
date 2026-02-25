package org.example.controller;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.example.dao.NotificationDao;
import org.example.model.Notification;
import org.example.model.User;
import org.example.util.ThemeManager;
import org.example.util.UserSession;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;

public class HeaderController implements Initializable {

    @FXML
    private Label lblPageTitle;
    @FXML
    private Label lblUserName;
    @FXML
    private ImageView imgUserAvatar;
    @FXML
    public Button btnThemeToggle;
    @FXML
    public ImageView iconTheme;
    @FXML
    private TextField txtSearch;

    // --- BIẾN CHO THÔNG BÁO ---
    @FXML
    private ImageView iconBell; // Cái chuông (fx:id="iconBell" bên FXML)
    @FXML
    private Label lblUnreadCount; // Chấm đỏ (fx:id="lblUnreadCount" bên FXML)

    private NotificationDao notiDao = new NotificationDao();
    private ContextMenu notiMenu; // Menu thả xuống

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 1. Load thông tin User
        User user = UserSession.getInstance().getUser();
        if (user != null) {
            if (lblUserName != null)
                lblUserName.setText(user.getFullname());
            try {
                if (imgUserAvatar != null && user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                    String aUrl = user.getAvatarUrl();
                    if (aUrl.startsWith("/userAvatar/")) {
                        aUrl = new java.io.File("src/main/resources" + aUrl).toURI().toString();
                    }
                    imgUserAvatar.setImage(new Image(aUrl, 36, 36, true, true));
                }
            } catch (Exception e) {
            }
        }

        updateThemeIcon();

        if (btnThemeToggle != null) {
            btnThemeToggle.setOnAction(e -> toggleTheme());
        }

        // --- BẮT ĐẦU CHẠY THÔNG BÁO ---
        if (iconBell != null) {
            // Khởi tạo ContextMenu một lần
            notiMenu = new ContextMenu();
            notiMenu.getStyleClass().add("notification-context-menu"); // Class CSS khung ngoài (trong suốt)

            // Click vào chuông thì hiện danh sách
            iconBell.setOnMouseClicked(event -> handleShowNotifications());

            // Tự động kiểm tra tin nhắn mới mỗi 5 giây
            startNotiPolling();
        }
    }

    // --- LOGIC 1: CHẠY NGẦM ĐẾM SỐ TIN CHƯA ĐỌC ---
    private void startNotiPolling() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> updateUnreadCount()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        updateUnreadCount(); // Chạy ngay lần đầu
    }

    private void updateUnreadCount() {
        User user = UserSession.getInstance().getUser();
        if (user == null)
            return;

        new Thread(() -> {
            int count = notiDao.countUnread(user.getId());
            Platform.runLater(() -> {
                if (lblUnreadCount != null) {
                    if (count > 0) {
                        lblUnreadCount.setVisible(true);
                        lblUnreadCount.setText(count > 9 ? "9+" : String.valueOf(count));
                    } else {
                        lblUnreadCount.setVisible(false);
                    }
                }
            });
        }).start();
    }

    // --- LOGIC 2: HIỂN THỊ POPUP THÔNG BÁO (QUAN TRỌNG NHẤT) ---
    private void handleShowNotifications() {
        User user = UserSession.getInstance().getUser();
        if (user == null)
            return;

        // Xóa menu cũ để vẽ lại từ đầu
        notiMenu.getItems().clear();

        // 1. Tạo Container Chính (VBox) - Nơi chứa toàn bộ nội dung popup
        VBox container = new VBox();
        container.getStyleClass().add("notification-container"); // Class CSS nền trắng/đen, bo góc

        // --- A. HEADER (Tiêu đề xanh) ---
        HBox header = new HBox();
        header.getStyleClass().add("notification-header");

        Label lblHeader = new Label("Thông báo mới");
        lblHeader.getStyleClass().add("notification-header-label");
        header.getChildren().add(lblHeader);

        // --- B. BODY (Danh sách thông báo) ---
        VBox listItems = new VBox(); // Không cần spacing vì CSS item đã có padding/border

        List<Notification> list = notiDao.getMyNotifications(user.getId());

        if (list.isEmpty()) {
            Label emptyLbl = new Label("Bạn chưa có thông báo nào.");
            emptyLbl.setStyle("-fx-padding: 20; -fx-text-fill: #94A3B8; -fx-font-style: italic;");
            // Căn giữa thông báo rỗng
            HBox emptyBox = new HBox(emptyLbl);
            emptyBox.setAlignment(javafx.geometry.Pos.CENTER);
            listItems.getChildren().add(emptyBox);
        } else {
            for (Notification n : list) {
                // Thêm từng dòng thông báo vào list
                listItems.getChildren().add(createNotificationItemRow(n));
            }
        }

        // --- C. FOOTER (Nút đánh dấu đã đọc) ---
        HBox footer = new HBox();
        footer.getStyleClass().add("notification-footer");

        Button btnMarkRead = new Button("Đánh dấu tất cả là đã đọc");
        btnMarkRead.getStyleClass().add("btn-mark-read");

        // Sự kiện click nút đánh dấu
        btnMarkRead.setOnAction(e -> {
            new Thread(() -> {
                notiDao.markAsRead(user.getId());
                Platform.runLater(() -> {
                    updateUnreadCount();
                    notiMenu.hide(); // Ẩn menu sau khi bấm
                });
            }).start();
        });

        footer.getChildren().add(btnMarkRead);

        // --- GHÉP LẠI ---
        container.getChildren().addAll(header, listItems, footer);

        // Đưa Container vào MỘT CustomMenuItem duy nhất
        CustomMenuItem item = new CustomMenuItem(container);
        item.setHideOnClick(false); // Bấm vào vùng trống không bị tắt popup

        notiMenu.getItems().add(item);

        // Hiển thị Popup ngay dưới nút chuông, lệch sang trái một chút cho đẹp
        notiMenu.show(iconBell, Side.BOTTOM, -280, 10);
    }

    // --- LOGIC 3: TẠO GIAO DIỆN TỪNG DÒNG (HBox) ---
    // Trả về HBox để add vào container, KHÔNG PHẢI trả về CustomMenuItem
    private HBox createNotificationItemRow(Notification n) {
        HBox row = new HBox(12);
        row.getStyleClass().add("notification-item"); // Class CSS hover hiệu ứng

        // 1. Icon bên trái (Dựa trên loại thông báo)
        String iconText = "ℹ️"; // Mặc định
        if ("SUCCESS".equals(n.getType()))
            iconText = "✅";
        else if ("WARNING".equals(n.getType()))
            iconText = "⚠️";

        Label lblIcon = new Label(iconText);
        lblIcon.setStyle("-fx-font-size: 18px;"); // Icon to rõ

        VBox iconBox = new VBox(lblIcon);
        iconBox.getStyleClass().add("notif-icon-box"); // Căn chỉnh vị trí icon

        // 2. Nội dung bên phải
        VBox content = new VBox(2);

        Label lblTitle = new Label(n.getTitle());
        lblTitle.getStyleClass().add("notif-title"); // CSS Title đậm

        Label lblMsg = new Label(n.getMessage());
        lblMsg.getStyleClass().add("notif-msg"); // CSS Message xám
        lblMsg.setWrapText(true);
        lblMsg.setMaxWidth(260); // Giới hạn chiều rộng để tự xuống dòng

        // Format thời gian
        String timeStr = (n.getCreatedAt() != null)
                ? new SimpleDateFormat("HH:mm dd/MM").format(n.getCreatedAt())
                : "";
        Label lblTime = new Label(timeStr);
        lblTime.getStyleClass().add("notif-time"); // CSS Time nhỏ

        content.getChildren().addAll(lblTitle, lblMsg, lblTime);

        // Ghép icon và content
        row.getChildren().addAll(iconBox, content);

        return row;
    }

    // --- CÁC HÀM CŨ (THEME) GIỮ NGUYÊN ---
    public void setTitle(String title) {
        if (lblPageTitle != null)
            lblPageTitle.setText(title);
    }

    public void showThemeButton(boolean show) {
        if (btnThemeToggle != null) {
            btnThemeToggle.setVisible(show);
            btnThemeToggle.setManaged(show);
        }
    }

    private void toggleTheme() {
        if (lblPageTitle == null || lblPageTitle.getScene() == null)
            return;
        Parent root = lblPageTitle.getScene().getRoot();

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), root);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.1);

        fadeOut.setOnFinished(event -> {
            ThemeManager.setDarkMode(!ThemeManager.isDarkMode());
            ThemeManager.applyTheme(root);
            updateThemeIcon();

            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), root);
            fadeIn.setFromValue(0.1);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }

    private void updateThemeIcon() {
        if (iconTheme != null) {
            String iconUrl = ThemeManager.isDarkMode()
                    ? "https://img.icons8.com/ios-glyphs/30/ffffff/sun--v1.png"
                    : "https://img.icons8.com/ios-glyphs/30/000000/moon-symbol.png";
            try {
                iconTheme.setImage(new Image(iconUrl));
            } catch (Exception e) {
            }
        }

        if (iconBell != null) {
            String bellUrl = ThemeManager.isDarkMode()
                    ? "https://img.icons8.com/ios-filled/50/ffffff/bell.png"
                    : "https://img.icons8.com/ios-filled/50/334155/bell.png";
            try {
                iconBell.setImage(new Image(bellUrl));
            } catch (Exception e) {
            }
        }
    }

    @FXML
    public void handleSearch(javafx.event.ActionEvent event) {
        if (txtSearch == null)
            return;
        String query = txtSearch.getText().trim();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/courses.fxml"));
            Parent view = loader.load();
            CoursesController controller = loader.getController();

            // Nếu ô tìm kiếm không trống thì fetch data theo query, ngược lại load all.
            if (!query.isEmpty()) {
                controller.searchCourses(query);
            }

            if (txtSearch.getScene() != null) {
                javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) txtSearch.getScene()
                        .lookup("#contentArea");
                if (contentArea != null) {
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(view);
                    ThemeManager.applyTheme(view);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}