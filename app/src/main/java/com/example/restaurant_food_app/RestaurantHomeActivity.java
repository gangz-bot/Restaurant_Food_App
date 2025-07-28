package com.example.restaurant_food_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RestaurantHomeActivity extends AppCompatActivity {

    private LinearLayout ordersContainer;
    private List<Order> ordersList = new ArrayList<>();
    private String restaurantId;
    private ImageButton refreshButton;

    static class Order {
        String id;
        String customerName;
        int orderSize;

        Order(String id, String customerName, int orderSize) {
            this.id = id;
            this.customerName = customerName;
            this.orderSize = orderSize;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_home);

        Toolbar toolbar = findViewById(R.id.homeToolbar);
        setSupportActionBar(toolbar);

        ordersContainer = findViewById(R.id.ordersContainer);
        refreshButton = findViewById(R.id.refreshOrdersButton);

        restaurantId = getIntent().getStringExtra("restaurantId");

        if (restaurantId == null || restaurantId.isEmpty()) {
            Toast.makeText(this, "Error: No restaurant ID found", Toast.LENGTH_LONG).show();
        }

        refreshButton.setOnClickListener(v -> fetchOrdersFromBackend());

        fetchOrdersFromBackend(); // Initial fetch
    }

    private void fetchOrdersFromBackend() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://192.168.138.89:3000/orders")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(RestaurantHomeActivity.this,
                                "Failed to fetch orders", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(RestaurantHomeActivity.this,
                                    "Error loading orders", Toast.LENGTH_SHORT).show());
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    JSONArray jsonArray = new JSONArray(responseBody);

                    ordersList.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject order = jsonArray.getJSONObject(i);
                        String id = order.getString("_id");
                        String name = order.getString("customerName");
                        int size = order.getJSONArray("items").length();
                        ordersList.add(new Order(id, name, size));
                    }

                    runOnUiThread(() -> displayOrders());

                } catch (JSONException e) {
                    runOnUiThread(() ->
                            Toast.makeText(RestaurantHomeActivity.this,
                                    "Error parsing orders", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void displayOrders() {
        ordersContainer.removeAllViews();

        if (ordersList.isEmpty()) {
            TextView noOrdersView = new TextView(this);
            noOrdersView.setText("No current orders.");
            noOrdersView.setTextSize(18);
            noOrdersView.setPadding(24, 60, 24, 24);
            ordersContainer.addView(noOrdersView);
            return;
        }

        for (Order order : ordersList) {
            View orderView = getLayoutInflater().inflate(R.layout.order_item_layout, null);

            TextView customerName = orderView.findViewById(R.id.customerName);
            TextView orderSize = orderView.findViewById(R.id.orderSize);
            Button completeOrderButton = orderView.findViewById(R.id.completeOrderButton);

            customerName.setText(order.customerName);
            orderSize.setText("Items: " + order.orderSize);

            completeOrderButton.setOnClickListener(v -> {
                Intent intent = new Intent(RestaurantHomeActivity.this, OrderDetailsActivity.class);
                intent.putExtra("orderId", order.id);
                intent.putExtra("customerName", order.customerName);
                intent.putExtra("orderSize", order.orderSize);
                startActivity(intent);
            });

            ordersContainer.addView(orderView);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.updateMenuOption) {
            Intent intent = new Intent(this, UploadMenuActivity.class);
            intent.putExtra("restaurantId", restaurantId);
            startActivity(intent);
            return true;
        }
        if (item.getItemId() == R.id.sortOrdersOption) {
            ordersList.sort(Comparator.comparingInt(o -> o.orderSize));
            displayOrders();
            Toast.makeText(this, "Orders sorted by size", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
