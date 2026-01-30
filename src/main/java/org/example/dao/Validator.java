package org.example.dao;

import java.util.regex.Pattern;

public class Validator {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
    private static final String PHONE_REGEX = "^0\\d{9}$";
    private static final String NAME_REGEX = "^[\\p{L} .'-]+$";
    private static final String PASSWORD_STRONG_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        return Pattern.matches(EMAIL_REGEX, email);
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return false;
        return Pattern.matches(PHONE_REGEX, phone);
    }

    public static boolean isValidName(String name) {
        if (name == null || name.trim().length() < 2 || name.trim().length() > 50) {
            return false;
        }
        return Pattern.matches(NAME_REGEX, name);
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) return false;
        return true;
        // Dùng khi test

//        if (password == null) return false;
//        return Pattern.matches(PASSWORD_STRONG_REGEX, password);
        // Dùng khi đưa vào hoạt động
    }
}
