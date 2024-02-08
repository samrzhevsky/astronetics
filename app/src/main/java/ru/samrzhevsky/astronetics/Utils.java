package ru.samrzhevsky.astronetics;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.TypedValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import ru.samrzhevsky.astronetics.data.Constants;

public class Utils {
    public static String declension(int number, String declension1, String declension3, String declension5) {
        String result;
        int count = number % 100;

        if (count >= 5 && count <= 20) {
            result = declension5;
        } else {
            count = count % 10;
            if (count == 1) {
                result = declension1;
            } else if (count >= 2 && count <= 4) {
                result = declension3;
            } else {
                result = declension5;
            }
        }

        return result;
    }

    public static String timeToString(int time) {
        int ss = time % 60;
        int mm = Math.round(time / 60f) % 60;
        int hh = Math.round(time / 3600f) % 24;
        int dd = Math.round(time / 86400f);
        ArrayList<String> t = new ArrayList<>();

        if (dd != 0) t.add(dd + " д.");
        if (hh != 0 || (dd != 0 && mm != 0)) t.add(hh+ " ч.");
        if (dd == 0 && hh == 0) t.add(mm + " мин.");
        if (time < 60) t.add(ss + "c.");

        return String.join(" ", t);
    }

    public static int dp2px(Context context, int dp) {
        return Math.round(
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        dp,
                        context.getResources().getDisplayMetrics()
                )
        );
    }

    public static boolean isNetworkUnavailable(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        return networkInfo == null || !networkInfo.isConnected();
    }

    @SuppressLint("HardwareIds")
    public static String apiGetRequest(Context context, String action, String urlParams) throws IOException {
        String urlStr = Constants.API_URL + "?action=" + action + "&token=" + VkAuth.getUserToken(context);
        if (urlParams != null) {
            urlStr += "&" + urlParams;
        }

        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } else {
            throw new IOException("GET request failed with response code " + responseCode);
        }
    }

    @SuppressLint("HardwareIds")
    public static String apiPostRequest(Context context, String action, String urlParams, String requestBody) throws IOException {
        String urlStr = Constants.API_URL + "?action=" + action + "&token=" + VkAuth.getUserToken(context);
        if (urlParams != null) {
            urlStr += "&" + urlParams;
        }

        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        OutputStream outputStream = connection.getOutputStream();
        byte[] requestBodyBytes = requestBody.getBytes();
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

            return response.toString();
        } else {
            throw new IOException("POST request failed with response code " + responseCode);
        }
    }
}
