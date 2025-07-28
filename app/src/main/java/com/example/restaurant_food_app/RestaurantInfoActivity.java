package com.example.restaurant_food_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RestaurantInfoActivity extends AppCompatActivity {

    private EditText addressInput, openTimeInput, closeTimeInput;
    private Button saveButton;
    private String restaurantEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_info);

        // Get email from registration
        restaurantEmail = getIntent().getStringExtra("email");
        if (restaurantEmail == null || restaurantEmail.isEmpty()) {
            Toast.makeText(this, "Error: No restaurant email found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        addressInput = findViewById(R.id.addressInput);
        openTimeInput = findViewById(R.id.openTimeInput);
        closeTimeInput = findViewById(R.id.closeTimeInput);
        saveButton = findViewById(R.id.saveButton);

        saveButton.setOnClickListener(v -> saveRestaurantInfo());
    }

    private void saveRestaurantInfo() {
        String address = addressInput.getText().toString().trim();
        String openTime = openTimeInput.getText().toString().trim();
        String closeTime = closeTimeInput.getText().toString().trim();

        if (address.isEmpty() || openTime.isEmpty() || closeTime.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("email", restaurantEmail)
                .add("address", address)
                .add("openTime", openTime)
                .add("closeTime", closeTime)
                .build();

        Request request = new Request.Builder()
                .url("http://192.168.138.89:3000/restaurants/info")
                .post(formBody)
                .build();

        saveButton.setEnabled(false);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    saveButton.setEnabled(true);
                    Toast.makeText(RestaurantInfoActivity.this,
                            "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseBody = response.body() != null ? response.body().string() : "{}";
                runOnUiThread(() -> {
                    saveButton.setEnabled(true);

                    if (response.isSuccessful()) {
                        try {
                            JSONObject json = new JSONObject(responseBody);
                            String restaurantId = json.getString("_id");

                            Toast.makeText(RestaurantInfoActivity.this,
                                    "Info Saved Successfully", Toast.LENGTH_SHORT).show();

                            // Pass to UploadMenuActivity with restaurantId
                            Intent intent = new Intent(RestaurantInfoActivity.this, UploadMenuActivity.class);
                            intent.putExtra("restaurantId", restaurantId);
                            startActivity(intent);
                            finish();
                        } catch (JSONException e) {
                            Toast.makeText(RestaurantInfoActivity.this,
                                    "Error processing response", Toast.LENGTH_SHORT).show();
                            Log.e("RestaurantInfo", "Error parsing JSON", e);
                        }
                    } else {
                        Toast.makeText(RestaurantInfoActivity.this,
                                "Failed to save info: " + responseBody, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}