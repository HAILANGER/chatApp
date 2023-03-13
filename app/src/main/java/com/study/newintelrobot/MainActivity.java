package com.study.newintelrobot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private ChatGPTService chatGPTService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendMessage("Hello, how are you?");
    }

    private void sendMessage(String message) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Create a URL object
                    URL url = new URL("https://api.openai.com/v1/completions");

                    // Create a HttpURLConnection object
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Authorization", "Bearer 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");

                    // Create a JSON object with the request data
                    JSONObject requestData = new JSONObject();

                    requestData.put("model", "text-davinci-003");
                    requestData.put("prompt", "今天星期几");
                    requestData.put("temperature", 0.9);
                    requestData.put("max_tokens", 2048);
                    requestData.put("top_p", 1);
                    requestData.put("frequency_penalty", 0.0);
                    requestData.put("presence_penalty", 0.6);

                    // Convert the JSON object to a byte array
                    byte[] requestDataBytes = requestData.toString().getBytes("UTF-8");

                    // Set the content length of the request
                    connection.setRequestProperty("Content-Length", String.valueOf(requestDataBytes.length));

                    // Send the request
                    connection.setDoOutput(true);
                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                    outputStream.write(requestDataBytes);
                    outputStream.flush();
                    outputStream.close();

                    // Get the response code
                    int responseCode = connection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // If the response code is 200, read the response data
                        InputStream inputStream = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        String line;
                        StringBuilder responseBuilder = new StringBuilder();
                        while ((line = reader.readLine()) != null) {
                            responseBuilder.append(line);
                        }
                        String responseData = responseBuilder.toString();
                        reader.close();
                        inputStream.close();

                        // Handle the response data
                        Log.d("MainActivity", "Response: " + responseData);
                    } else {
                        // If the response code is not 200, log an error
                        Log.e("MainActivity", "Error: " + responseCode);
                    }

                    // Disconnect the connection
                    connection.disconnect();

                } catch (Exception e) {
                    // Log any exceptions that occur
                    Log.e("MainActivity", "Error: " + e.getMessage());
                }
            }
        }).start();
    }
}