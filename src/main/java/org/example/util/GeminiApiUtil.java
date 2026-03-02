package org.example.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.example.util.Config;

public class GeminiApiUtil {

    private static final String API_KEY = Config.get("gemini.api_key");

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
            + API_KEY;

    public static String getGeminiResponse(String prompt) {
        if (API_KEY.equals("YOUR_API_KEY_HERE")) {
            return "Lỗi: Bạn chưa cấu hình API Key của Google Gemini trong file GeminiApiUtil.java. Vui lòng lấy Key tại Google AI Studio và thay thế biến API_KEY.";
        }

        try {

            JsonObject textPart = new JsonObject();
            textPart.addProperty("text", prompt);

            JsonArray partsArray = new JsonArray();
            partsArray.add(textPart);

            JsonObject contentObj = new JsonObject();
            contentObj.add("parts", partsArray);

            JsonArray contentsArray = new JsonArray();
            contentsArray.add(contentObj);

            JsonObject requestBody = new JsonObject();
            requestBody.add("contents", contentsArray);

            String requestBodyString = requestBody.toString();

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {

                JsonObject responseJson = JsonParser.parseString(response.body()).getAsJsonObject();
                JsonArray candidates = responseJson.getAsJsonArray("candidates");
                if (candidates != null && candidates.size() > 0) {
                    JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
                    JsonObject content = firstCandidate.getAsJsonObject("content");
                    if (content != null) {
                        JsonArray parts = content.getAsJsonArray("parts");
                        if (parts != null && parts.size() > 0) {
                            return parts.get(0).getAsJsonObject().get("text").getAsString();
                        }
                    }
                }
            } else {
                return "Lỗi từ Gemini API (Code " + response.statusCode() + "): " + response.body();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Đã xảy ra lỗi khi kết nối với máy chủ AI: " + e.getMessage();
        }

        return "Xin lỗi, tôi không thể trả lời lúc này.";
    }
}
