package com.example.restaurant_food_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OrderDetailsActivity extends AppCompatActivity {

    private TextView customerNameText, orderSizeText, addressText, itemsText, totalAmountText;
    private Button completeOrderButton;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        customerNameText = findViewById(R.id.customerNameText);
        orderSizeText = findViewById(R.id.orderSizeText);
        addressText = findViewById(R.id.addressText);
        itemsText = findViewById(R.id.itemsText);
        totalAmountText = findViewById(R.id.totalAmountText);
        completeOrderButton = findViewById(R.id.completeOrderButton);

        orderId = getIntent().getStringExtra("orderId");

        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, "No order ID found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchOrderDetails(orderId);

        completeOrderButton.setOnClickListener(v -> markOrderComplete(orderId));
    }

    private void fetchOrderDetails(String orderId) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://192.168.138.89:3000/orders/" + orderId)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(OrderDetailsActivity.this, "Failed to fetch order", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(OrderDetailsActivity.this, "Error fetching order details", Toast.LENGTH_SHORT).show());
                    return;
                }

                String responseBody = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject order = new JSONObject(responseBody);

                        customerNameText.setText("Customer: " + order.getString("customerName"));
                        orderSizeText.setText("Total Items: " + order.getJSONArray("items").length());

                        JSONObject address = order.getJSONObject("address");
                        String fullAddress = address.getString("street") + ", " +
                                address.getString("city") + ", " +
                                address.getString("state") + " - " +
                                address.getString("zip");

                        addressText.setText("Address: " + fullAddress);

                        JSONArray itemsArray = order.getJSONArray("items");
                        StringBuilder itemsBuilder = new StringBuilder();
                        for (int i = 0; i < itemsArray.length(); i++) {
                            JSONObject item = itemsArray.getJSONObject(i);
                            itemsBuilder.append("- ").append(item.getString("dishName"))
                                    .append(" x ").append(item.getInt("quantity"))
                                    .append(" = ₹").append(item.getDouble("price"))
                                    .append("\n");
                        }

                        itemsText.setText(itemsBuilder.toString().trim());
                        totalAmountText.setText("Total: ₹" + order.getDouble("totalAmount"));

                    } catch (JSONException e) {
                        Toast.makeText(OrderDetailsActivity.this, "Error parsing order data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void markOrderComplete(String orderId) {
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("orderId", orderId)
                .build();

        Request request = new Request.Builder()
                .url("http://192.168.138.89:3000/orders/complete")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(OrderDetailsActivity.this, "Failed to complete order", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(OrderDetailsActivity.this, "Order Completed!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(OrderDetailsActivity.this, RestaurantHomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    });
                }
            }
        });
    }
}
