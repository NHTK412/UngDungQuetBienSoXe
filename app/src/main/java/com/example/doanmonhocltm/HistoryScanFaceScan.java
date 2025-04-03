package com.example.doanmonhocltm;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HistoryScanFaceScan extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_scan_face_scan);

        // Thêm nút quay lại
        Button btnBack = findViewById(R.id.btnBack); // Giả sử bạn có nút có ID là btnBack
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Khi nhấn nút quay lại sẽ đóng Activity hiện tại và quay về màn hình trước
            }
        });
    }
}