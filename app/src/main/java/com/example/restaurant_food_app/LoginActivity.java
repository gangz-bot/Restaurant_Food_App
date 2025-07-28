package com.example.restaurant_food_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    private EditText emailLogin, passwordLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailLogin = findViewById(R.id.emailLogin);
        passwordLogin = findViewById(R.id.passwordLogin);

        findViewById(R.id.loginSubmitButton).setOnClickListener(v -> performLogin());
    }

    private void performLogin() {
        String email = emailLogin.getText().toString();
        String password = passwordLogin.getText().toString();

        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .build();

        Request request = new Request.Builder()
                .url("http://192.168.138.89:3000/restaurants/login")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Login", "Login Failed", e);
                runOnUiThread(() ->
                        Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseBody = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        String restaurantId = json.optString("_id", "");

                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, RestaurantHomeActivity.class);
                            intent.putExtra("restaurantId", restaurantId);
                            startActivity(intent);
                            finish();
                        });

                    } catch (JSONException e) {
                        runOnUiThread(() ->
                                Toast.makeText(LoginActivity.this, "Error parsing login response", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
