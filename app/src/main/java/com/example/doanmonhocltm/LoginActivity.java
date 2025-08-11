package com.example.doanmonhocltm;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.doanmonhocltm.callapi.ApiClient;
import com.example.doanmonhocltm.callapi.ApiService;
import com.example.doanmonhocltm.callapi.SessionManager;
import com.example.doanmonhocltm.model.FcmToken;
import com.example.doanmonhocltm.model.LoginHistory;
import com.example.doanmonhocltm.model.LoginRequest;
import com.example.doanmonhocltm.model.Person;
import com.example.doanmonhocltm.model.ResultLogin;
import com.example.doanmonhocltm.model.User;
import com.example.doanmonhocltm.util.DeviceUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private TextInputEditText edtUsername;
    private TextInputEditText edtPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressBar;
    private ApiService apiService;
    private SessionManager sessionManager;

    // Permission launcher for notification permission
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "Notification permission granted");
                    // Permission granted, continue with app flow
                } else {
                    Log.d(TAG, "Notification permission denied");
                    showPermissionDeniedDialog();
                }
            });

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        setupWindowInsets();

        initializeViews();
        initializeServices();
        setupEventListeners();

        // Check notification permission when app starts
        checkNotificationPermission();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Show rationale dialog before requesting permission
                showPermissionRationaleDialog();
            }
        } else {
            // For older versions, check if notifications are enabled
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                showNotificationSettingsDialog();
            }
        }
    }

    private void showPermissionRationaleDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cấp quyền thông báo")
                .setMessage("Ứng dụng cần quyền thông báo để gửi các thông tin quan trọng đến bạn. Bạn có muốn cấp quyền không?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                    }
                })
                .setNegativeButton("Từ chối", (dialog, which) -> {
                    showPermissionDeniedDialog();
                })
                .setCancelable(false)
                .show();
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Quyền thông báo bị từ chối")
                .setMessage("Ứng dụng không thể hoạt động mà không có quyền thông báo. Ứng dụng sẽ được đóng.")
                .setPositiveButton("Thoát ứng dụng", (dialog, which) -> {
                    finish();
                    System.exit(0);
                })
                .setNegativeButton("Thử lại", (dialog, which) -> {
                    checkNotificationPermission();
                })
                .setCancelable(false)
                .show();
    }

    private void showNotificationSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Thông báo bị tắt")
                .setMessage("Vui lòng bật thông báo trong cài đặt để tiếp tục sử dụng ứng dụng.")
                .setPositiveButton("Mở cài đặt", (dialog, which) -> {
                    Intent intent = new Intent();
                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                    intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
                    startActivity(intent);
                })
                .setNegativeButton("Thoát ứng dụng", (dialog, which) -> {
                    finish();
                    System.exit(0);
                })
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check notification permission again when returning to the activity
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                showNotificationSettingsDialog();
            }
        }
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeServices() {
        this.apiService = ApiClient.getClient(LoginActivity.this).create(ApiService.class);
        this.sessionManager = new SessionManager(LoginActivity.this);
    }

    private void initializeViews() {
        this.edtUsername = findViewById(R.id.edtUsername);
        this.edtPassword = findViewById(R.id.edtPassword);
        this.btnLogin = findViewById(R.id.btnLogin);
        this.progressBar = findViewById(R.id.progressBar);
    }

    private void setupEventListeners() {
        btnLogin.setOnClickListener(v -> {
            // Check notification permission before login
            if (hasNotificationPermission()) {
                handleLoginButtonClick();
            } else {
                checkNotificationPermission();
            }
        });
    }

    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return NotificationManagerCompat.from(this).areNotificationsEnabled();
        }
    }

    private void handleLoginButtonClick() {
        String username = edtUsername.getText().toString();
        String password = edtPassword.getText().toString();
        Log.e("username", username);
        Log.e("password", password);

        if (!validateLoginInput(username, password)) {
            return;
        }

        showLoading(true);
        performLogin(username, password);
    }

    private boolean validateLoginInput(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Vui lòng nhập đầy đủ thông tin đăng nhập", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void performLogin(String username, String password) {
        LoginRequest loginRequest = new LoginRequest(username, password);
        Call<ResultLogin> resultLogin = apiService.login(loginRequest);

        resultLogin.enqueue(new Callback<ResultLogin>() {
            @Override
            public void onResponse(Call<ResultLogin> call, Response<ResultLogin> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleSuccessfulLogin(response.body());
                } else {
                    handleFailedLogin(response.code());
                }
            }

            @Override
            public void onFailure(Call<ResultLogin> call, Throwable t) {
                handleServerError();
            }
        });
    }

    private void handleSuccessfulLogin(ResultLogin result) {
        String token = result.getToken();
        String username = result.getUsername();
        String userId = result.getId();

        sessionManager.saveToken(token);
        fetchUserDetails(userId, token, username);
    }

    private void fetchUserDetails(String userId, String token, String username) {
        Call<Person> resultFaceRecognition = apiService.getPersonById(userId);

        resultFaceRecognition.enqueue(new Callback<Person>() {
            @Override
            public void onResponse(Call<Person> call, Response<Person> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String fullName = response.body().getFullName();
                    if (fullName != null) {
                        fetchUserEmail(userId, token, username, fullName);
                    } else {
                        showLoading(false);
                        Toast.makeText(LoginActivity.this, "Tên người dùng không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this, "Không lấy được thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Person> call, Throwable t) {
                handleServerError();
            }
        });
    }

    private void fetchUserEmail(String userId, String token, String username, String fullName) {
        Call<User> getUserMailCall = apiService.getUserMail(userId);

        getUserMailCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String email = response.body().getEmail();
                    if (email != null) {
                        fetchFacePath(userId, token, username, fullName, email);
                    } else {
                        showLoading(false);
                        Toast.makeText(LoginActivity.this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this, "Không lấy được email người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                handleServerError();
            }
        });
    }

    private void fetchFacePath(String userId, String token, String username, String fullName, String email) {
        Call<Person> facePathCall = apiService.getPersonById(userId);
        facePathCall.enqueue(new Callback<Person>() {

            @Override
            public void onResponse(Call<Person> call, Response<Person> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String facePath = response.body().getFacePath();
                    if (facePath != null) {
                        fetchUserImage(userId, token, username, fullName, email, facePath);
                    } else {
                        showLoading(false);
                        Toast.makeText(LoginActivity.this, "Không tìm thấy ảnh người dùng", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this, "Lỗi lấy facePath", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Person> call, Throwable t) {
                handleServerError();
            }
        });
    }

    private void fetchUserImage(String userId, String token, String username, String fullName, String email, String facePathCall) {
        Call<ResponseBody> imageCall = apiService.getImage(facePathCall);

        imageCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        byte[] imageBytes = response.body().bytes();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                        saveUserSession(userId, token, username, fullName, email, imageBytes);
                        logLoginHistory(userId);
                    } catch (IOException e) {
                        e.printStackTrace();
                        showLoading(false);
                        Toast.makeText(LoginActivity.this, "Lỗi xử lý hình ảnh", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showLoading(false);
                    Log.e("API", "Image not found or error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                showLoading(false);
                Log.e("API", "Request failed: " + t.getMessage());
            }
        });
    }

    private void saveUserSession(String userId, String token, String username, String fullName,
                                 String email, byte[] imageBytes) {
        sessionManager.saveUserSession(token, userId, username, fullName, email);
        sessionManager.saveImageToPrefs(imageBytes);
    }

    private void logLoginHistory(String userId) {
        LoginHistory loginHistory = new LoginHistory(
                userId,
                DeviceUtil.getIPAddress(true),
                DeviceUtil.getDeviceInfo(),
                "SUCCESS"
        );

        Call<LoginHistory> loginHistoryCall = apiService.createLoginHistory(loginHistory);
        loginHistoryCall.enqueue(new Callback<LoginHistory>() {
            @Override
            public void onResponse(Call<LoginHistory> call, Response<LoginHistory> response) {
                if (response.isSuccessful()) {
                    navigateToMainScreen();
                } else {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this, "Lỗi ghi nhật ký đăng nhập", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginHistory> call, Throwable t) {
                handleServerError();
            }
        });
    }

    private void navigateToMainScreen() {
        showLoading(false);
        Toast.makeText(LoginActivity.this, "Đăng Nhập Thành Công", Toast.LENGTH_SHORT).show();

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        // Vẫn chuyển màn hình dù lấy token thất bại
                        proceedToMainActivity();
                        return;
                    }

                    String token = task.getResult();
                    Log.d(TAG, "FCM Token: " + token);

                    String userId = sessionManager.getUserId();

                    // Gửi token về server Spring Boot để lưu
                    Call<FcmToken> fcmTokenCall = apiService.postCreateFcmToken(new FcmToken(token, userId));

                    fcmTokenCall.enqueue(new Callback<FcmToken>() {
                        @Override
                        public void onResponse(Call<FcmToken> call, Response<FcmToken> response) {
                            // Chuyển màn hình bất kể response có thành công hay không
                            proceedToMainActivity();
                        }

                        @Override
                        public void onFailure(Call<FcmToken> call, Throwable t) {
                            // Vẫn chuyển màn hình dù gửi FCM token thất bại
                            Log.e(TAG, "Failed to send FCM token: " + t.getMessage());
                            proceedToMainActivity();
                        }
                    });
                });
    }

    private void proceedToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, FindLicensePlateActivity.class);
        startActivity(intent);
        finish(); // Đóng LoginActivity khi đã đăng nhập thành công
    }

    private void handleFailedLogin(int responseCode) {
        showLoading(false);
        System.out.println("❌ Đăng nhập thất bại: " + responseCode);
        Toast.makeText(LoginActivity.this, "Tên Đăng Nhập Hoặc Mật Khẩu Không Đúng", Toast.LENGTH_SHORT).show();
    }

    private void handleServerError() {
        showLoading(false);
        Toast.makeText(LoginActivity.this, "Server Đang Gặp Lỗi", Toast.LENGTH_SHORT).show();
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);
            btnLogin.setText("Đang đăng nhập...");
        } else {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
            btnLogin.setText("Đăng nhập");
        }
    }
}