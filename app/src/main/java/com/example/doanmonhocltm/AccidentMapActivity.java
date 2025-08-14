package com.example.doanmonhocltm;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

/**
 * Activity hiển thị bản đồ vị trí tai nạn
 * Cho phép xem thông tin chi tiết và mở Google Maps để chỉ đường
 */
public class AccidentMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Biến lưu trữ thông tin vị trí
    private double userLatitude;        // Vĩ độ của người dùng
    private double userLongitude;       // Kinh độ của người dùng
    private double accidentLatitude;    // Vĩ độ vị trí tai nạn
    private double accidentLongitude;   // Kinh độ vị trí tai nạn

    // Đối tượng GoogleMap để hiển thị bản đồ
    private GoogleMap mMap;

    /**
     * Khởi tạo Activity và thiết lập giao diện
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accident_map);

        // Khởi tạo fragment bản đồ Google Maps
        initializeMap();

        // Nhận và xử lý dữ liệu từ Intent
        receiveIntentData();

        // Thiết lập sự kiện cho các nút
        setupButtonListeners();
    }

    /**
     * Khởi tạo fragment Google Maps
     */
    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.accidentUserMapView);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Nhận dữ liệu từ Intent và cập nhật UI
     */
    private void receiveIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            // Lấy thông tin vị trí từ Intent
            userLatitude = intent.getDoubleExtra("userLatitude", 0.0);
            userLongitude = intent.getDoubleExtra("userLongitude", 0.0);
            accidentLatitude = intent.getDoubleExtra("accidentLatitude", 0.0);
            accidentLongitude = intent.getDoubleExtra("accidentLongitude", 0.0);

            // Lấy thông tin hiển thị
            String distance = intent.getStringExtra("distance");
            String currentTime = intent.getStringExtra("currentTime");

            // Cập nhật các TextView với thông tin nhận được
            updateUIWithData(distance, currentTime);
        }
    }

    /**
     * Cập nhật giao diện với dữ liệu nhận được
     * @param distance Khoảng cách ước tính
     * @param currentTime Thời gian nhận tín hiệu
     */
    private void updateUIWithData(String distance, String currentTime) {
        TextView tvDistance = findViewById(R.id.tvDistance);
        TextView tvCoordinates = findViewById(R.id.tvCoordinates);
        TextView tvReceivedTime = findViewById(R.id.tvReceivedTime);

        // Hiển thị khoảng cách (nếu có thông tin, nếu không hiển thị "< 1 phút")
        tvDistance.setText(distance != null ? distance : "< 1 phút");

        // Hiển thị thời gian nhận tín hiệu
        tvReceivedTime.setText(currentTime != null ? currentTime : "Đang cập nhật...");

        // Hiển thị tọa độ tai nạn với định dạng độ, phút
        tvCoordinates.setText(String.format(Locale.getDefault(), "%.4f° N, %.4f° E",
                accidentLatitude, accidentLongitude));
    }

    /**
     * Thiết lập sự kiện click cho các nút
     */
    private void setupButtonListeners() {
        Button btnDecline = findViewById(R.id.btnDecline);
        Button btnAccept = findViewById(R.id.btnAccept);

        // Nút "Trở Lại" - quay về activity trước đó
        btnDecline.setOnClickListener(v -> {
            handleBackAction();
        });

        // Nút "Chấp Nhận & Chỉ Đường" - mở Google Maps chỉ đường
        btnAccept.setOnClickListener(v -> {
            handleNavigationAction();
        });
    }

    /**
     * Xử lý khi người dùng bấm nút "Trở Lại"
     * Đơn giản chỉ quay về activity trước đó
     */
    private void handleBackAction() {
        // Quay về activity trước đó
        finish();
    }

    /**
     * Xử lý khi người dùng bấm nút "Chấp Nhận & Chỉ Đường"
     * Mở Google Maps với chỉ đường đến vị trí tai nạn
     */
    private void handleNavigationAction() {
        if (accidentLatitude == 0.0 && accidentLongitude == 0.0) {
            Toast.makeText(this, "Không có thông tin vị trí tai nạn", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mở Google Maps chỉ đường
        openGoogleMaps();
    }

    /**
     * Mở ứng dụng Google Maps với chỉ đường từ vị trí hiện tại đến vị trí tai nạn
     */
    private void openGoogleMaps() {
        // Tạo URI cho Google Maps với chỉ đường lái xe
        String uri = "https://www.google.com/maps/dir/?api=1" +
                "&origin=" + userLatitude + "," + userLongitude +
                "&destination=" + accidentLatitude + "," + accidentLongitude +
                "&travelmode=driving";

        // Tạo Intent để mở Google Maps
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        mapIntent.setPackage("com.google.android.apps.maps");

        // Kiểm tra xem Google Maps có được cài đặt không
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Nếu không có Google Maps, thử mở trình duyệt web
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            if (webIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(webIntent);
                Toast.makeText(this, "Mở chỉ đường trên trình duyệt web", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Không thể mở ứng dụng chỉ đường", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Callback khi Google Maps sẵn sàng sử dụng
     * Hiển thị markers và zoom đến vị trí tai nạn
     * @param googleMap Đối tượng GoogleMap
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Đặt marker tại vị trí tai nạn với icon đỏ
        LatLng accidentLocation = new LatLng(accidentLatitude, accidentLongitude);
        mMap.addMarker(new MarkerOptions()
                .position(accidentLocation)
                .title("Vị trí tai nạn")
                .snippet("Cần hỗ trợ khẩn cấp"));

        // Đặt marker tại vị trí người dùng (nếu có thông tin hợp lệ)
        if (userLatitude != 0.0 && userLongitude != 0.0) {
            LatLng userLocation = new LatLng(userLatitude, userLongitude);
            mMap.addMarker(new MarkerOptions()
                    .position(userLocation)
                    .title("Vị trí của bạn")
                    .snippet("Điểm xuất phát"));
        }

        // Zoom camera đến vị trí tai nạn với mức zoom phù hợp
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(accidentLocation, 15));

        // Cho phép zoom và di chuyển bản đồ
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
    }
}