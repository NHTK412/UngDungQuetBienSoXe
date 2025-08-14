package com.example.doanmonhocltm;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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
import com.example.doanmonhocltm.model.Person;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindPersonActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private MaterialButton btnScanIdCard;

    private MaterialButton btnLookupCitizen;

    private TextInputEditText editTextCitizenId;

    private TextView userName;
    private CircleImageView userAvatar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_find_person);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo các thành phần giao diện
        initializeViews();

        // Thiết lập các sự kiện
        setupEventListeners();
    }

    private void initializeViews() {

        btnScanIdCard = findViewById(R.id.btnScanIdCard);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_tra_nguoi_lai);
        btnLookupCitizen = findViewById(R.id.btnLookupCitizen);
        editTextCitizenId = findViewById(R.id.editTextCitizenId);
        userAvatar = findViewById(R.id.userAvatar);

        //__________________________________________________________________________________________________________
        SessionManager sessionManager = new SessionManager(FindPersonActivity.this);
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

        btnScanIdCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle ticketData = new Bundle();
                ticketData.putInt("type", 3);
                ticketData.putString("licensePlate", "");

                Intent intent = new Intent(FindPersonActivity.this, ScanPersonActivity.class);
                intent.putExtra("ticketData", ticketData);
                startActivity(intent);
            }
        });

        bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_tra_nguoi_lai) {
                    // Đang ở đây rồi
                    return true;
                } else if (id == R.id.nav_tra_bien_so) {
                    startActivity(new Intent(FindPersonActivity.this, FindLicensePlateActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                } else if (id == R.id.nav_thong_tin_user) {
                    startActivity(new Intent(FindPersonActivity.this, UserInfoActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                } else if (id == R.id.nav_accidents)
                {
                    startActivity(new Intent(FindPersonActivity.this, AccidentListActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                }

                return false;
            }
        });



        btnLookupCitizen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editTextCitizenId.getText().toString().isEmpty()) {

                    ApiService apiService = ApiClient.getClient(FindPersonActivity.this).create(ApiService.class);

                    Call<Person> resultFaceRecognitionCall = apiService.getPersonById(editTextCitizenId.getText().toString());
                    resultFaceRecognitionCall.enqueue(new Callback<Person>() {

                        @Override
                        public void onResponse(Call<Person> call, Response<Person> response) {
                            if (response.isSuccessful()) {
                                Person result = response.body();

                                Bundle bundle = new Bundle();
                                bundle.putString("id", result.getId());
                                bundle.putString("fullName", result.getFullName());
                                bundle.putLong("birthDate", result.getBirthDate());
                                bundle.putString("gender", result.getGender());
                                bundle.putString("address", result.getAddress());
                                bundle.putString("phoneNumber", result.getPhoneNumber());
                                bundle.putString("facePath", result.getFacePath());


                                Intent intent = new Intent(FindPersonActivity.this, PersonInfoActivity.class);

                                intent.putExtra("result", bundle);

                                startActivity(intent);
                            } else {
                                Toast.makeText(FindPersonActivity.this, "Không Tìm Thấy Thông Tin Người Này", Toast.LENGTH_SHORT).show();

                            }

                        }

                        @Override
                        public void onFailure(Call<Person> call, Throwable t) {
                            Toast.makeText(FindPersonActivity.this,
                                    "Server Đang Bị Lỗi",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Toast.makeText(FindPersonActivity.this,
                            "Vui Lòng Nhập Số CCCD ",
                            Toast.LENGTH_SHORT).show();
                }


            }

        });
    }


}