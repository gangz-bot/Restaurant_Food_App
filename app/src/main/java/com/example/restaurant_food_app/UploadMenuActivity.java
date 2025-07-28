package com.example.restaurant_food_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadMenuActivity extends AppCompatActivity {

    private EditText dishNameInput, dishPriceInput, dishImageUrlInput;
    private Button saveMenuButton, viewMenuButton, homeButton;
    private ListView menuListView;
    private List<MenuItem> menuItems = new ArrayList<>();
    private MenuAdapter menuAdapter;
    private String restaurantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_menu);

        // Get restaurant ID from intent
        restaurantId = getIntent().getStringExtra("restaurantId");
        if (restaurantId == null || restaurantId.isEmpty()) {
            Toast.makeText(this, "Error: No restaurant ID found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize views
        dishNameInput = findViewById(R.id.dishNameInput);
        dishPriceInput = findViewById(R.id.dishPriceInput);
        dishImageUrlInput = findViewById(R.id.dishImageUrlInput);
        saveMenuButton = findViewById(R.id.saveMenuButton);
        viewMenuButton = findViewById(R.id.viewMenuButton);
        homeButton = findViewById(R.id.homeButton);
        menuListView = findViewById(R.id.menuListView);

        // Initialize adapter
        menuAdapter = new MenuAdapter(this, menuItems, restaurantId);
        menuListView.setAdapter(menuAdapter);

        // Set click listeners
        saveMenuButton.setOnClickListener(v -> saveMenuItem());
        viewMenuButton.setOnClickListener(v -> fetchMenuItems());
        homeButton.setOnClickListener(v -> goToHome());

        // Load menu items on activity start
        fetchMenuItems();
    }

    private void saveMenuItem() {
        String dishName = dishNameInput.getText().toString().trim();
        String priceStr = dishPriceInput.getText().toString().trim();
        String imageUrl = dishImageUrlInput.getText().toString().trim();

        if (dishName.isEmpty() || priceStr.isEmpty() || imageUrl.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("dishName", dishName)
                .add("price", priceStr)
                .add("imageUrl", imageUrl)
                .build();

        Request request = new Request.Builder()
                .url("http://192.168.138.89:3000/restaurants/" + restaurantId + "/menu")
                .post(formBody)
                .build();

        saveMenuButton.setEnabled(false);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    saveMenuButton.setEnabled(true);
                    Toast.makeText(UploadMenuActivity.this,
                            "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseBody = response.body() != null ? response.body().string() : "{}";
                runOnUiThread(() -> {
                    saveMenuButton.setEnabled(true);

                    if (response.isSuccessful()) {
                        Toast.makeText(UploadMenuActivity.this,
                                "Menu item saved!", Toast.LENGTH_SHORT).show();
                        clearInputs();
                        fetchMenuItems();
                    } else {
                        Toast.makeText(UploadMenuActivity.this,
                                "Failed to save: " + responseBody, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void fetchMenuItems() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://192.168.138.89:3000/restaurants/" + restaurantId + "/menu")
                .header("Accept", "application/json") // Add this line to ensure JSON response
                .build();

        viewMenuButton.setEnabled(false);
        Toast.makeText(this, "Refreshing menu...", Toast.LENGTH_SHORT).show();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    viewMenuButton.setEnabled(true);
                    Toast.makeText(UploadMenuActivity.this,
                            "Failed to fetch menu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    viewMenuButton.setEnabled(true);

                    if (response.isSuccessful()) {
                        try {
                            String responseBody = response.body().string();
                            JSONArray menuArray = new JSONArray(responseBody);
                            menuItems.clear();

                            for (int i = 0; i < menuArray.length(); i++) {
                                JSONObject item = menuArray.getJSONObject(i);
                                menuItems.add(new MenuItem(
                                        item.getString("_id"),
                                        item.getString("dishName"),
                                        item.getDouble("price"),
                                        item.getString("imageUrl"),
                                        item.optBoolean("isAvailable", true)
                                ));
                            }

                            menuAdapter.notifyDataSetChanged();
                            Toast.makeText(UploadMenuActivity.this,
                                    "Menu refreshed!", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(UploadMenuActivity.this,
                                    "Error parsing menu data", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(UploadMenuActivity.this,
                                "Failed to fetch menu. Server error.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void goToHome() {
        Intent intent = new Intent(this, RestaurantHomeActivity.class);
        intent.putExtra("restaurantId", restaurantId); // Pass the restaurantId to home
        startActivity(intent);
        finish();
    }

    private void clearInputs() {
        dishNameInput.setText("");
        dishPriceInput.setText("");
        dishImageUrlInput.setText("");
        dishNameInput.requestFocus();
    }
}