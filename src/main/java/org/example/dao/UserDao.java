package org.example.dao;

import org.example.connect.DatabaseConnect;
import org.example.model.User;
import org.example.util.SecurityUtil;

import java.sql.*;

public class UserDao {
    public boolean register(User newUser) {
        String sql = "INSERT INTO users (user_name, full_name, password_hash, email, role, avatar_url, is_active) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try(Connection conn = DatabaseConnect.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newUser.getUsername());
            stmt.setString(2, newUser.getFullname());
            stmt.setString(3, newUser.getPasswordHash());
            stmt.setString(4, newUser.getEmail());
            stmt.setString(5,newUser.getRole());
            stmt.setString(6,newUser.getAvatarUrl());
            stmt.setBoolean(7,true);

            return stmt.executeUpdate() >0;
        }
        catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String generateOTP(){
        int randomPin = (int) (Math.random()*900000) + 100000;
        return String.valueOf(randomPin);
    }

    public boolean isEmailExists(String email) {
        String sql = "SELECT user_id FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();

            return rs.next();
        }
        catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public User login(String loginKey, String password) {
        String sql = "SELECT * FROM users WHERE (email = ? OR user_name = ?) AND password_hash = ?";

        try (Connection conn = DatabaseConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, loginKey);
            stmt.setString(2, loginKey);
            stmt.setString(3, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {

                boolean isActive = rs.getBoolean("is_active");

                if (!isActive) {
                    System.out.println("User " + loginKey + " is banned or inactive.");
                    return null;
                }

                int id = rs.getInt("user_id");
                String username = rs.getString("user_name");
                String fullname = rs.getString("full_name");
                String emailDB = rs.getString("email");
                String passwordHash = rs.getString("password_hash");
                String role = rs.getString("role");
                String avatarUrl = rs.getString("avatar_url");

                return new User(id, username, fullname, emailDB, passwordHash, role, avatarUrl, isActive);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("user_id"),
                        rs.getString("user_name"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("role"),
                        rs.getString("avatar_url"),
                        rs.getBoolean("is_active")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isUsernameExists(String username) {
        String sql = "SELECT user_id FROM users WHERE user_name = ?";
        try (Connection conn = DatabaseConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePassword(String email, String newPassword) {
        String sql = "UPDATE users SET password_hash = ? WHERE email = ?";

        try(Connection conn = DatabaseConnect.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, newPassword);
            stmt.setString(2, email);

            int rowsAffected = stmt.executeUpdate();
            return  rowsAffected > 0;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkPassword(int userId, String currentPassword) {
        String sql = "SELECT password_hash FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String hashedInput = SecurityUtil.hashPassword(currentPassword);
                return storedHash.equals(hashedInput);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean changePassword(int userId, String newPassword) {
        String hashedPassword = SecurityUtil.hashPassword(newPassword);

        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hashedPassword);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateProfile(int userId, String fullName, String avatarUrl) {
        String sql = "UPDATE users SET full_name = ?, avatar_url = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fullName);
            stmt.setString(2, avatarUrl);
            stmt.setInt(3, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
