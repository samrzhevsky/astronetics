package ru.samrzhevsky.astronetics;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.provider.Settings;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

import ru.samrzhevsky.astronetics.data.Constants;

public class VkAuth {
    private static String codeVerifier;
    private static String codeChallenge;
    private static String state;

    private static void generateParameters() {
        state = UUID.randomUUID().toString();

        // generate codeVerifier
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifierBytes = new byte[128];
        secureRandom.nextBytes(codeVerifierBytes);
        codeVerifier = Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifierBytes);

        // convert string to sha256
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(codeVerifier.getBytes());
            codeChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void startNewIntent(Context context) {
        generateParameters();

        // create auth uri
        Resources resources = context.getResources();
        int vkAppId = resources.getInteger(R.integer.vk_app_id);
        String uriStr = "https://id.vk.com/auth?" +
                        "state=" + state + "&" +
                        "response_type=code&" +
                        "code_challenge=" + codeChallenge + "&" +
                        "code_challenge_method=sha256&" +
                        "app_id=" + vkAppId + "&" +
                        "v=0.0.2&" +
                        "redirect_uri=vk" + vkAppId + "://vk.com&" +
                        "uuid=" + UUID.randomUUID();

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriStr));
        context.startActivity(intent);
    }

    @SuppressLint("HardwareIds")
    public static String exchangeCode(Context context, String code) {
        try {
            URL url = new URL(Constants.API_URL + "?action=exchange");

            JSONObject requestBody = new JSONObject();
            requestBody.put("device_id", Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
            requestBody.put("code", code);
            requestBody.put("code_verifier", codeVerifier);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            OutputStream outputStream = connection.getOutputStream();
            byte[] requestBodyBytes = requestBody.toString().getBytes();
            outputStream.write(requestBodyBytes);
            outputStream.flush();
            outputStream.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject responseJson = new JSONObject(response.toString());
                if (responseJson.getInt("status") != 1) {
                    return responseJson.getString("error");
                }

                // save token
                SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("user_token", responseJson.getString("token"));
                editor.apply();

                return null;
            } else {
                return context.getString(R.string.error_fatal);
            }
        } catch (Exception e) {
            return context.getString(R.string.error_fatal);
        }
    }

    public static boolean validateState(String _state) {
        return state.equals(_state);
    }

    public static String getUserToken(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getString("user_token", null);
    }

    public static void unsetUserToken(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("user_token");
        editor.apply();
    }
}
