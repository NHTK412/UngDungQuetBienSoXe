package com.example.doanmonhocltm;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;

public class HomeActivity extends Activity {

    private TextView txt_Name;
    private TextView txt_Birthday;
    private TextView txt_Gender;
    private Button btn_Update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user); // Đảm bảo tên layout trùng khớp

        // Ánh xạ các view
        txt_Name = findViewById(R.id.txtfullName);
        txt_Birthday = findViewById(R.id.txtbirthDay);
        txt_Gender = findViewById(R.id.txtGender);
        btn_Update = findViewById(R.id.btn_Update);

        // Lấy dữ liệu từ Intent nếu có
        String userEmail = getIntent().getStringExtra("USER_EMAIL");

        // Xử lý sự kiện khi nhấn nút Cập nhật thông tin
        btn_Update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = txt_Name.getText().toString().trim();
                String birthday = txt_Birthday.getText().toString().trim();
                String gender = txt_Gender.getText().toString().trim();

                if (name.isEmpty()) {
                    Toast.makeText(HomeActivity.this, "Vui lòng nhập tên", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Ở đây bạn có thể thêm code lưu thông tin người dùng vào database hoặc sharedPreferences

                Toast.makeText(HomeActivity.this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
            }
        });
    }
}