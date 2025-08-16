package com.example.doanmonhocltm;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.doanmonhocltm.callapi.ApiClient;
import com.example.doanmonhocltm.callapi.ApiService;
import com.example.doanmonhocltm.callapi.SessionManager;
import com.example.doanmonhocltm.model.LocationResponse;
import com.example.doanmonhocltm.model.ResponderStatusRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import org.checkerframework.checker.units.qual.C;

import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Intent;
import android.net.Uri;
import android.Manifest;

public class AccidentDetailActivity extends AppCompatActivity {

    private static final String TAG = "AccidentDetailActivity";
    private TextView tvRoadName;
    private TextView tvAccidentType;
    private TextView tvTimestamp;
    private Chip chipStatus;
    private ImageView ivAccidentImage;

    private int accidentId;

    private BottomNavigationView bottomNavigation;

    private TextView userName;
    private CircleImageView userAvatar;

    private SessionManager sessionManager;

    MaterialButton btnPrimaryAction, btnNavigation, btnCancel;

    private String statusCode;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_accident_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        this.apiService = ApiClient.getClient(AccidentDetailActivity.this).create(ApiService.class);

        this.chipStatus = findViewById(R.id.chipStatus);

        this.tvRoadName = findViewById(R.id.tvRoadName);
        this.tvAccidentType = findViewById(R.id.tvAccidentType);
        this.tvTimestamp = findViewById(R.id.tvTimestamp);
        this.ivAccidentImage = findViewById(R.id.ivAccidentImage);

        this.bottomNavigation = findViewById(R.id.bottomNavigation);
        setupBottomNavigation();
        this.sessionManager = new SessionManager(AccidentDetailActivity.this);

        userName = findViewById(R.id.userName);
        userAvatar = findViewById(R.id.userAvatar);
        setupUserInfo();

        btnPrimaryAction = findViewById(R.id.btnPrimaryAction);
        btnNavigation = findViewById(R.id.btnNavigation);
        btnCancel = findViewById(R.id.btnCancel);

        btnPrimaryAction.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        btnNavigation.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2196F3")));
        btnCancel.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336")));

        Intent intent = getIntent();

        Bundle bundle = intent.getBundleExtra("data");

        this.statusCode = bundle.getString("statusCode");
        updateStatusUI(bundle.getString("statusCode"));

        this.accidentId = bundle.getInt("accidentId");

