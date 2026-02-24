package org.example.util;

import javafx.scene.Parent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
    private static boolean isDarkMode = false; // Mặc định là Sáng
    private static final List<WeakReference<Parent>> trackedRoots = new ArrayList<>();

    public static boolean isDarkMode() {
        return isDarkMode;
    }

    public static void setDarkMode(boolean darkMode) {
        isDarkMode = darkMode;
        applyToAllTrackedRoots();
    }

    // Hàm áp dụng theme cho bất kỳ giao diện nào (gọi khi chuyển trang)
    public static void applyTheme(Parent root) {
        if (root == null)
            return;

        // Theo dõi root này để update sau này
        trackRoot(root);

        // Xóa class cũ để tránh trùng lặp
        root.getStyleClass().remove("dark-theme");

        if (isDarkMode) {
            root.getStyleClass().add("dark-theme");
        }
    }

    private static void trackRoot(Parent root) {
        // Xóa các root đã bị thu hồi (garbage collected) và kiểm tra xem đã track chưa
        trackedRoots.removeIf(ref -> ref.get() == null);
        for (WeakReference<Parent> ref : trackedRoots) {
            if (ref.get() == root) {
                return; // Đã theo dõi root này
            }
        }
        trackedRoots.add(new WeakReference<>(root));
    }

    private static void applyToAllTrackedRoots() {
        trackedRoots.removeIf(ref -> ref.get() == null);
        for (WeakReference<Parent> ref : trackedRoots) {
            Parent root = ref.get();
            if (root != null) {
                root.getStyleClass().remove("dark-theme");
                if (isDarkMode) {
                    root.getStyleClass().add("dark-theme");
                }
            }
        }
    }
}