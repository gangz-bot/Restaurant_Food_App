package com.example.restaurant_food_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class RegisterActivity extends AppCompatActivity {

    private EditText emailRegister, passwordRegister, restaurantName;
    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailRegister = findViewById(R.id.emailRegister);
        passwordRegister = findViewById(R.id.passwordRegister);
        restaurantName = findViewById(R.id.restaurantName);

        findViewById(R.id.registerSubmitButton).setOnClickListener(v -> {
            v.setEnabled(false); // Disable button during registration
            performRegistration();
        });
    }

    private void performRegistration() {
        String email = emailRegister.getText().toString().trim();
        String password = passwordRegister.getText().toString().trim();
        String restaurant = restaurantName.getText().toString().trim();

        Log.d(TAG, "Registration attempt - Email: " + email + ", Name: " + restaurant);

        // Validate inputs
        if (!validateInputs(email, password, restaurant)) {
            findViewById(R.id.registerSubmitButton).setEnabled(true);
            return;
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .add("name", restaurant)
                .build();

        Request request = new Request.Builder()
                .url("http://192.168.138.89:3000/restaurants/register")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Registration failed - Network error: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this,
                            "Network error. Please try again.", Toast.LENGTH_LONG).show();
                    findViewById(R.id.registerSubmitButton).setEnabled(true);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseBody = response.body() != null ? response.body().string() : "{}";
                Log.d(TAG, "Registration response - Code: " + response.code() + ", Body: " + responseBody);

                runOnUiThread(() -> {
                    findViewById(R.id.registerSubmitButton).setEnabled(true);

                    try {
                        if (response.isSuccessful()) {
                            handleSuccessfulRegistration(responseBody, email);
                        } else {
                            handleFailedRegistration(responseBody, response.code());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing response", e);
                        Toast.makeText(RegisterActivity.this,
                                "Registration error occurred", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private boolean validateInputs(String email, String password, String restaurant) {
        if (email.isEmpty() || password.isEmpty() || restaurant.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void handleSuccessfulRegistration(String responseBody, String email) throws JSONException {
        JSONObject json = new JSONObject(responseBody);
        String restaurantId = json.optString("_id", "");
        String message = json.optString("message", "Registration Successful");

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        // Navigate to RestaurantInfoActivity with both email and restaurantId
        Intent intent = new Intent(this, RestaurantInfoActivity.class);
        intent.putExtra("email", email);
        if (!restaurantId.isEmpty()) {
            intent.putExtra("restaurantId", restaurantId);
        }
        startActivity(intent);
        finish();
    }

    private void handleFailedRegistration(String responseBody, int statusCode) {
        try {
            String errorMsg = "Registration Failed";
            if (!responseBody.isEmpty()) {
                JSONObject json = new JSONObject(responseBody);
                errorMsg = json.optString("message", errorMsg);
            }
            Toast.makeText(this, errorMsg + " (Code: " + statusCode + ")", Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            Toast.makeText(this, "Registration Failed (Code: " + statusCode + ")", Toast.LENGTH_LONG).show();
        }
    }
}