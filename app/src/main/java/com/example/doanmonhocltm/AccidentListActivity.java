package com.example.doanmonhocltm;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.doanmonhocltm.callapi.ApiClient;
import com.example.doanmonhocltm.callapi.ApiService;
import com.example.doanmonhocltm.callapi.SessionManager;
import com.example.doanmonhocltm.model.Accident;
import com.example.doanmonhocltm.adapter.AccidentListAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccidentListActivity extends AppCompatActivity implements AccidentListAdapter.OnItemClickListener {

    private static final String TAG = "AccidentListActivity";

    // Views - giữ nguyên như layout gốc
    private RecyclerView rvAccidentList;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout emptyStateContainer;
    private ProgressBar progressLoading;
    private ChipGroup statusChipGroup;
    private Chip chipAll,chipWait ,chipEnRoute, chipArrived;
    private BottomNavigationView bottomNavigation;


    private TextView userName;
    private CircleImageView userAvatar;
    // API và Session
    private ApiService apiService;
    private SessionManager sessionManager;

    // Data
    private AccidentListAdapter adapter;
    private List<Accident> accidents;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accident_list);

        initViews();
        setupRecyclerView();
        setupChipFilters();
        setupSwipeRefresh();
        setupBottomNavigation();

        // Load data from API
        loadDataFromApi();
    }

    private void initViews() {
        rvAccidentList = findViewById(R.id.rvAccidentList);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        emptyStateContainer = findViewById(R.id.emptyStateContainer);
        progressLoading = findViewById(R.id.progressLoading);
        statusChipGroup = findViewById(R.id.statusChipGroup);
        chipAll = findViewById(R.id.chipAll);
        chipEnRoute = findViewById(R.id.chipEnRoute);
        chipArrived = findViewById(R.id.chipArrived);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Initialize API service và session manager
        apiService = ApiClient.getClient(AccidentListActivity.this).create(ApiService.class);
        sessionManager = new SessionManager(AccidentListActivity.this);

        userName = findViewById(R.id.userName);
        userAvatar = findViewById(R.id.userAvatar);
        setupUserInfo();

        handler = new Handler();

        Log.d(TAG, "Views initialized successfully");
    }

    private void setupRecyclerView() {
        accidents = new ArrayList<>();
        adapter = new AccidentListAdapter(this, accidents);
        adapter.setOnItemClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvAccidentList.setLayoutManager(layoutManager);
        rvAccidentList.setAdapter(adapter);

        Log.d(TAG, "RecyclerView setup completed");
    }

    private void setupChipFilters() {
        statusChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                // If no chip is selected, select "All" by default
                chipAll.setChecked(true);
                return;
            }

            int checkedId = checkedIds.get(0);
            String filterStatus = "all";
            if (checkedId == R.id.chipWait)
            {
                filterStatus = "wait";
            }
            else if (checkedId == R.id.chipEnRoute) {
                filterStatus = "en_route";
            } else if (checkedId == R.id.chipArrived) {
                filterStatus = "arrived";
            }

            Log.d(TAG, "Filter selected: " + filterStatus);
            adapter.filterByStatus(filterStatus);
            updateEmptyState();
        });

        // Set default selection
        chipAll.setChecked(true);
        Log.d(TAG, "Chip filters setup completed");
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright)
        );

        swipeRefresh.setOnRefreshListener(this::refreshData);
        Log.d(TAG, "SwipeRefresh setup completed");
    }

    private void setupBottomNavigation() {
        // Set current selected item
        bottomNavigation.setSelectedItemId(R.id.nav_accidents);

        bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_accidents) {
                    // Đang ở đây rồi

                    return true;
                } else if (id == R.id.nav_tra_nguoi_lai) {
                    startActivity(new Intent(AccidentListActivity.this, FindPersonActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                } else if (id == R.id.nav_thong_tin_user) {
                    startActivity(new Intent(AccidentListActivity.this, UserInfoActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                } else if (id == R.id.nav_tra_bien_so) {
                    startActivity(new Intent(AccidentListActivity.this, FindLicensePlateActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                }

                return false;
            }
        });

        Log.d(TAG, "Bottom navigation setup completed");
    }

    // Load data from API - cải thiện logic xử lý
    private void loadDataFromApi() {
        Log.d(TAG, "Starting to load data from API");
        showLoading(true);

        // Kiểm tra user ID từ session manager
        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            Log.w(TAG, "User ID not found in session, using hardcoded ID");
            userId = "058205002155";
        }

        Log.d(TAG, "Loading data for userId: " + userId);

        Call<List<Accident>> call = apiService.getAccidentByUnitId(userId);
        call.enqueue(new Callback<List<Accident>>() {
            @Override
            public void onResponse(Call<List<Accident>> call, Response<List<Accident>> response) {
                Log.d(TAG, "API Response received. Code: " + response.code());
                showLoading(false);

                if (swipeRefresh.isRefreshing()) {
                    swipeRefresh.setRefreshing(false);
                }

                if (response.isSuccessful() && response.body() != null) {
                    List<Accident> apiAccidents = response.body();
                    Log.d(TAG, "API returned " + apiAccidents.size() + " accidents");

                    // Log sample data để debug
                    if (!apiAccidents.isEmpty()) {
                        Accident sample = apiAccidents.get(0);
                        Log.d(TAG, "Sample accident: " + sample.toString());
                    }

                    if (!apiAccidents.isEmpty()) {
                        // Có data từ API
                        accidents.clear();
                        accidents.addAll(apiAccidents);
                        adapter.updateData(accidents);
                        updateEmptyState();

                        Toast.makeText(AccidentListActivity.this,
                                "Đã tải " + accidents.size() + " tai nạn",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // API trả về empty list
                        accidents.clear();
                        adapter.updateData(accidents);
                        updateEmptyState();

                        Toast.makeText(AccidentListActivity.this,
                                "Không có tai nạn nào",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // API response không thành công
                    Log.e(TAG, "API Error: " + response.code() + " - " + response.message());
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }

                    Toast.makeText(AccidentListActivity.this,
                            "Lỗi server: " + response.code() + " - " + response.message(),
                            Toast.LENGTH_LONG).show();
                    updateEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<Accident>> call, Throwable t) {
                Log.e(TAG, "API Failure: " + t.getMessage(), t);
                showLoading(false);

                if (swipeRefresh.isRefreshing()) {
                    swipeRefresh.setRefreshing(false);
                }

                Toast.makeText(AccidentListActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();

                updateEmptyState();
            }
        });
    }

    private void refreshData() {
        Log.d(TAG, "Refreshing data...");
        if (!swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(true);
        }
        loadDataFromApi();
    }

    private void showLoading(boolean show) {
        Log.d(TAG, "showLoading: " + show);
        if (show) {
            progressLoading.setVisibility(View.VISIBLE);
            rvAccidentList.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.GONE);
        } else {
            progressLoading.setVisibility(View.GONE);
            rvAccidentList.setVisibility(View.VISIBLE);
        }
    }

    private void updateEmptyState() {
        boolean isEmpty = adapter.getItemCount() == 0;
        Log.d(TAG, "updateEmptyState: isEmpty = " + isEmpty + ", adapter count = " + adapter.getItemCount());

        if (isEmpty) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            rvAccidentList.setVisibility(View.GONE);
        } else {
            emptyStateContainer.setVisibility(View.GONE);
            rvAccidentList.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(Accident accident) {
        Log.d(TAG, "Item clicked: " + accident.getAccident_id());
        Toast.makeText(this,
                "Clicked: Tai nạn #" + accident.getAccident_id() + " tại " + accident.getRoad_name(),
                Toast.LENGTH_SHORT).show();

        // TODO: Navigate to accident detail activity
        // Intent intent = new Intent(this, AccidentDetailActivity.class);
        // intent.putExtra("accident_id", accident.getAccident_id());
        // startActivity(intent);
    }

    @Override
    public void onViewDetailsClick(Accident accident) {
        Log.d(TAG, "View details clicked: " + accident.getAccident_id());
        Toast.makeText(this,
                "Xem chi tiết tai nạn #" + accident.getAccident_id(),
                Toast.LENGTH_SHORT).show();
        Call<ResponseBody> callImageAccident = apiService.getImageAccident(accident.getImage_url());
        callImageAccident.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(AccidentListActivity.this, "Xem chi tiết tai nạn #" + accident.getAccident_id(),
                                Toast.LENGTH_SHORT).show();

                        byte[] imageBytes = response.body().bytes();
                        Log.e(TAG, "Image bytes length: " + imageBytes.length);

                        Bundle bundle = new Bundle();
                        bundle.putString("roadName", accident.getRoad_name());
                        bundle.putString("timestamp", accident.getFormattedDate() + " " + accident.getFormattedTime());
                        bundle.putInt("accidentId", accident.getAccident_id());
                        bundle.putString("status", accident.getStatusText());
                        bundle.putString("accidentType", accident.getAccidentTypeText());
                        bundle.putInt("color", accident.getStatusColor());
                        bundle.putByteArray("imageAccident", imageBytes);

                        bundle.putString("statusCode", accident.getStatus());

                        Intent intent = new Intent(AccidentListActivity.this, AccidentDetailActivity.class);
                        intent.putExtra("data", bundle);

                        // Nếu context không phải Activity, thêm flag này
//                                if (!(context instanceof Activity)) {
//                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                }

                        startActivity(intent);
                        Log.e(TAG, "Started AccidentDetailActivity");

                    } else {
                        Log.e(TAG, "Response not successful or body is null");
                        Toast.makeText(AccidentListActivity.this, "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error processing image: " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(AccidentListActivity.this, "Lỗi xử lý hình ảnh", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

                Log.e("API", "Request failed: " + t.getMessage());
            }
        });
        // TODO: Navigate to accident detail activity
        // Intent intent = new Intent(this, AccidentDetailActivity.class);
        // intent.putExtra("accident_id", accident.getAccident_id());
        // startActivity(intent);
    }

    private void setupUserInfo() {
        userName.setText(sessionManager.getNamePerson());

        Bitmap userImage = sessionManager.loadImageFromPrefs();
        if (userImage != null) {
            userAvatar.setImageBitmap(userImage);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        Log.d(TAG, "Activity destroyed");
    }
}