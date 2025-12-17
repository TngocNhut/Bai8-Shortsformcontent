package com.tngocnhat.shortformcontents;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Views
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView tvEmail = findViewById(R.id.tvProfileEmail);
        TextView tvTotalVideos = findViewById(R.id.tvTotalVideosCount);

        // Set static data for now (as per mockup reqs)
        tvEmail.setText("user@example.com");
        tvTotalVideos.setText("10");

        // Back Button functionality
        btnBack.setOnClickListener(v -> {
            finish(); // Return to previous activity (MainActivity)
        });
    }
}
