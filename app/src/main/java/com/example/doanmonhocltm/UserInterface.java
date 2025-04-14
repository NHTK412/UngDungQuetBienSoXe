package com.example.doanmonhocltm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UserInterface extends AppCompatActivity {

    // UI Components
    private Button btnQuayLai;
    private EditText beHoVaTen, beID, beGioiTinh, beNgaySinh, beDiaChiThuongTru, beSoDienThoai;

    // API and Network
    private static final String API_ENDPOINT_GET_USER = "YOUR_API_ENDPOINT_FOR_USER_INFO"; // nhớ 15/4 thay URL API
    private static final String TAG = "UserInterface";
    private OkHttpClient client = new OkHttpClient();

    // Có thể lưu ID người dùng từ màn hình trước đó (có thể là từ kết quả nhận dạng khuôn mặt)
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_interface);

        // Khởi tạo UI components
        initViews();
        setupListeners();

        // Nhận dữ liệu từ Intent nếu có
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("user_id")) {
            userId = intent.getStringExtra("user_id");
        }

        // Lấy thông tin người dùng từ database
        fetchUserDataFromDatabase();
    }

    private void initViews() {
        // UI
        btnQuayLai = findViewById(R.id.btnQuayLai);
        beHoVaTen = findViewById(R.id.beHoVaTen);
        beID = findViewById(R.id.beID);
        beGioiTinh = findViewById(R.id.beGioiTinh);
        beNgaySinh = findViewById(R.id.beNgaySinh);
        beDiaChiThuongTru = findViewById(R.id.beDiaChiThuongTru);
        beSoDienThoai = findViewById(R.id.beSoDienThoai);

        // Thiết lập trạng thái chỉ đọc cho các trường thông tin
        setFieldsReadOnly(true);
    }

    //quay lai
    private void setupListeners() {
        // Thiết lập sự kiện cho nút Quay lại
        btnQuayLai.setOnClickListener(v -> {
            finish();
        });
    }

    private void setFieldsReadOnly(boolean readOnly) {
        //  EditText: chir được đọc
        beHoVaTen.setEnabled(!readOnly);
        beID.setEnabled(!readOnly);
        beGioiTinh.setEnabled(!readOnly);
        beNgaySinh.setEnabled(!readOnly);
        beDiaChiThuongTru.setEnabled(!readOnly);
        beSoDienThoai.setEnabled(!readOnly);
    }

    private void fetchUserDataFromDatabase() {
        // Hiển thị thông báo đang tải dữ liệu
        Toast.makeText(this, "Đang tải thông tin người dùng...", Toast.LENGTH_SHORT).show();

        // Phương pháp 1: Sử dụng OkHttp để gọi API lấy dữ liệu
        // Tạo URL với userId
        String url = API_ENDPOINT_GET_USER;
        if (userId != null && !userId.isEmpty()) {
            url += "?id=" + userId; // ỦL theo API
        }

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error fetching user data: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(UserInterface.this,
                            "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();

                    //demo
                    displaySampleData();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        final String responseData = response.body().string();
                        JSONObject jsonData = new JSONObject(responseData);

                        // Xử lý dữ liệu trả về
                        if (jsonData.optBoolean("success", false)) {
                            JSONObject userData = jsonData.getJSONObject("data");

                            // Cập nhật UI trên main thread
                            runOnUiThread(() -> {
                                try {
                                    displayUserData(userData);
                                    Toast.makeText(UserInterface.this,
                                            "Đã tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    Log.e(TAG, "Error parsing user data: " + e.getMessage());
                                    Toast.makeText(UserInterface.this,
                                            "Lỗi hiển thị dữ liệu", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            String message = jsonData.optString("message", "Không tìm thấy thông tin người dùng");
                            runOnUiThread(() -> {
                                Toast.makeText(UserInterface.this, message, Toast.LENGTH_SHORT).show();
                                displaySampleData(); // Hiển thị dữ liệu mẫu trong trường hợp không tìm thấy
                            });
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing API response: " + e.getMessage());
                        runOnUiThread(() -> {
                            Toast.makeText(UserInterface.this,
                                    "Lỗi xử lý dữ liệu từ server", Toast.LENGTH_SHORT).show();
                            displaySampleData();
                        });
                    }
                } else {
                    Log.e(TAG, "API error: " + response.code());
                    runOnUiThread(() -> {
                        Toast.makeText(UserInterface.this,
                                "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                        displaySampleData();
                    });
                }
            }
        });

        // Phương pháp 2: Sử dụng Room Database trực tiếp (xem phần alternateMethod)
    }

    private void displayUserData(JSONObject userData) throws JSONException {
        // Điền dữ liệu vào các trường
        beHoVaTen.setText(userData.optString("full_name", ""));
        beID.setText(userData.optString("id", ""));
        beGioiTinh.setText(userData.optString("gender", ""));
        beNgaySinh.setText(userData.optString("birth_date", ""));
        beDiaChiThuongTru.setText(userData.optString("address", ""));
        beSoDienThoai.setText(userData.optString("phone", ""));

        // Thiết lập các trường thành chỉ đọc
        setFieldsReadOnly(true);
    }

    private void displaySampleData() {
        // Hiển thị dữ liệu mẫu trong trường hợp không thể kết nối API
        // Chỉ sử dụng cho mục đích phát triển/testing
        beHoVaTen.setText("Hoang Gia Bao Test");
        beID.setText("123456789");
        beGioiTinh.setText("Nam");
        beNgaySinh.setText("11/08/2005");
        beDiaChiThuongTru.setText("40/21 - Duong vao tim em, Quận Quen Loi Ve, TP.HCM");
        beSoDienThoai.setText("0388661185");

        // Thiết lập các trường thành chỉ đọc
        setFieldsReadOnly(true);
    }

    // Phương pháp thay thế sử dụng Room Database
    private void alternateMethod() {
        // Nếu bạn đang sử dụng Room Database, bạn có thể truy vấn cơ sở dữ liệu cục bộ như sau:
        /*
        // Giả sử bạn đã định nghĩa AppDatabase và UserDao
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        UserDao userDao = db.userDao();

        // Sử dụng ExecutorService để thực hiện truy vấn cơ sở dữ liệu trên background thread
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            // Truy vấn User từ database
            User user = userDao.getUserById(userId);

            // Cập nhật UI trên main thread
            handler.post(() -> {
                if (user != null) {
                    beHoVaTen.setText(user.getFullName());
                    beID.setText(user.getId());
                    beGioiTinh.setText(user.getGender());
                    beNgaySinh.setText(user.getBirthDate());
                    beDiaChiThuongTru.setText(user.getAddress());
                    beSoDienThoai.setText(user.getPhone());

                    Toast.makeText(this, "Đã tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                    displaySampleData();
                }
            });
        });
        */
    }
}