package com.example.doanmonhocltm;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.doanmonhocltm.callapi.ApiClient;
import com.example.doanmonhocltm.callapi.ApiService;
import com.example.doanmonhocltm.callapi.SessionManager;
import com.example.doanmonhocltm.model.Logout;
import com.example.doanmonhocltm.model.Person;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserInfoActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigation;

    private TextView userName;

    // UI Components
    private CircleImageView userAvatar;
    private TextView userPosition;
    private TextView tvCitizenId, tvBirthDate, tvPhone, tvAddress;
    private TextView tvUsername, tvEmail;

    // Expandable sections
    private ImageButton btnExpandPersonal, btnExpandAccount;
    private LinearLayout layoutPersonalInfo, layoutAccountInfo;
    private TextView tvPersonalHeader, tvAccountHeader;

    private MaterialButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo các thành phần giao diện
        initializeViews();

        // Hàm truyền dữ liệu
        loadUserData();

        // Thiết lập các sự kiện
        setupEventListeners();
    }

    private void loadUserData() {
        SessionManager sessionManager = new SessionManager(UserInfoActivity.this);
        String userId = sessionManager.getUserId();

        ApiService apiService = ApiClient.getClient(UserInfoActivity.this).create(ApiService.class);
        Call<Person> call = apiService.getPersonById(userId);

        call.enqueue(new Callback<Person>() {

            @Override
            public void onResponse(Call<Person> call, Response<Person> response) {
                Person result = response.body();
                if (result != null) {
                    tvCitizenId.setText(result.getId());
                    tvPhone.setText(result.getPhoneNumber());
                    tvAddress.setText(result.getAddress());

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String formattedDate = sdf.format(new Date(result.getBirthDate()));

                    tvBirthDate.setText(formattedDate);

//                    tvBirthDate.setText(result.getBirthDate());

                    tvUsername.setText(sessionManager.getUsername());


                    tvEmail.setText(new SessionManager(UserInfoActivity.this).getUserMail());
                }

            }

            @Override
            public void onFailure(Call<Person> call, Throwable t) {
                t.printStackTrace();
            }
        });

    }

    private void initializeViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_thong_tin_user);

        // Profile components
        userAvatar = findViewById(R.id.userAvatar);
        userPosition = findViewById(R.id.userPosition);

        // Personal info section
        tvPersonalHeader = findViewById(R.id.tvPersonalHeader);
        btnExpandPersonal = findViewById(R.id.btnExpandPersonal);
        layoutPersonalInfo = findViewById(R.id.layoutPersonalInfo);
        tvCitizenId = findViewById(R.id.tvCitizenId);
        tvBirthDate = findViewById(R.id.tvBirthDate);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);
        tvEmail = findViewById(R.id.tvEmail);

        // Account info section
        tvAccountHeader = findViewById(R.id.tvAccountHeader);
        btnExpandAccount = findViewById(R.id.btnExpandAccount);
        layoutAccountInfo = findViewById(R.id.layoutAccountInfo);
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);

        // Button out
        btnLogout = findViewById(R.id.btnLogout);

        //__________________________________________________________________________________________________________
        SessionManager sessionManager = new SessionManager(UserInfoActivity.this);
        userName = findViewById(R.id.userName);
        userName.setText(sessionManager.getNamePerson());
        //__________________________________________________________________________________________________________
        //__________________________________________________________________________________________________________
        userAvatar = findViewById(R.id.userAvatar);
        Bitmap image = sessionManager.loadImageFromPrefs();
        userAvatar.setImageBitmap(image);
        //__________________________________________________________________________________________________________

    }

    private void setupEventListeners() {

        btnLogout.setOnClickListener(v -> {
            SessionManager sessionManager = new SessionManager(UserInfoActivity.this);
            ApiService apiService = ApiClient.getClient(UserInfoActivity.this).create(ApiService.class);
            Call<Logout> logoutCall = apiService.logout(sessionManager.getUserId());
            logoutCall.enqueue(new Callback<Logout>() {
                @Override
                public void onResponse(Call<Logout> call, Response<Logout> response) {
                    Logout result = response.body();
                    if (result.getStatus() == 1) {
                        sessionManager.clearSession();
                        Toast.makeText(UserInfoActivity.this, "Đăng Xuất Thành Công", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(UserInfoActivity.this, LoginActivity.class));
                    } else {
                        Toast.makeText(UserInfoActivity.this, "Lỗi Khi Đăng Xuẩt", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Logout> call, Throwable t) {
                    Toast.makeText(UserInfoActivity.this, "Server Đang Gặp Lỗi", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnExpandPersonal.setOnClickListener(v -> {
            boolean isVisible = layoutPersonalInfo.getVisibility() == View.VISIBLE;
            layoutPersonalInfo.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            btnExpandPersonal.setRotation(isVisible ? 0 : 180);
        });

        // Account info section expansion
        btnExpandAccount.setOnClickListener(v -> {
            boolean isVisible = layoutAccountInfo.getVisibility() == View.VISIBLE;
            layoutAccountInfo.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            btnExpandAccount.setRotation(isVisible ? 0 : 180);
        });


        bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_thong_tin_user) {
                    // Đang ở đây rồi
                    return true;
                } else if (id == R.id.nav_tra_bien_so) {
                    startActivity(new Intent(UserInfoActivity.this, FindLicensePlateActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                } else if (id == R.id.nav_tra_nguoi_lai) {
                    startActivity(new Intent(UserInfoActivity.this, FindPersonActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                } else if (id == R.id.nav_accidents) {
                    startActivity(new Intent(UserInfoActivity.this, AccidentListActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                }

                return false;
            }
        });

        bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_thong_tin_user) {
                    // Đang ở đây rồi
                    return true;

                } else if (id == R.id.nav_tra_nguoi_lai) {
                    startActivity(new Intent(UserInfoActivity.this, FindPersonActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;

                } else if (id == R.id.nav_tra_bien_so) {
                    startActivity(new Intent(UserInfoActivity.this, FindLicensePlateActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                }

                return false;
            }
        });
    }
}



