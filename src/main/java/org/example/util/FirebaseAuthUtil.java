package org.example.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class FirebaseAuthUtil {

    private static final String FIREBASE_API_KEY = "AIzaSyC5mqx0tUP4KUlC9ST1MVF-SiKHOl5vb1M";

    // REST API URLs
    private static final String SIGN_UP_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key="
            + FIREBASE_API_KEY;
    private static final String SIGN_IN_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key="
            + FIREBASE_API_KEY;
    private static final String SIGN_IN_WITH_IDP_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key="
            + FIREBASE_API_KEY;

    /**
     * Creates a new user in Firebase Auth with email and password.
     * 
     * @return Firebase ID Token if successful, or null on error.
     */
    public static String registerUserWithEmailPassword(String email, String password) throws Exception {
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("email", email);
        jsonBody.addProperty("password", password);
        jsonBody.addProperty("returnSecureToken", true);

        String response = sendPostRequest(SIGN_UP_URL, jsonBody.toString());
        JsonObject responseObj = JsonParser.parseString(response).getAsJsonObject();

        if (responseObj.has("idToken")) {
            return responseObj.get("idToken").getAsString();
        } else {
            throw new Exception("Firebase Register Error: " + responseObj.toString());
        }
    }

    /**
     * Signs in a user to Firebase Auth with email and password.
     * 
     * @return Firebase ID Token if successful, or null on error.
     */
    public static String signInWithEmailPassword(String email, String password) throws Exception {
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("email", email);
        jsonBody.addProperty("password", password);
        jsonBody.addProperty("returnSecureToken", true);

        String response = sendPostRequest(SIGN_IN_URL, jsonBody.toString());
        JsonObject responseObj = JsonParser.parseString(response).getAsJsonObject();

        if (responseObj.has("idToken")) {
            return responseObj.get("idToken").getAsString();
        } else {
            throw new Exception("Firebase Login Error: " + responseObj.toString());
        }
    }

    /**
     * Authenticates with Firebase using a Google OAuth ID Token (from the Google
     * standard OAuth flow)
     */
    public static String signInWithGoogleIdToken(String idToken) throws Exception {
        String postBody = "postBody="
                + java.net.URLEncoder.encode("id_token=" + idToken + "&providerId=google.com", "UTF-8")
                + "&requestUri=http://localhost"
                + "&returnIdpCredential=true"
                + "&returnSecureToken=true";
        return signInWithIdp(postBody);
    }

    /**
     * Authenticates with Firebase using a Facebook Access Token
     */
    public static String signInWithFacebookToken(String accessToken) throws Exception {
        String postBody = "postBody="
                + java.net.URLEncoder.encode("access_token=" + accessToken + "&providerId=facebook.com", "UTF-8")
                + "&requestUri=http://localhost"
                + "&returnIdpCredential=true"
                + "&returnSecureToken=true";
        return signInWithIdp(postBody);
    }

    private static String signInWithIdp(String body) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SIGN_IN_WITH_IDP_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject responseObj = JsonParser.parseString(response.body()).getAsJsonObject();

        if (responseObj.has("idToken")) {
            return responseObj.get("idToken").getAsString();
        } else {
            throw new Exception("Firebase OAuth Login Error: " + responseObj.toString());
        }
    }

    private static String sendPostRequest(String urlString, String jsonBody) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
