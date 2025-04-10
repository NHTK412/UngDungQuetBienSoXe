package com.example.doanmonhocltm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private EditText txt_Phone;
    private EditText txt_Password;
    private Button btn_Login;
    private TextView txt_Forgetpass;
    private Button btn_SMS;
    private TextView txt_Hoac;

    // Mẫu cho số điện thoại (đơn giản hóa: 9-11 chữ số)
    private static final String PHONE_PATTERN = "\\d{9,11}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Ánh xạ các view với ID
        txt_Phone = findViewById(R.id.txt_SDT);
        txt_Password = findViewById(R.id.txt_Password);
        btn_Login = findViewById(R.id.btn_Login);
        txt_Forgetpass = findViewById(R.id.txt_Forgetpass);
        btn_SMS = findViewById(R.id.btn_SMS);
       // txt_Hoac = findViewById(R.id.txt_Hoac);

        // Xử lý sự kiện khi nhấn nút đăng nhập
        btn_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = txt_Phone.getText().toString().trim();
                String password = txt_Password.getText().toString().trim();

                if (phoneNumber.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                } else if (!phoneNumber.matches(PHONE_PATTERN)) {
                    Toast.makeText(MainActivity.this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
                } else {
                    // Ở đây bạn có thể thêm logic kiểm tra đăng nhập thực tế
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(intent);
                }
            }
        });

        // Xử lý sự kiện kiểm tra số điện thoại khi nhập
        txt_Phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần xử lý
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Kiểm tra định dạng số điện thoại khi người dùng nhập
                String phoneNumber = s.toString().trim();
                if (!phoneNumber.isEmpty() && !phoneNumber.matches(PHONE_PATTERN)) {
                    txt_Phone.setError("Số điện thoại không hợp lệ");
                } else {
                    txt_Phone.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Không cần xử lý
            }
        });

        // Xử lý sự kiện khi nhấn Quên mật khẩu
        txt_Forgetpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        // Xử lý sự kiện khi nhấn nút SMS
        btn_SMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SMSActivity.class);
                startActivity(intent);
            }
        });
    }
}