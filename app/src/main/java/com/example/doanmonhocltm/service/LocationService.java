package com.example.doanmonhocltm.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.Manifest;
import android.os.Looper;
import android.util.Log;

import com.example.doanmonhocltm.LoginActivity;
import com.example.doanmonhocltm.R;
import com.example.doanmonhocltm.callapi.ApiClient;
import com.example.doanmonhocltm.callapi.ApiService;
import com.example.doanmonhocltm.callapi.SessionManager;
import com.example.doanmonhocltm.model.UserLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationService extends Service {

    private static final String TAG = "LocationService";
    private static final String CHANNEL_ID = "Location_Channel";
    private static final int NOTIFICATION_ID = 1;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "🚀 LocationService onCreate() được gọi");

        // Khởi tạo SessionManager và ApiService
        this.sessionManager = new SessionManager(this);
        this.apiService = ApiClient.getClient(this).create(ApiService.class);

        // Tạo notification channel cho Android 8+
        createNotificationChannel();

        // Kiểm tra quyền trước khi bắt đầu foreground service
        if (!checkLocationPermissions()) {
            Log.e(TAG, "❌ Không có quyền location, stopping service");
            stopSelf();
            return;
        }

        // Tạo và hiển thị notification
        Notification notification = createNotification();
        try {
            startForeground(NOTIFICATION_ID, notification);
            Log.d(TAG, "✅ Service started in foreground successfully");
        } catch (SecurityException e) {
            Log.e(TAG, "❌ SecurityException khi start foreground: " + e.getMessage());
            stopSelf();
            return;
        }

        // Khởi tạo location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Bắt đầu location updates
        startLocationUpdates();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Tracking",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Theo dõi vị trí người dùng");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d(TAG, "✅ Notification channel đã được tạo");
            }
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Đang theo dõi vị trí")
                .setContentText("Ứng dụng đang cập nhật vị trí của bạn")
                .setSmallIcon(R.mipmap.ic_logo)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private boolean checkLocationPermissions() {
        boolean fineLocation = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarseLocation = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        Log.d(TAG, "📍 Quyền FINE_LOCATION: " + (fineLocation ? "✅" : "❌"));
        Log.d(TAG, "📍 Quyền COARSE_LOCATION: " + (coarseLocation ? "✅" : "❌"));

        return fineLocation && coarseLocation;
    }

    private void startLocationUpdates() {
        Log.d(TAG, "🎯 Bắt đầu location updates");

        // Tạo LocationRequest
        LocationRequest locationRequest = new LocationRequest.Builder(10000) // 10 giây
                .setMinUpdateIntervalMillis(5000) // tối thiểu 5 giây
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build();

        // Tạo LocationCallback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                    for (Location location : locationResult.getLocations()) {
                        Log.d(TAG, "📍 Nhận được vị trí mới: " +
                                "Lat=" + location.getLatitude() +
                                ", Lng=" + location.getLongitude() +
                                ", Accuracy=" + location.getAccuracy() + "m");

                        sendLocationToServer(location.getLatitude(), location.getLongitude());
                    }
                } else {
                    Log.w(TAG, "⚠️ LocationResult null hoặc rỗng");
                }
            }
        };

        // Bắt đầu nhận location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ Location updates đã được đăng ký thành công");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Lỗi khi đăng ký location updates: " + e.getMessage());
                    });
        }
    }

    private void sendLocationToServer(double latitude, double longitude) {
        String userId = sessionManager.getUserId();

        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "❌ UserID null hoặc rỗng, không thể gửi vị trí");
            return;
        }

        Log.d(TAG, "🚀 Đang gửi vị trí lên server...");
        Log.d(TAG, "   - UserID: " + userId);
        Log.d(TAG, "   - Latitude: " + latitude);
        Log.d(TAG, "   - Longitude: " + longitude);

        UserLocationRequest locationRequest = new UserLocationRequest(userId, latitude, longitude);
        Call<Void> call = apiService.updateUserLocation(locationRequest);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Gửi vị trí thành công - Response code: " + response.code());
                } else {
                    Log.e(TAG, "❌ Gửi vị trí thất bại - Response code: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e(TAG, "   Error body: " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "   Không đọc được error body: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "❌ Lỗi network khi gửi vị trí: " + t.getMessage());
                if (t.getCause() != null) {
                    Log.e(TAG, "   Cause: " + t.getCause().getMessage());
                }
                t.printStackTrace();
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "🔄 onStartCommand được gọi - flags: " + flags + ", startId: " + startId);
        return START_STICKY; // Service sẽ restart nếu bị hệ thống kill
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "🛑 LocationService onDestroy() được gọi");

        // Dừng location updates khi service bị destroy
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "✅ Location updates đã được dừng"))
                    .addOnFailureListener(e -> Log.e(TAG, "❌ Lỗi khi dừng location updates: " + e.getMessage()));
        }
    }
}