        this.tvRoadName.setText(bundle.getString("roadName"));
        this.tvTimestamp.setText(bundle.getString("timestamp"));
        this.tvAccidentType.setText(bundle.getString("accidentType"));
        this.chipStatus.setText(bundle.getString("status"));
        this.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(bundle.getInt("color")));

        byte[] imageBytes = bundle.getByteArray("imageAccident");
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        this.ivAccidentImage.setImageBitmap(bitmap);

        // Nút Primary Action
        btnPrimaryAction.setOnClickListener(v -> {
            ResponderStatusRequest responderStatusRequest = new ResponderStatusRequest();
            responderStatusRequest.setAccidentId(bundle.getInt("accidentId"));
            responderStatusRequest.setUnitId(sessionManager.getUserId());
            if (("wait").equals(statusCode)) {
                responderStatusRequest.setStatus("en_route");
            } else if (("en_route").equals(statusCode)) {
                responderStatusRequest.setStatus("arrived");
            }

            Call<Map<String, String>> call = apiService.updateResponderStatus(responderStatusRequest);

            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    Map<String, String> result = response.body();
                    if (("wait").equals(statusCode)) {
                        statusCode = "en_route";
                    } else if (("en_route").equals(statusCode)) {
                        statusCode = "arrived";
                    }
                    int color;
                    switch (statusCode) {
                        case "wait":
                            color = Color.parseColor("#FF9800"); // Orange - cho status "wait" từ JSON
                            break;

                        case "en_route":
                            color = Color.parseColor("#2196F3"); // Blue
                            break;

                        case "arrived":
                            color = Color.parseColor("#4CAF50"); // Green
                            break;

                        case "resolved":

                        case "completed":
                            color = Color.parseColor("#9E9E9E"); // Grey
                            break;

                        default:
                            color = Color.parseColor("#757575"); // Dark Grey
                            break;
                    }
                    String statusText;
                    switch (statusCode) {
                        case "wait":
                            statusText = "Đang chờ";
                            break;
                        case "en_route":
                            statusText = "Đang đến";
                            break;

                        case "arrived":
                            statusText = "Đã đến";
                            break;

                        case "resolved":
                            statusText = "Đã xử lý";
                            break;

                        case "completed":
                            statusText = "Hoàn thành";
                            break;
                        case "cancelled":
                            statusText = "Đã hủy";
                            break;
                        default:
                            statusText = "Không xác định";
                            break;
                    }

                    chipStatus.setText(statusText);
                    chipStatus.setChipBackgroundColor(ColorStateList.valueOf(color));

                    updateStatusUI(statusCode);
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    Toast.makeText(AccidentDetailActivity.this, "Lỗi khi cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Nút Navigation (chỉ hiển thị khi en_route)
        btnNavigation.setOnClickListener(v -> {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(AccidentDetailActivity.this);

            // Kiểm tra quyền
            if (ActivityCompat.checkSelfPermission(AccidentDetailActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(AccidentDetailActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: request permissions
                return;
            }

            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    Log.d("Location", "Lat: " + latitude + ", Lng: " + longitude);

                    // Truyền vào API Retrofit
                    Call<LocationResponse> callLocation = apiService.getLocationCamere(accidentId, latitude, longitude, sessionManager.getUserId());

                    callLocation.enqueue(new Callback<LocationResponse>() {
                        @Override
                        public void onResponse(Call<LocationResponse> call, Response<LocationResponse> response) {
                            Intent intentMap = new Intent(AccidentDetailActivity.this, AccidentMapActivity.class);
                            LocationResponse result = response.body();

                            // Sử dụng intentMap thay vì intent
                            intentMap.putExtra("userLatitude", latitude);
                            intentMap.putExtra("userLongitude", longitude);
                            intentMap.putExtra("accidentLatitude", result.getLatitude());
                            intentMap.putExtra("accidentLongitude", result.getLongitude());
                            intentMap.putExtra("distance", result.getDistanceWithUnit());
                            intentMap.putExtra("currentTime", result.getFormattedDate() + " " + result.getFormattedTime());

                            startActivity(intentMap);
                        }

                        @Override
                        public void onFailure(Call<LocationResponse> call, Throwable t) {
                            Toast.makeText(AccidentDetailActivity.this, "Lỗi khi lấy thông tin vị trí", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e("Location", "Không lấy được vị trí hiện tại");
                    Toast.makeText(AccidentDetailActivity.this, "Không thể lấy vị trí hiện tại", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Nút Cancel
        btnCancel.setOnClickListener(v -> {
            // Hủy thao tác
            ResponderStatusRequest responderStatusRequest = new ResponderStatusRequest();
            responderStatusRequest.setAccidentId(bundle.getInt("accidentId"));
            responderStatusRequest.setUnitId(sessionManager.getUserId());
            responderStatusRequest.setStatus("cancelled");

            Call<Map<String, String>> call = apiService.updateResponderStatus(responderStatusRequest);

            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    Map<String, String> result = response.body();

                    int color = Color.parseColor("#757575"); // Grey color for cancelled
                    String statusText = "Đã hủy";

                    chipStatus.setText(statusText);
                    chipStatus.setChipBackgroundColor(ColorStateList.valueOf(color));

                    statusCode = "cancelled"; // Cập nhật statusCode
                    updateStatusUI(statusCode);

                    // Có thể thêm finish() ở đây nếu muốn đóng Activity sau khi hủy
                    // finish();
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    // Xử lý lỗi
                    Toast.makeText(AccidentDetailActivity.this, "Lỗi khi hủy thao tác", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void updateStatusUI(String status) {
        switch (status) {
            case "wait":
                btnPrimaryAction.setText("Xác nhận đang đến");
                btnNavigation.setVisibility(View.GONE);
                break;
            case "en_route":
                btnPrimaryAction.setText("Đã đến");
                btnNavigation.setVisibility(View.VISIBLE);
                break;
            case "arrived":
                btnPrimaryAction.setVisibility(View.GONE);
                btnNavigation.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                break;
            case "cancelled":
                btnPrimaryAction.setVisibility(View.GONE);
                btnNavigation.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                break;
        }
    }

    private void setupBottomNavigation() {
        // Set current selected item
        bottomNavigation.setSelectedItemId(R.id.nav_accidents);

        bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_accidents) {
                    startActivity(new Intent(AccidentDetailActivity.this, AccidentListActivity.class));
                    return true;
                } else if (id == R.id.nav_tra_nguoi_lai) {
                    startActivity(new Intent(AccidentDetailActivity.this, FindPersonActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                } else if (id == R.id.nav_thong_tin_user) {
                    startActivity(new Intent(AccidentDetailActivity.this, UserInfoActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                } else if (id == R.id.nav_tra_bien_so) {
                    startActivity(new Intent(AccidentDetailActivity.this, FindLicensePlateActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                }

                return false;
            }
        });

        Log.d(TAG, "Bottom navigation setup completed");
    }

    private void setupUserInfo() {
        userName.setText(sessionManager.getNamePerson());

        Bitmap userImage = sessionManager.loadImageFromPrefs();
        if (userImage != null) {
            userAvatar.setImageBitmap(userImage);
        }
    }
}