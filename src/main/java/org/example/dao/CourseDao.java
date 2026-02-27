package org.example.dao;

import org.example.connect.DatabaseConnect;
import org.example.model.Course;
import org.example.model.Section;
import org.example.model.CodingExercise;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseDao {

    // --- 1. CÁC HÀM LẤY KHÓA HỌC (GIỮ NGUYÊN) ---

    public List<Course> getAllCourses() {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT c.*, cat.name AS category_name " +
                "FROM courses c " +
                "LEFT JOIN categories cat ON c.category_id = cat.category_id " +
                "WHERE c.status = 'active'";

        try (Connection conn = DatabaseConnect.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapCourse(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Course> getCoursesFiltered(int categoryId) {
        List<Course> list = new ArrayList<>();
        String sql = (categoryId == 0)
                ? "SELECT c.*, cat.name AS category_name FROM courses c LEFT JOIN categories cat ON c.category_id = cat.category_id ORDER BY c.view_count DESC"
                : "SELECT c.*, cat.name AS category_name FROM courses c LEFT JOIN categories cat ON c.category_id = cat.category_id WHERE c.category_id = ? ORDER BY c.view_count DESC";

        try (Connection conn = DatabaseConnect.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (categoryId != 0) {
                stmt.setInt(1, categoryId);
            }
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(mapCourse(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Course> searchCoursesByName(String query) {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT c.*, cat.name AS category_name " +
                "FROM courses c " +
                "LEFT JOIN categories cat ON c.category_id = cat.category_id " +
                "WHERE c.status = 'active' AND c.title LIKE ?";

        try (Connection conn = DatabaseConnect.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + query + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(mapCourse(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Course> getCoursesByCategoryId(int categoryId) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses WHERE category_id = ?";

        try (Connection conn = DatabaseConnect.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Course c = new Course();
                c.setCourseId(rs.getInt("course_id"));
                c.setCategoryId(rs.getInt("category_id"));
                c.setTitle(rs.getString("title"));
                c.setDescription(rs.getString("description"));
                c.setPrice(rs.getDouble("price"));
                c.setSalePrice(rs.getDouble("sale_price"));
                c.setThumbnailUrl(rs.getString("thumbnail_url"));
                c.setLevel(rs.getString("level"));
                c.setStatus(rs.getString("status"));
                courses.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    public List<Course> getMyCourses(int userId) {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT c.*, e.progress_percent " +
                "FROM courses c " +
                "INNER JOIN enrollments e ON c.course_id = e.course_id " +
                "WHERE e.user_id = ?";

        try (Connection conn = DatabaseConnect.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Course c = new Course();
                c.setCourseId(rs.getInt("course_id"));
                c.setTitle(rs.getString("title"));
                c.setThumbnailUrl(rs.getString("thumbnail_url"));
                c.setPrice(rs.getDouble("price"));
                c.setProgressPercent(rs.getInt("progress_percent"));
                c.setCategoryName("Đã sở hữu");
                list.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Hàm phụ trợ để map dữ liệu Course cho gọn code
    private Course mapCourse(ResultSet rs) throws SQLException {
        Course c = new Course();
        c.setCourseId(rs.getInt("course_id"));
        c.setTitle(rs.getString("title"));
        c.setDescription(rs.getString("description"));
        c.setPrice(rs.getDouble("price"));
        c.setSalePrice(rs.getDouble("sale_price"));
        c.setThumbnailUrl(rs.getString("thumbnail_url"));
        c.setLevel(rs.getString("level"));

        try {
            String catName = rs.getString("category_name");
            c.setCategoryName(catName != null ? catName : "Chưa phân loại");
        } catch (SQLException ignored) {
        } // Bỏ qua nếu không có cột này

        if (c.getThumbnailUrl() == null || c.getThumbnailUrl().isEmpty()) {
            c.setThumbnailUrl(getClass().getResource("/Images/default.png").toExternalForm());
        }
        return c;
    }

    // --- 2. CÁC HÀM XỬ LÝ NỘI DUNG KHÓA HỌC (Video, Đăng ký) ---

    public List<Section> getCurriculum(int courseId) {
        List<Section> sections = new ArrayList<>();
        String sql = "SELECT s.section_id, s.title as section_title, l.title as lesson_title " +
                "FROM sections s " +
                "LEFT JOIN lessons l ON s.section_id = l.section_id " +
                "WHERE s.course_id = ? " +
                "ORDER BY s.order_index, l.order_index";

        try (Connection conn = DatabaseConnect.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();

            Section currentSection = null;
            int currentSectionId = -1;

            while (rs.next()) {
                int secId = rs.getInt("section_id");

                if (secId != currentSectionId) {
                    currentSection = new Section(secId, rs.getString("section_title"));
                    sections.add(currentSection);
                    currentSectionId = secId;
                }

                String lessonTitle = rs.getString("lesson_title");
                if (lessonTitle != null && currentSection != null) {
                    currentSection.addLesson(lessonTitle);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sections;
    }

    public boolean registerCourse(int userId, int courseId) {
        String checkSql = "SELECT COUNT(*) FROM enrollments WHERE user_id = ? AND course_id = ?";
        String insertSql = "INSERT INTO enrollments (user_id, course_id, enrolled_at, progress_percent, status) VALUES (?, ?, NOW(), 0, 'active')";

        try (Connection conn = DatabaseConnect.getConnection()) {
            PreparedStatement psCheck = conn.prepareStatement(checkSql);
            psCheck.setInt(1, userId);
            psCheck.setInt(2, courseId);
            ResultSet rs = psCheck.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                return false; // Đã đăng ký rồi
            }

            PreparedStatement psInsert = conn.prepareStatement(insertSql);
            psInsert.setInt(1, userId);
            psInsert.setInt(2, courseId);

            int rows = psInsert.executeUpdate();
            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- 3. CÁC HÀM XỬ LÝ BÀI TẬP CODING (QUAN TRỌNG) ---

    // Lấy danh sách bài tập của khóa học
    public List<CodingExercise> getExercises(int courseId) {
        List<CodingExercise> list = new ArrayList<>();
        String sql = "SELECT * FROM coding_exercises WHERE course_id = ?";

        try (Connection conn = DatabaseConnect.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String lang = rs.getString("language");
                if (lang == null || lang.isEmpty())
                    lang = "java";

                // Sử dụng Constructor đầy đủ (bao gồm course_id và language)
                list.add(new CodingExercise(
                        rs.getInt("exercise_id"),
                        rs.getInt("course_id"), // Lấy course_id từ DB
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("starter_code"),
                        rs.getString("expected_output"),
                        lang));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // [MỚI] Tìm bài tập tiếp theo để chuyển bài
    public CodingExercise getNextExercise(int currentCourseId, int currentExerciseId) {
        String sql = "SELECT * FROM coding_exercises WHERE course_id = ? AND exercise_id > ? ORDER BY exercise_id ASC LIMIT 1";

        try (Connection conn = DatabaseConnect.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentCourseId);
            stmt.setInt(2, currentExerciseId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String lang = rs.getString("language");
                if (lang == null || lang.isEmpty())
                    lang = "java";

                return new CodingExercise(
                        rs.getInt("exercise_id"),
                        rs.getInt("course_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("starter_code"),
                        rs.getString("expected_output"),
                        lang);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void handleExerciseCompletion(int userId, int exerciseId, int courseId, int pointsToAdd) {
        String checkSql = "SELECT COUNT(*) FROM exercise_completions WHERE user_id = ? AND exercise_id = ?";
        String insertSql = "INSERT INTO exercise_completions (user_id, exercise_id, course_id) VALUES (?, ?, ?)";
        String updatePointsSql = "UPDATE users SET points = points + ? WHERE id = ?";

        try (Connection conn = DatabaseConnect.getConnection()) {
            PreparedStatement psCheck = conn.prepareStatement(checkSql);
            psCheck.setInt(1, userId);
            psCheck.setInt(2, exerciseId);
            ResultSet rs = psCheck.executeQuery();

            if (rs.next() && rs.getInt(1) == 0) {

                PreparedStatement psInsert = conn.prepareStatement(insertSql);
                psInsert.setInt(1, userId);
                psInsert.setInt(2, exerciseId);
                psInsert.setInt(3, courseId);
                psInsert.executeUpdate();

                PreparedStatement psPoints = conn.prepareStatement(updatePointsSql);
                psPoints.setInt(1, pointsToAdd);
                psPoints.setInt(2, userId);
                psPoints.executeUpdate();

                updateCourseProgress(conn, userId, courseId);

                System.out.println(
                        ">>> User " + userId + ": Hoàn thành bài " + exerciseId + ", Cộng " + pointsToAdd + " điểm.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCourseProgress(Connection conn, int userId, int courseId) throws SQLException {
        String countTotalSql = "SELECT COUNT(*) FROM coding_exercises WHERE course_id = ?";
        String countDoneSql = "SELECT COUNT(*) FROM exercise_completions WHERE user_id = ? AND course_id = ?";
        String updateProgressSql = "UPDATE enrollments SET progress_percent = ? WHERE user_id = ? AND course_id = ?";

        int totalExercises = 0;
        int completedExercises = 0;

        try (PreparedStatement psTotal = conn.prepareStatement(countTotalSql)) {
            psTotal.setInt(1, courseId);
            ResultSet rsTotal = psTotal.executeQuery();
            if (rsTotal.next())
                totalExercises = rsTotal.getInt(1);
        }

        if (totalExercises == 0)
            return;

        try (PreparedStatement psDone = conn.prepareStatement(countDoneSql)) {
            psDone.setInt(1, userId);
            psDone.setInt(2, courseId);
            ResultSet rsDone = psDone.executeQuery();
            if (rsDone.next())
                completedExercises = rsDone.getInt(1);
        }

        int percent = (int) (((double) completedExercises / totalExercises) * 100);

        try (PreparedStatement psUpdate = conn.prepareStatement(updateProgressSql)) {
            psUpdate.setInt(1, percent);
            psUpdate.setInt(2, userId);
            psUpdate.setInt(3, courseId);
            psUpdate.executeUpdate();
        }
    }

    public org.example.model.Course getCourseById(int courseId) {
        String sql = "SELECT * FROM courses WHERE course_id = ?";
        try (java.sql.Connection conn = org.example.connect.DatabaseConnect.getConnection();
                java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);
            java.sql.ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                org.example.model.Course c = new org.example.model.Course();
                c.setCourseId(rs.getInt("course_id"));
                c.setTitle(rs.getString("title"));
                c.setDescription(rs.getString("description"));
                c.setThumbnailUrl(rs.getString("thumbnail_url"));
                return c;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void markLessonAsCompleted(int userId, int courseId, String lessonName) {
        String sql = "INSERT IGNORE INTO lesson_progress (user_id, course_id, lesson_name) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnect.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, courseId);
            stmt.setString(3, lessonName);
            stmt.executeUpdate();

            updateEnrollmentProgressTotal(userId, courseId);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getCompletedLessonCount(int userId, int courseId) {
        String sql = "SELECT COUNT(*) FROM lesson_progress WHERE user_id = ? AND course_id = ?";
        try (Connection conn = DatabaseConnect.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, courseId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalLessonCount(int courseId) {
        String sqlVideo = "SELECT COUNT(*) FROM lessons l JOIN sections s ON l.section_id = s.section_id WHERE s.course_id = ?";
        String sqlCode = "SELECT COUNT(*) FROM coding_exercises WHERE course_id = ?";

        int total = 0;
        try (Connection conn = DatabaseConnect.getConnection()) {
            // Đếm video
            try (PreparedStatement ps1 = conn.prepareStatement(sqlVideo)) {
                ps1.setInt(1, courseId);
                ResultSet rs1 = ps1.executeQuery();
                if (rs1.next())
                    total += rs1.getInt(1);
            }
            // Đếm bài code
            try (PreparedStatement ps2 = conn.prepareStatement(sqlCode)) {
                ps2.setInt(1, courseId);
                ResultSet rs2 = ps2.executeQuery();
                if (rs2.next())
                    total += rs2.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    private void updateEnrollmentProgressTotal(int userId, int courseId) {
        int completed = getCompletedLessonCount(userId, courseId);
        int total = getTotalLessonCount(courseId);

        if (total == 0)
            return;

        int percent = (int) (((double) completed / total) * 100);
        if (percent > 100)
            percent = 100;

        String sql = "UPDATE enrollments SET progress_percent = ? WHERE user_id = ? AND course_id = ?";
        try (Connection conn = DatabaseConnect.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, percent);
            stmt.setInt(2, userId);
            stmt.setInt(3, courseId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- 4. CÁC HÀM XỬ LÝ ĐÁNH GIÁ KHÓA HỌC ---
    public int getCourseRating(int userId, int courseId) {
        String sql = "SELECT rating FROM enrollments WHERE user_id = ? AND course_id = ?";
        try (Connection conn = DatabaseConnect.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, courseId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("rating");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // 0 means not rated yet or error
    }

    public boolean updateCourseRating(int userId, int courseId, int rating) {
        String sql = "UPDATE enrollments SET rating = ? WHERE user_id = ? AND course_id = ?";
        try (Connection conn = DatabaseConnect.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, rating);
            stmt.setInt(2, userId);
            stmt.setInt(3, courseId);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}