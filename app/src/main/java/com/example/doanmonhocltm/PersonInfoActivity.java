package com.example.doanmonhocltm;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doanmonhocltm.adapter.DriverLicenseAdapter;
import com.example.doanmonhocltm.callapi.ApiClient;
import com.example.doanmonhocltm.callapi.ApiService;
import com.example.doanmonhocltm.callapi.SessionManager;
import com.example.doanmonhocltm.model.DriverLicense;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PersonInfoActivity extends AppCompatActivity {

    private static final String TAG = "PersonInfoActivity";
    private static final String DATE_FORMAT_INPUT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String DATE_FORMAT_OUTPUT = "dd/MM/yyyy";
    private static final String PERMANENT_DATE = "31/12/9999";
    private static final String PERMANENT_TEXT = "Vĩnh Viễn";

    // UI Components
    private BottomNavigationView bottomNavigation;
    private TextView tvPersonId, tvFullName, tvBirthDate, tvGender, tvAddress, tvPhoneNumber;
    private ImageView personImage;
    private CircleImageView userAvatar;
    private TextView userName;
    private RecyclerView recyclerViewDriverLicenses;
    private DriverLicenseAdapter driverLicenseAdapter;
    private TextView tvEmptyLicenses;

    // Data
    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupEdgeToEdge();
        setContentView(R.layout.activity_person_info);
        setupWindowInsets();

        initializeComponents();
        displayPersonInfo();
        setupEventListeners();
    }

    private void setupEdgeToEdge() {
        androidx.activity.EdgeToEdge.enable(this);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeComponents() {
        initializeViews();
        initializeServices();
        setupRecyclerView();
        setupUserInfo();
    }

    private void initializeViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_tra_nguoi_lai);

        tvPersonId = findViewById(R.id.tvPersonId);
        tvFullName = findViewById(R.id.tvFullName);
        tvBirthDate = findViewById(R.id.tvBirthDate);
        tvGender = findViewById(R.id.tvGender);
        tvAddress = findViewById(R.id.tvAddress);
        tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        personImage = findViewById(R.id.personImage);

        userName = findViewById(R.id.userName);
        userAvatar = findViewById(R.id.userAvatar);

        recyclerViewDriverLicenses = findViewById(R.id.recyclerViewDriverLicenses);
        tvEmptyLicenses = findViewById(R.id.tvEmptyLicenses);
        tvEmptyLicenses.setVisibility(View.GONE);
    }

    private void initializeServices() {
        sessionManager = new SessionManager(this);
        apiService = ApiClient.getClient(this).create(ApiService.class);
    }

    private void setupRecyclerView() {
        recyclerViewDriverLicenses.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupUserInfo() {
        userName.setText(sessionManager.getNamePerson());

        Bitmap userImage = sessionManager.loadImageFromPrefs();
        if (userImage != null) {
            userAvatar.setImageBitmap(userImage);
        }
    }

    private void displayPersonInfo() {
        Bundle personData = getPersonDataFromIntent();
        if (personData != null) {
            setPersonDetails(personData);
            loadPersonImage(personData.getString("facePath"));
            loadDriverLicenses(personData.getString("id"));
        }
    }

    private Bundle getPersonDataFromIntent() {
        Intent intent = getIntent();
        return intent.getBundleExtra("result");
    }

    private void setPersonDetails(Bundle bundle) {
        String personId = bundle.getString("id");
        String fullName = bundle.getString("fullName");
        Long birthDate = bundle.getLong("birthDate");
        String gender = bundle.getString("gender");
        String address = bundle.getString("address");
        String phoneNumber = bundle.getString("phoneNumber");

        tvPersonId.setText(personId);
        tvFullName.setText(fullName);
        tvBirthDate.setText(formatBirthDate(birthDate));
        tvGender.setText(formatGender(gender));
        tvAddress.setText(address);
        tvPhoneNumber.setText(phoneNumber);
    }

    private String formatBirthDate(Long birthDate) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_OUTPUT, Locale.getDefault());
        return sdf.format(new Date(birthDate));
    }

    private String formatGender(String gender) {
        switch (gender) {
            case "MALE":
                return "Nam";
            case "FEMALE":
                return "Nữ";
            default:
                return "";
        }
    }

    private void loadPersonImage(String facePath) {
        if (facePath == null || facePath.isEmpty()) {
            return;
        }

        Call<ResponseBody> imageCall = apiService.getImage(facePath);
        imageCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                handleImageResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load person image: " + t.getMessage());
            }
        });
    }

    private void handleImageResponse(Response<ResponseBody> response) {
        if (response.isSuccessful() && response.body() != null) {
            try {
                byte[] imageBytes = response.body().bytes();
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                personImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                Log.e(TAG, "Error processing image: " + e.getMessage());
            }
        } else {
            Log.e(TAG, "Image not found or error: " + response.code());
        }
    }

    private void loadDriverLicenses(String personId) {
        if (personId == null || personId.isEmpty()) {
            showEmptyLicensesMessage("ID người không hợp lệ.");
            return;
        }

        Call<List<DriverLicense>> driverLicenseCall = apiService.getDriverLicenseByPersonId(personId);
        driverLicenseCall.enqueue(new Callback<List<DriverLicense>>() {
            @Override
            public void onResponse(@NonNull Call<List<DriverLicense>> call, @NonNull Response<List<DriverLicense>> response) {
                handleDriverLicenseResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<List<DriverLicense>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load driver licenses: " + t.getMessage());
                showEmptyLicensesMessage("Có lỗi xảy ra khi truy xuất dữ liệu.");
            }
        });
    }

    private void handleDriverLicenseResponse(Response<List<DriverLicense>> response) {
        if (response.isSuccessful() && response.body() != null) {
            List<DriverLicense> licenses = response.body();
            formatDriverLicenseDates(licenses);
            displayDriverLicenses(licenses);
        } else if (response.code() == 404) {
            showEmptyLicensesMessage("Người này không có bằng lái xe.");
        } else {
            showEmptyLicensesMessage("Có lỗi xảy ra khi truy xuất dữ liệu.");
        }
    }

    private void formatDriverLicenseDates(List<DriverLicense> licenses) {
        for (DriverLicense license : licenses) {
            license.setIssueDate(convertDate(license.getIssueDate()));
            license.setExpiryDate(convertDate(license.getExpiryDate()));
        }
    }

    private void displayDriverLicenses(List<DriverLicense> licenses) {
        driverLicenseAdapter = new DriverLicenseAdapter(licenses);
        recyclerViewDriverLicenses.setAdapter(driverLicenseAdapter);
        tvEmptyLicenses.setVisibility(View.GONE);
    }

    private void showEmptyLicensesMessage(String message) {
        tvEmptyLicenses.setVisibility(View.VISIBLE);
        tvEmptyLicenses.setText(message);
        recyclerViewDriverLicenses.setAdapter(null);
    }

    private String convertDate(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        // Add seconds if missing for valid ISO 8601 format
        String normalizedInput = normalizeInputDate(input);

        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat(DATE_FORMAT_INPUT, Locale.getDefault());
            Date date = isoFormat.parse(normalizedInput);

            SimpleDateFormat outputFormat = new SimpleDateFormat(DATE_FORMAT_OUTPUT, Locale.getDefault());
            String formattedDate = outputFormat.format(date);

            return PERMANENT_DATE.equals(formattedDate) ? PERMANENT_TEXT : formattedDate;
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date: " + input, e);
            return input; // Return original if parsing fails
        }
    }

    private String normalizeInputDate(String input) {
        if (input.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:?")) {
            return input + "00"; // Add seconds if missing
        }
        return input;
    }

    private void setupEventListeners() {
        bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_tra_nguoi_lai) {
                    // Đang ở đây rồi
                    return true;
                } else if (id == R.id.nav_tra_bien_so) {
                    startActivity(new Intent(PersonInfoActivity.this, FindLicensePlateActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                } else if (id == R.id.nav_thong_tin_user) {
                    startActivity(new Intent(PersonInfoActivity.this, UserInfoActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                } else if (id == R.id.nav_accidents)
                {
                    startActivity(new Intent(PersonInfoActivity.this, AccidentListActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                }

                return false;
            }
        });
    }




}