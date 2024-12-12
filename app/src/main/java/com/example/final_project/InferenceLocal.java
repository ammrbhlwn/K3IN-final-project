package com.example.final_project;

import android.os.Build;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class InferenceLocal {
    public interface InferenceCallback {
        void onResult(String detectedClass);
        void onError(Exception e);
    }

    public void Infer(String bitmapPath, InferenceCallback inferenceCallback) {
        new Thread(() -> {
            try {
                File file = new File(bitmapPath);

                if (!file.exists()) {
                    inferenceCallback.onError(new FileNotFoundException("File not found: " + bitmapPath));
                    return;
                }

                String encodedFile;
                try (FileInputStream fileInputStreamReader = new FileInputStream(file)) {
                    byte[] bytes = new byte[(int) file.length()];
                    fileInputStreamReader.read(bytes);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        encodedFile = Base64.getEncoder().encodeToString(bytes);
                    } else {
                        inferenceCallback.onError(new Exception("Base64 encoding not supported"));
                        return;
                    }
                }

                String uploadURL = "https://detect.roboflow.com/occupational-health-and-safety/2?api_key=xGN6YojLgOgSpQireQaH&name=" + file.getName();

                URL url = new URL(uploadURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setDoOutput(true);

                try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                    wr.writeBytes("image=" + encodedFile);
                    wr.flush();
                }

                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line).append("\n");
                    }
                }

                JSONObject jsonObject = new JSONObject(response.toString());
                JSONArray predictions = jsonObject.getJSONArray("predictions");

                if (predictions.length() > 0) {
                    String detectedClass = predictions.getJSONObject(0).getString("class");
                    inferenceCallback.onResult(detectedClass);
                } else {
                    inferenceCallback.onError(new Exception("No predictions found in API response"));
                }
            } catch (Exception e) {
                inferenceCallback.onError(e);
            }
        }).start();
    }
}