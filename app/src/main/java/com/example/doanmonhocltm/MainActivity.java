package com.example.doanmonhocltm;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends Activity {
    private EditText txt_Phone;
    private EditText txt_Password;
    private Button btn_Login;
    private TextView txt_Forgetpass;
    private Button btn_SMS;
    //private TextView txt_Hoac;
    private static final String TAG = "MainActivity";

    // Mẫu cho số điện thoại (đơn giản hóa: 9-11 chữ số)
    private static final String PHONE_PATTERN = "\\d{9,11}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Ánh xạ các view với ID
        txt_Phone = findViewById(R.id.txtPhoneNumber);
        txt_Password = findViewById(R.id.txtPassword);
        btn_Login = findViewById(R.id.btnLogin);
        txt_Forgetpass = findViewById(R.id.txtForgetpass);
        btn_SMS = findViewById(R.id.btnSMS);
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
                    // Gọi API đăng nhập
                    callLoginApi(phoneNumber, password);
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

        // Thêm nút hoặc chức năng gọi API để test
        Button btnTestApi = new Button(this);
        btnTestApi.setText("Test API");
        // Thêm nút này vào layout của bạn hoặc gọi API khi cần

        btnTestApi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gọi API khi nhấn nút
                callSignupApi("123456789000", "userA", "userA@example.com", "123456");
            }
        });
    }

    /**
     * Hàm gọi API đăng nhập người dùng
     * @param username Tên người dùng
     * @param password Mật khẩu
     */
    private void callLoginApi(String username, String password) {
        OkHttpClient client = new OkHttpClient();

        // Tạo JSON body cho request
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", username);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Lỗi tạo dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo request body
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

        // Tạo request
        Request request = new Request.Builder()
                .url("http://localhost:8087/quet/api/auth/signin")
                .post(body)
                .build();

        // Thực hiện request bất đồng bộ
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Xử lý khi request thất bại
                Log.e(TAG, "API call failed: " + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Lỗi kết nối API: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Lấy response body
                final String responseData = response.body().string();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Login successful: " + responseData);
                            Toast.makeText(MainActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                            // Xử lý dữ liệu trả về, có thể lưu thông tin đăng nhập
                            saveLoginSession(responseData);

                            // Chuyển đến trang chính
                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish(); // Đóng màn hình đăng nhập
                        } else {
                            Log.e(TAG, "Login failed: " + response.code() + " - " + responseData);
                            Toast.makeText(MainActivity.this, "Đăng nhập thất bại: Sai tên đăng nhập hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    /**
     * Lưu thông tin phiên đăng nhập
     * @param jsonResponse Phản hồi JSON từ API
     */
    private void saveLoginSession(String jsonResponse) {
        try {
            JSONObject userJson = new JSONObject(jsonResponse);

            // Lưu thông tin người dùng vào SharedPreferences
            // Ví dụ: token, user ID, username, v.v.
            SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // Giả sử API trả về token và thông tin người dùng
            if (userJson.has("accessToken")) {
                editor.putString("token", userJson.getString("accessToken"));
            }

            if (userJson.has("username")) {
                editor.putString("username", userJson.getString("username"));
            }

            // Lưu thêm các thông tin khác nếu cần

            editor.apply();

            Log.d(TAG, "Đã lưu thông tin đăng nhập thành công");
        } catch (JSONException e) {
            Log.e(TAG, "Lỗi xử lý dữ liệu JSON: " + e.getMessage());
            Toast.makeText(this, "Lỗi xử lý dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Hàm gọi API đăng ký người dùng
     * @param id ID người dùng
     * @param username Tên người dùng
     * @param email Email người dùng
     * @param password Mật khẩu
     */
    private void callSignupApi(String id, String username, String email, String password) {
        OkHttpClient client = new OkHttpClient();

        // Tạo JSON body cho request
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("id", id);
            jsonBody.put("username", username);
            jsonBody.put("email", email);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Lỗi tạo dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo request body
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

        // Tạo request
        Request request = new Request.Builder()
                .url("http://localhost:8087/quet/api/auth/signup")
                .post(body)
                .build();

        // Thực hiện request bất đồng bộ
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Xử lý khi request thất bại
                Log.e(TAG, "API call failed: " + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Lỗi kết nối API: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Lấy response body
                final String responseData = response.body().string();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "API call successful: " + responseData);
                            Toast.makeText(MainActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

                            // Xử lý dữ liệu trả về ở đây, ví dụ lưu vào SQLite
                            saveUserToDatabase(responseData);
                        } else {
                            Log.e(TAG, "API call failed: " + response.code() + " - " + responseData);
                            Toast.makeText(MainActivity.this, "Đăng ký thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    /**
     * Lưu thông tin người dùng từ API vào cơ sở dữ liệu cục bộ
     * @param jsonResponse Phản hồi JSON từ API
     */
    private void saveUserToDatabase(String jsonResponse) {
        try {
            JSONObject userJson = new JSONObject(jsonResponse);
            String id = userJson.getString("id");
            String username = userJson.getString("username");
            String email = userJson.getString("email");
            String password = userJson.getString("password");

            // Bạn cần tạo một DatabaseHelper để lưu dữ liệu
            // DatabaseHelper dbHelper = new DatabaseHelper(this);
            // dbHelper.addUser(id, username, email, password);

            Log.d(TAG, "Đã lưu người dùng vào database: " + username);
        } catch (JSONException e) {
            Log.e(TAG, "Lỗi xử lý dữ liệu JSON: " + e.getMessage());
            Toast.makeText(this, "Lỗi xử lý dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}