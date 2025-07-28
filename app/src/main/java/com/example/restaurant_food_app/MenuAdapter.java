package com.example.restaurant_food_app;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MenuAdapter extends ArrayAdapter<MenuItem> {

    private Context context;
    private List<MenuItem> menuItems;
    private String restaurantId;

    public MenuAdapter(Context context, List<MenuItem> menuItems, String restaurantId) {
        super(context, R.layout.menu_item_layout, menuItems);
        this.context = context;
        this.menuItems = menuItems;
        this.restaurantId = restaurantId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView = inflater.inflate(R.layout.menu_item_layout, parent, false);

        MenuItem item = menuItems.get(position);

        ImageView dishImage = rowView.findViewById(R.id.dishImage);
        TextView dishName = rowView.findViewById(R.id.dishName);
        TextView dishPrice = rowView.findViewById(R.id.dishPrice);
        Switch availabilitySwitch = rowView.findViewById(R.id.availabilitySwitch);

        Picasso.get().load(item.getImageUrl()).into(dishImage);
        dishName.setText(item.getDishName());
        dishPrice.setText(String.format("₹%.2f", item.getPrice()));
        availabilitySwitch.setChecked(item.isAvailable());

        availabilitySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateAvailability(item.getId(), isChecked);
        });

        return rowView;
    }

    private void updateAvailability(String itemId, boolean isAvailable) {
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("isAvailable", String.valueOf(isAvailable))
                .build();

        Request request = new Request.Builder()
                .url("http://192.168.138.89:3000/restaurants/" + restaurantId +
                        "/menu/items/" + itemId)
                .patch(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ((Activity)context).runOnUiThread(() -> {
                    Toast.makeText(context, "Failed to update availability", Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged(); // Refresh list on failure
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                ((Activity)context).runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(context,
                                "Availability updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context,
                                "Failed to update availability", Toast.LENGTH_SHORT).show();
                        notifyDataSetChanged(); // Refresh list on failure
                    }
                });
            }
        });
    }
}