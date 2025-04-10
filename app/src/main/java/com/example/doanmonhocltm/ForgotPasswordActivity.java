package com.example.doanmonhocltm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText txtPhoneNumber;
    private Button btnSend;
    private TextView txtReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgetpass);

        // Ánh xạ các view từ XML
        txtPhoneNumber = findViewById(R.id.txt_Edit); // EditText for phone number input
        txtReturn = findViewById(R.id.txt_Return); // TextView for "Quay lại"
        btnSend = findViewById(R.id.btn_GuiMa);

        // Xử lý sự kiện khi nhấn "Quay lại"
        txtReturn.setOnClickListener(v -> {
            // Quay lại màn hình đăng nhập
            Intent intent = new Intent(ForgotPasswordActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Đóng activity hiện tại
        });

        // Xử lý sự kiện khi nhấn nút "Gửi"
        btnSend.setOnClickListener(v -> {
            String phoneNumber = txtPhoneNumber.getText().toString().trim();

            if (phoneNumber.isEmpty()) {
                Toast.makeText(ForgotPasswordActivity.this, "Vui lòng nhập số điện thoại!", Toast.LENGTH_SHORT).show();
            } else if (!isValidPhoneNumber(phoneNumber)) {
                Toast.makeText(ForgotPasswordActivity.this, "Số điện thoại không hợp lệ!", Toast.LENGTH_SHORT).show();
            } else {
                // Xử lý gửi mã xác nhận qua SMS
                Toast.makeText(ForgotPasswordActivity.this, "Đã gửi mã xác nhận tới số điện thoại của bạn!", Toast.LENGTH_SHORT).show();

                // Chuyển đến màn hình nhập mã xác nhận
                Intent intent = new Intent(ForgotPasswordActivity.this, VerifyCodeActivity.class);
                // Truyền số điện thoại để sử dụng trong quá trình xác thực
                intent.putExtra("account_data", phoneNumber);
                startActivity(intent);
            }
        });
    }

    // Phương thức kiểm tra số điện thoại hợp lệ
    private boolean isValidPhoneNumber(String phoneNumber) {
        // Đơn giản hóa: kiểm tra độ dài trong khoảng 9-11 số và chỉ chứa ký tự số
        return phoneNumber.matches("\\d{9,11}");
    }
}