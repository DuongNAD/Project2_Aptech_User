package org.example.dao;

import org.example.connect.DatabaseConnect;
import org.example.model.Course;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseDao {

    public List<Course> getAllCourses() {
        List<Course> list = new ArrayList<>();

        String sql = "SELECT * FROM courses WHERE status = 'active'";

        try (Connection conn = DatabaseConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Course c = new Course();

                c.setCourseId(rs.getInt("course_id"));
                c.setTitle(rs.getString("title"));
                c.setDescription(rs.getString("description"));
                c.setPrice(rs.getDouble("price"));
                c.setSalePrice(rs.getDouble("sale_price"));
                c.setThumbnailUrl(rs.getString("thumbnail_url"));
                c.setLevel(rs.getString("level"));

                int cateId = rs.getInt("category_id");

                if (cateId == 1) {
                    c.setCategoryName("Lập trình");
                } else if (cateId == 2) {
                    c.setCategoryName("Thiết kế");
                } else {
                    c.setCategoryName("Chung");
                }

                if (c.getThumbnailUrl() == null || c.getThumbnailUrl().isEmpty()) {

                    c.setThumbnailUrl(getClass().getResource("/Images/default.png").toExternalForm());
                }

                list.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Course> getCoursesFiltered(int categoryId) {
        List<Course> list = new ArrayList<>();

        String sql = (categoryId == 0)
        ? "SELECT * FROM courses ORDER BY view_count DESC"
        : "SELECT * FROM courses WHERE category_id = ? ORDER BY view_count DESC";

        try(Connection conn = DatabaseConnect.getConnection();
            PreparedStatement stmt =conn.prepareStatement(sql)){
            if(categoryId != 0) {
                stmt.setInt(1, categoryId);
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Course());
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Course> getCoursesByCategoryId(int categoryId) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses WHERE category_id = ?";

        try (Connection conn = org.example.connect.DatabaseConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                courses.add(new Course(
                        rs.getInt("course_id"),
                        rs.getInt("category_id"),
                        rs.getString("title"),
                        rs.getString("subtitle"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getDouble("sale_price"),
                        rs.getString("thumbnail_url"),
                        rs.getString("level"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }
}