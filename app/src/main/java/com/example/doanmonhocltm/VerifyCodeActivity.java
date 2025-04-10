package com.example.doanmonhocltm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class VerifyCodeActivity extends Activity {

    private EditText[] codeBoxes = new EditText[6];
    private Button btn_Verify;
    private TextView txt_QuayLai;
    private TextView txt_ResendCode;
    private String phoneNumber;
    private CountDownTimer resendTimer;
    private static final int RESEND_TIMEOUT = 60000; // 60 seconds in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verify);

        // Lấy dữ liệu từ intent
        phoneNumber = getIntent().getStringExtra("account_data");

        // Ánh xạ các view
        codeBoxes[0] = findViewById(R.id.code_box_1);
        codeBoxes[1] = findViewById(R.id.code_box_2);
        codeBoxes[2] = findViewById(R.id.code_box_3);
        codeBoxes[3] = findViewById(R.id.code_box_4);
        codeBoxes[4] = findViewById(R.id.code_box_5);
        codeBoxes[5] = findViewById(R.id.code_box_6);

        btn_Verify = findViewById(R.id.btn_Verify);
        txt_QuayLai = findViewById(R.id.txt_QuayLai);
        txt_ResendCode = findViewById(R.id.txt_ResendCode);

        // Thiết lập input listeners cho các ô nhập mã
        setupCodeBoxes();

        // Bắt đầu đếm ngược cho việc gửi lại mã
        startResendTimer();

        // Xử lý sự kiện khi nhấn nút xác nhận
        btn_Verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = getCompleteCode();
                if (code.length() < 6) {
                    Toast.makeText(VerifyCodeActivity.this, "Vui lòng nhập đủ 6 chữ số", Toast.LENGTH_SHORT).show();
                } else {
                    // Xác minh mã
                    verifyCode(code);
                }
            }
        });

        // Xử lý sự kiện khi nhấn "Quay lại"
        txt_QuayLai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Quay lại màn hình trước đó
            }
        });

        // Xử lý sự kiện khi nhấn "Gửi lại mã"
        txt_ResendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txt_ResendCode.isEnabled()) {
                    resendVerificationCode();
                }
            }
        });
    }

    private void setupCodeBoxes() {
        // Thiết lập text watchers cho từng ô
        for (int i = 0; i < codeBoxes.length; i++) {
            final int currentBoxIndex = i;

            codeBoxes[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // Nếu người dùng nhập một ký tự và đây không phải ô cuối cùng
                    if (s.length() == 1 && currentBoxIndex < codeBoxes.length - 1) {
                        // Di chuyển focus tới ô tiếp theo
                        codeBoxes[currentBoxIndex + 1].requestFocus();
                    }

                    // Kiểm tra xem tất cả các ô đã được điền chưa
                    checkAllBoxesFilled();
                }
            });

            // Xử lý sự kiện nhấn nút xóa (backspace)
            codeBoxes[i].setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    // Nếu người dùng nhấn nút xóa và ô hiện tại trống và không phải ô đầu tiên
                    if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (codeBoxes[currentBoxIndex].getText().toString().isEmpty() && currentBoxIndex > 0) {
                            // Di chuyển focus về ô trước đó
                            codeBoxes[currentBoxIndex - 1].requestFocus();
                            codeBoxes[currentBoxIndex - 1].setText("");
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
    }

    private void checkAllBoxesFilled() {
        boolean allFilled = true;
        for (EditText codeBox : codeBoxes) {
            if (codeBox.getText().toString().isEmpty()) {
                allFilled = false;
                break;
            }
        }

        // Có thể tự động kích hoạt nút xác nhận nếu tất cả các ô đã được điền
        // btn_Verify.setEnabled(allFilled);
    }

    private String getCompleteCode() {
        StringBuilder codeBuilder = new StringBuilder();
        for (EditText codeBox : codeBoxes) {
            codeBuilder.append(codeBox.getText().toString());
        }
        return codeBuilder.toString();
    }

    // Bắt đầu bộ đếm thời gian cho việc gửi lại mã
    private void startResendTimer() {
        // Vô hiệu hóa nút gửi lại mã ban đầu
        txt_ResendCode.setEnabled(false);
        txt_ResendCode.setText("Gửi lại mã (60s)");

        resendTimer = new CountDownTimer(RESEND_TIMEOUT, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                txt_ResendCode.setText("Gửi lại mã (" + secondsRemaining + "s)");
            }

            @Override
            public void onFinish() {
                txt_ResendCode.setEnabled(true);
                txt_ResendCode.setText("Gửi lại mã");
            }
        }.start();
    }

    // Gửi lại mã xác nhận
    private void resendVerificationCode() {
        // Trong thực tế, bạn sẽ gọi API để gửi lại mã xác nhận
        Toast.makeText(this, "Đã gửi lại mã xác nhận tới số điện thoại của bạn!", Toast.LENGTH_SHORT).show();

        // Xóa tất cả các ô nhập mã
        for (EditText codeBox : codeBoxes) {
            codeBox.setText("");
        }

        // Focus vào ô đầu tiên
        codeBoxes[0].requestFocus();

        // Bắt đầu lại bộ đếm thời gian
        startResendTimer();
    }

    // Phương thức xác minh mã trong VerifyCodeActivity
    private void verifyCode(String code) {
        // Trong thực tế, bạn sẽ gọi API để xác minh mã
        // Giả lập mã đúng là "123456"
        if (code.equals("123456")) {
            Toast.makeText(this, "Xác nhận thành công!", Toast.LENGTH_SHORT).show();

            // Chuyển đến màn hình đặt mật khẩu mới
            Intent intent = new Intent(this, ResetPassword.class);
            // Truyền số điện thoại nếu cần
            intent.putExtra("phone_number", phoneNumber);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Mã xác nhận không đúng", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy bỏ bộ đếm thời gian khi Activity bị hủy
        if (resendTimer != null) {
            resendTimer.cancel();
        }
    }
}