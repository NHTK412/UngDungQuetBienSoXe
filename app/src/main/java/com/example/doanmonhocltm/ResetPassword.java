package com.example.doanmonhocltm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ResetPassword extends Activity {
    private EditText txt_Reset;
    private EditText txt_Resetpass;
    private Button btn_Agree;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resetpassword);

        // Lấy số điện thoại từ intent nếu có
        phoneNumber = getIntent().getStringExtra("phone_number");

        // Ánh xạ các thành phần giao diện
        txt_Reset = findViewById(R.id.txt_Reset);
        txt_Resetpass = findViewById(R.id.txt_Resetpass);
        btn_Agree = findViewById(R.id.btn_Agree);

        // Thiết lập sự kiện cho nút xác nhận
        btn_Agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lấy giá trị từ các trường nhập liệu
                String newPassword = txt_Reset.getText().toString();
                String confirmPassword = txt_Resetpass.getText().toString();

                // Kiểm tra các trường không được để trống
                if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(ResetPassword.this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Kiểm tra mật khẩu trùng khớp
                if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(ResetPassword.this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Kiểm tra độ phức tạp của mật khẩu (ít nhất 8 ký tự)
                if (newPassword.length() < 8) {
                    Toast.makeText(ResetPassword.this, "Mật khẩu phải có ít nhất 8 ký tự", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Nếu mọi thứ đều đúng, đặt lại mật khẩu
                resetPassword(newPassword);
            }
        });
    }

    private void resetPassword(String newPassword) {
        // Phương thức này nên triển khai logic thay đổi mật khẩu
        // Ví dụ: gọi API hoặc cập nhật cơ sở dữ liệu với số điện thoại và mật khẩu mới

        // Giả lập thành công cho ví dụ này
        Toast.makeText(this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();

        // Chuyển về màn hình MainActivity
        Intent mainIntent = new Intent(this, MainActivity.class);
        // Xóa tất cả các activity trước đó trong stack
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);

        // Đóng activity hiện tại
        finish();
    }
}