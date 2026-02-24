package org.example.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.util.StringConverter;
import org.example.dao.CategoryDao;
import org.example.dao.CourseDao;
import org.example.model.Category;
import org.example.model.Course;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CoursesController implements Initializable {

    @FXML
    private FlowPane coursesContainer;
    @FXML
    private HBox chipsContainer;
    @FXML
    private ScrollPane mainScrollPane;
    @FXML
    private ComboBox<Category> categoryComboBox;
    @FXML
    private VBox rootPane;
    @FXML
    private HeaderController headerController;

    private CourseDao courseDao = new CourseDao();
    private CategoryDao categoryDao = new CategoryDao();
    private Button currentActiveBtn = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadCoursesAsync(0);
        renderCategories();
        org.example.util.ScrollUtil.applySmoothScrolling(mainScrollPane);

        if (headerController != null) {
            headerController.setTitle("Cửa hàng khóa học");
            headerController.showThemeButton(true);
        }

        if (rootPane.getScene() != null) {
            org.example.util.ThemeManager.applyTheme(rootPane.getScene().getRoot());
        } else {
            rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    javafx.application.Platform
                            .runLater(() -> org.example.util.ThemeManager.applyTheme(newScene.getRoot()));
                }
            });
        }
    }

    private void renderCategories() {
        chipsContainer.getChildren().clear();
        List<Category> allCategories = categoryDao.getAllCategories();

        Button btnAll = createChipButton("Tất cả", 0);
        setActiveStyle(btnAll);
        chipsContainer.getChildren().add(btnAll);

        int limit = Math.min(allCategories.size(), 5);
        for (int i = 0; i < limit; i++) {
            Category cat = allCategories.get(i);
            Button btn = createChipButton(cat.getName(), cat.getCategoryId());
            chipsContainer.getChildren().add(btn);
        }

        setupCategoryComboBox(allCategories);
    }

    private Button createChipButton(String text, int categoryId) {
        Button btn = new Button(text);
        btn.getStyleClass().add("filter-chip");
        btn.setOnAction(e -> {
            categoryComboBox.getSelectionModel().clearSelection();
            setActiveStyle(btn);
            loadCoursesAsync(categoryId);
        });
        return btn;
    }

    private void setActiveStyle(Button btn) {
        if (currentActiveBtn != null) {
            currentActiveBtn.getStyleClass().remove("filter-chip-active");
            currentActiveBtn.getStyleClass().add("filter-chip");
        }
        btn.getStyleClass().remove("filter-chip");
        btn.getStyleClass().add("filter-chip-active");
        currentActiveBtn = btn;
    }

    private void setupCategoryComboBox(List<Category> list) {
        categoryComboBox.getItems().clear();
        categoryComboBox.getItems().addAll(list);

        categoryComboBox.setConverter(new StringConverter<Category>() {
            @Override
            public String toString(Category category) {
                return category == null ? null : category.getName();
            }

            @Override
            public Category fromString(String string) {
                return null;
            }
        });

        categoryComboBox.setOnAction(e -> {
            Category selected = categoryComboBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (currentActiveBtn != null) {
                    currentActiveBtn.getStyleClass().remove("filter-chip-active");
                    currentActiveBtn.getStyleClass().add("filter-chip");
                    currentActiveBtn = null;
                }
                loadCoursesAsync(selected.getCategoryId());
            }
        });
    }

    private void loadCoursesAsync(int categoryId) {
        Task<List<Course>> loadTask = new Task<>() {
            @Override
            protected List<Course> call() throws Exception {
                return (categoryId == 0) ? courseDao.getAllCourses() : courseDao.getCoursesByCategoryId(categoryId);
            }
        };
        loadTask.setOnSucceeded(e -> renderCourses(loadTask.getValue()));
        new Thread(loadTask).start();
    }

    private void renderCourses(List<Course> courses) {
        coursesContainer.getChildren().clear();
        if (courses == null || courses.isEmpty()) {
            Label emptyLabel = new Label("Chưa có khóa học nào.");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #718096;");
            coursesContainer.getChildren().add(emptyLabel);
            return;
        }
        for (Course course : courses) {
            coursesContainer.getChildren().add(createCourseCard(course));
        }
    }

    private VBox createCourseCard(Course course) {
        VBox card = new VBox();
        card.setPrefWidth(260);
        card.getStyleClass().add("course-card");

        // Bật Cache để giảm lag khi cuộn (quan trọng cho Shadow CSS)
        card.setCache(true);
        card.setCacheHint(javafx.scene.CacheHint.SPEED);

        StackPane imageContainer = new StackPane();
        ImageView imageView = new ImageView();
        imageView.setFitWidth(260);
        imageView.setFitHeight(150);
        imageView.getStyleClass().add("course-image");

        Rectangle clip = new Rectangle(260, 150);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        imageView.setClip(clip);

        try {
            if (course.getThumbnailUrl() != null && !course.getThumbnailUrl().isEmpty()) {
                // Thêm true ở cuối để bật tải ảnh ngầm (background loading), không làm đơ UI
                imageView.setImage(new Image(course.getThumbnailUrl(), 400, 0, true, true, true));
            } else {
                imageView.setImage(new Image(getClass().getResource("/View/avatar.jpg").toExternalForm(), 400, 0, true,
                        true, true));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        imageContainer.getChildren().add(imageView);

        Label badge = new Label("HOT");
        badge.getStyleClass().add("badge-best-seller");
        StackPane.setAlignment(badge, Pos.TOP_LEFT);
        StackPane.setMargin(badge, new javafx.geometry.Insets(10, 0, 0, 10));
        imageContainer.getChildren().add(badge);

        VBox content = new VBox();
        content.setSpacing(5);
        content.getStyleClass().add("card-content");

        Label categoryLabel = new Label(course.getCategoryName() != null ? course.getCategoryName() : "General");
        categoryLabel.getStyleClass().add("course-category");

        Label titleLabel = new Label(course.getTitle());
        titleLabel.getStyleClass().add("course-title");
        titleLabel.setWrapText(true);
        titleLabel.setPrefHeight(45);

        String priceText = String.format("%,.0f đ",
                course.getSalePrice() > 0 ? course.getSalePrice() : course.getPrice());
        Label priceLabel = new Label(priceText);
        priceLabel.getStyleClass().add("course-price-new");

        Button btnDetail = new Button("Xem chi tiết");
        btnDetail.setMaxWidth(Double.MAX_VALUE);
        btnDetail.getStyleClass().add("btn-view-detail");

        btnDetail.setOnAction(e -> goToDetail(course, e));

        content.getChildren().addAll(categoryLabel, titleLabel, priceLabel, btnDetail);
        card.getChildren().addAll(imageContainer, content);

        return card;
    }

    private void goToDetail(Course course, javafx.event.Event event) {
        org.example.util.Navigation.to(
                event,
                org.example.util.Navigation.COURSE_DETAIL_VIEW,
                (org.example.controller.CourseDetailController controller) -> {
                    controller.setCourseData(course);
                });
    }
}