// ScanLicensePlateActivity.java
package com.example.doanmonhocltm;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
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
import com.example.doanmonhocltm.model.Person;
import com.example.doanmonhocltm.model.ScanLog;
import com.example.doanmonhocltm.model.Vehicles;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.*;

public class FindLicensePlateActivity extends AppCompatActivity {

    private MaterialButton btnChupAnh;
    private MaterialButton btnTraCuu;

    private TextInputEditText editTextLicensePlate;

    private BottomNavigationView bottomNavigation;

    private TextView userName;

    private CircleImageView userAvatar;


    private void showCustomVehicleTypeDialog(int DialogType) {
        // Tạo dialog với custom layout
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_vehicle_type);

        // Set dialog width to match parent
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Khai báo các view trong dialog
        TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
        LinearLayout carOption = dialog.findViewById(R.id.option_car);
        LinearLayout motorcycleOption = dialog.findViewById(R.id.option_motorcycle);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);

        // Set tiêu đề
        dialogTitle.setText("Chọn loại phương tiện");

        // Set sự kiện click cho các lựa chọn
        carOption.setOnClickListener(v -> {
            if (DialogType == 1) {
                performVehicleLookup("Xe ô tô");
            } else {
                LogicDialogScan(1);
            }
            dialog.dismiss();
        });

        motorcycleOption.setOnClickListener(v -> {
            if (DialogType == 1) {
                performVehicleLookup("Xe máy");
            } else {
                LogicDialogScan(2);
            }
            dialog.dismiss();
        });

        // Set sự kiện cho nút hủy
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Hiển thị dialog
        dialog.show();
    }

    private void LogicDialogScan(int type) {

        Intent intent = new Intent(FindLicensePlateActivity.this, ScanLicensePlateActivity.class);
        intent.putExtra("type", type);
        startActivity(intent);
    }


    private void performVehicleLookup(String vehicleType) {
        String vehicleInfo = editTextLicensePlate.getText().toString();


        if (vehicleInfo.trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập biển số xe", Toast.LENGTH_SHORT).show();
            return;
        }
        vehicleInfo = vehicleInfo.replace("-", "").replace(".", "").replace(" ", "");
        final int type = vehicleType.equals("Xe ô tô") ? 1 :
                vehicleType.equals("Xe máy") ? 2 : -1;
        if (type == -1) {
            Toast.makeText(this, "Vui lòng chọn loại phương tiện", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient(FindLicensePlateActivity.this).create(ApiService.class);
        Call<Vehicles> vehiclesCall = null;

        if (type == 1) {
            vehiclesCall = apiService.getCar(vehicleInfo);
        } else if (type == 2) {
            vehiclesCall = apiService.getMotorcycle(vehicleInfo);
        }

        vehiclesCall.enqueue(new Callback<Vehicles>() {
            @Override
            public void onResponse(Call<Vehicles> call, Response<Vehicles> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Vehicles vehicles = response.body();

                    Call<Person> resultFaceRecognitionCall = apiService.getPersonById(vehicles.getOwnerId());

                    resultFaceRecognitionCall.enqueue(new Callback<Person>() {
                        @Override
                        public void onResponse(Call<Person> call, Response<Person> response) {
                            if (response.isSuccessful()) {
                                Person person = response.body();
                                // Chuyển sang màn hình VehicleInfoActivity
                                Intent intent = new Intent(FindLicensePlateActivity.this, VehicleInfoActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putInt("type", type); // 1: xe hoi
                                bundle.putString("licensePlate", vehicles.getLicensePlate());
                                bundle.putString("brand", vehicles.getBrand());
                                bundle.putString("color", vehicles.getColor());
                                bundle.putString("owner", person.getFullName());
                                intent.putExtra("Infor", bundle);


//                                Call<ScanLog> scanLogCall = apiService.createCarScanLog(new ScanLog(vehicles.getLicensePlate(), vehicles.getOwnerId()));
                                Call<ScanLog> scanLogCall = (type == 1) ?
                                        apiService.createCarScanLog(new ScanLog(vehicles.getLicensePlate(), vehicles.getOwnerId())) : apiService.createMotorcycleScanLog(new ScanLog(vehicles.getLicensePlate(), vehicles.getOwnerId()));
                                scanLogCall.enqueue(new Callback<ScanLog>() {

                                    @Override
                                    public void onResponse(Call<ScanLog> call, Response<ScanLog> response) {
                                        if (response.isSuccessful()) {
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(FindLicensePlateActivity.this,
                                                    "Không tìm thấy thông tin xe. Mã lỗi: " + response.code(),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ScanLog> call, Throwable t) {
                                        Toast.makeText(FindLicensePlateActivity.this,
                                                "Lỗi kết nối: " + t.getMessage(),
                                                Toast.LENGTH_SHORT).show();

                                    }
                                });
                            } else {
                                Toast.makeText(FindLicensePlateActivity.this,
                                        "Không tìm thấy thông tin xe. Mã lỗi: " + response.code(),
                                        Toast.LENGTH_SHORT).show();

                            }
                        }

                        @Override
                        public void onFailure(Call<Person> call, Throwable t) {
                            Toast.makeText(FindLicensePlateActivity.this,
                                    "Lỗi kết nối: " + t.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Toast.makeText(FindLicensePlateActivity.this,
                            "Không tìm thấy thông tin xe. Mã lỗi: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    System.out.println("Response error code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Vehicles> call, Throwable t) {
                Toast.makeText(FindLicensePlateActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_find_license_plate);
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
        btnChupAnh = findViewById(R.id.btnChupAnh);
        btnTraCuu = findViewById(R.id.btnTraCuu);
        editTextLicensePlate = findViewById(R.id.editTextLicensePlate);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_tra_bien_so);

        SessionManager sessionManager = new SessionManager(FindLicensePlateActivity.this);
        userName = findViewById(R.id.userName);
        userName.setText(sessionManager.getNamePerson());

        //__________________________________________________________________________________________________________
        userAvatar = findViewById(R.id.userAvatar);
        Bitmap image = sessionManager.loadImageFromPrefs();
        userAvatar.setImageBitmap(image);
        //__________________________________________________________________________________________________________


    }

    private void setupEventListeners() {
        // Xử lý sự kiện khi nhấn nút "Chụp Ảnh"
        btnChupAnh.setOnClickListener(v -> {
            showCustomVehicleTypeDialog(2);
        });

        btnTraCuu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomVehicleTypeDialog(1);
            }
        });

        bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_tra_bien_so) {
                    // Đang ở đây rồi
                    return true;
                } else if (id == R.id.nav_tra_nguoi_lai) {
                    startActivity(new Intent(FindLicensePlateActivity.this, FindPersonActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                } else if (id == R.id.nav_thong_tin_user) {
                    startActivity(new Intent(FindLicensePlateActivity.this, UserInfoActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                } else if (id == R.id.nav_accidents)
                {
                    startActivity(new Intent(FindLicensePlateActivity.this, AccidentListActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                }

                return false;
            }
        });
    }
}