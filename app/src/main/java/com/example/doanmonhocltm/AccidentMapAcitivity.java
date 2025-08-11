//package com.example.doanmonhocltm;
//
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.doanmonhocltm.R;
//
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MarkerOptions;
//
// TODO: THÊM CÁC IMPORT RETROFIT NÀY
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//import java.util.Locale;
//
//public class AccidentMapAcitivity extends AppCompatActivity implements OnMapReadyCallback {
//
//    private double userLatitude;
//    private double userLongitude;
//    private double accidentLatitude;
//    private double accidentLongitude;
//    private GoogleMap mMap;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_accident_map);
//
//        // tạo map
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.accidentUserMapView);
//        if (mapFragment != null) {
//            mapFragment.getMapAsync(this);
//        }
//
//        // get data từ Intent
//        Intent intent = getIntent();
//        if (intent != null) {
//            userLatitude = intent.getDoubleExtra("userLatitude", 0.0);
//            userLongitude = intent.getDoubleExtra("userLongitude", 0.0);
//            accidentLatitude = intent.getDoubleExtra("accidentLatitude", 0.0);
//            accidentLongitude = intent.getDoubleExtra("accidentLongitude", 0.0);
//            String distance = intent.getStringExtra("distance");
//            String currentTime = intent.getStringExtra("currentTime");
//
//            TextView tvDistance = findViewById(R.id.tvDistance);
//            TextView tvCoordinates = findViewById(R.id.tvCoordinates);
//            TextView tvReceivedTime = findViewById(R.id.tvReceivedTime);
//
//            tvDistance.setText("Khoảng cách ước tính: " + distance);
//            tvReceivedTime.setText(currentTime);
//            tvCoordinates.setText(String.format(Locale.getDefault(), "%.4f° N, %.4f° E", accidentLatitude, accidentLongitude));
//        }
//
//        Button btnDecline = findViewById(R.id.btnDecline);
//        Button btnAccept = findViewById(R.id.btnAccept);
//
//
//        btnDecline.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(AccidentMapAcitivity.this, AccidentListActivity.class);
//                startActivity(intent);
//                finish();
//            }
//        });
//
//        btnAccept.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//
//                // vi du id: 0000001
//                String accidentId = "0000001";
//
//                // update trạng thái là en_route đang đi
//                StatusUpdateRequest statusUpdate = new StatusUpdateRequest("en_route");
//
//                // tạo service + call APi
//                RetrofitApi apiService = RetrofitClient.getRetrofitInstance().create(RetrofitApi.class);
//                Call<ApiResponse> call = apiService.updateAccidentStatus(accidentId, statusUpdate);
//
//                // gửi API async
//                call.enqueue(new Callback<ApiResponse>() {
//                    @Override
//                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
//                        if (response.isSuccessful()) {
//                            Toast.makeText(AccidentMapAcitivity.this, "Cập nhật trạng thái thành công!", Toast.LENGTH_SHORT).show();
//
//
//                        } else {
//                            Toast.makeText(AccidentMapAcitivity.this, "Lỗi khi cập nhật trạng thái: " + response.code(), Toast.LENGTH_SHORT).show();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<ApiResponse> call, Throwable t) {
//                        Toast.makeText(AccidentMapAcitivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//
//
//                if (mMap != null) {
//                    // zoom tới vị trí tai nạn
//                    LatLng accidentLocation = new LatLng(accidentLatitude, accidentLongitude);
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(accidentLocation, 15));
//
//                    // mở map
//                    String uri = "https://www.google.com/maps/dir/?api=1" +
//                            "&origin=" + userLatitude + "," + userLongitude +
//                            "&destination=" + accidentLatitude + "," + accidentLongitude +
//                            "&travelmode=driving";
//                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
//                    mapIntent.setPackage("com.google.android.apps.maps");
//
//                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
//                        startActivity(mapIntent);
//                    } else {
//                        Toast.makeText(AccidentMapAcitivity.this, "Ứng dụng Google Maps không được cài đặt.", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//        });
//    }
//
//    @Override
//    public void onMapReady(@NonNull GoogleMap googleMap) {
//        mMap = googleMap;
//
//        // điểm đánh dấu vị trí tai nạn
//        LatLng accidentLocation = new LatLng(accidentLatitude, accidentLongitude);
//        mMap.addMarker(new MarkerOptions()
//                .position(accidentLocation)
//                .title("Vị trí tai nạn"));
//
//        // zôom tới cái vị trí mà tai nạn
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(accidentLocation, 15));
//    }
//}


package com.example.doanmonhocltm;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

public class AccidentMapAcitivity extends AppCompatActivity implements OnMapReadyCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}
