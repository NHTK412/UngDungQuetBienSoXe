package com.example.doanmonhocltm;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccidentListActivity extends AppCompatActivity implements AccidentListAdapter.OnItemClickListener {

    // Views
    private RecyclerView rvAccidentList;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout emptyStateContainer;
    private ProgressBar progressLoading;
    private ImageButton btnRefresh;
    private ChipGroup statusChipGroup;
    private Chip chipAll, chipEnRoute, chipArrived;
    private BottomNavigationView bottomNavigation;

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
        btnRefresh = findViewById(R.id.btnRefresh);
        statusChipGroup = findViewById(R.id.statusChipGroup);
        chipAll = findViewById(R.id.chipAll);
        chipEnRoute = findViewById(R.id.chipEnRoute);
        chipArrived = findViewById(R.id.chipArrived);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Initialize API service và session manager
        apiService = ApiClient.getClient(AccidentListActivity.this).create(ApiService.class);
        sessionManager = new SessionManager(AccidentListActivity.this);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        handler = new Handler();

        // Setup refresh button click
        btnRefresh.setOnClickListener(v -> refreshData());
    }

    private void setupRecyclerView() {
        accidents = new ArrayList<>();
        adapter = new AccidentListAdapter(this, accidents);
        adapter.setOnItemClickListener(this);

        rvAccidentList.setLayoutManager(new LinearLayoutManager(this));
        rvAccidentList.setAdapter(adapter);
    }

    private void setupChipFilters() {
        statusChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int checkedId = checkedIds.get(0);
            String filterStatus = "all";

            if (checkedId == R.id.chipEnRoute) {
                filterStatus = "en_route";
            } else if (checkedId == R.id.chipArrived) {
                filterStatus = "arrived";
            }

            adapter.filterByStatus(filterStatus);
            updateEmptyState();
        });

        // Set default selection
        chipAll.setChecked(true);
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeColors(
                getResources().getColor(R.color.colorPrimary, getTheme())
        );

        swipeRefresh.setOnRefreshListener(this::refreshData);
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
    }

    // Load data from API
    private void loadDataFromApi() {
        showLoading(true);

        // Kiểm tra user ID từ session manager
        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            showLoading(false);
            if (swipeRefresh.isRefreshing()) {
                swipeRefresh.setRefreshing(false);
            }
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin người dùng", Toast.LENGTH_LONG).show();
            updateEmptyState();
            return;
        }
        userId = "058205002155";
        Call<List<Accident>> call = apiService.getAccidentByUnitId(userId);
        call.enqueue(new Callback<List<Accident>>() {
            @Override
            public void onResponse(Call<List<Accident>> call, Response<List<Accident>> response) {
                showLoading(false);

                if (swipeRefresh.isRefreshing()) {
                    swipeRefresh.setRefreshing(false);
                }

                if (response.isSuccessful()) {
                    if (response.body() != null && !response.body().isEmpty()) {
                        // Có data từ API
                        accidents.clear();
                        accidents.addAll(response.body());
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
                    Toast.makeText(AccidentListActivity.this,
                            "Lỗi server: " + response.code() + " - " + response.message(),
                            Toast.LENGTH_LONG).show();
                    updateEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<Accident>> call, Throwable t) {
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
        if (!swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(true);
        }

        loadDataFromApi();
    }

    private void showLoading(boolean show) {
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
        if (adapter.getItemCount() == 0) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            rvAccidentList.setVisibility(View.GONE);
        } else {
            emptyStateContainer.setVisibility(View.GONE);
            rvAccidentList.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(Accident accident) {
        Toast.makeText(this,
                "Clicked: Tai nạn #" + accident.getAccident_id() + " tại " + accident.getRoad_name(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewDetailsClick(Accident accident) {
        Toast.makeText(this,
                "Xem chi tiết tai nạn #" + accident.getAccident_id(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}