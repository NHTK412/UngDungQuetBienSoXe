package com.example.doanmonhocltm;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FaceScanInterface extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private ImageView imgViewScan;
    private Button btnScan, btnHistory, btnHelp;
//    private PreviewView previewView;

    private ImageCapture imageCapture;
    private ProcessCameraProvider cameraProvider;
    private DatabaseReference mDatabase;
    private ExecutorService cameraExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_scan_interface);

        // Initialize views
        imgViewScan = findViewById(R.id.imgViewScan);
        btnScan = findViewById(R.id.btnScan);
        btnHistory = findViewById(R.id.btnHistory);
        btnHelp = findViewById(R.id.btnHelp);
//        imgViewScan = findViewById(R.id.imgViewScan);

        // Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize camera executor
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Check and request permissions
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        // Set up button click listeners
        btnScan.setOnClickListener(v -> takePictureAndCompare());

        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, HistoryScanFaceScan.class);
            startActivity(intent);
        });

        btnHelp.setOnClickListener(v -> {
            Intent intent = new Intent(this, HelpFaceScan.class);
            startActivity(intent);
        });
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Cần cấp quyền để sử dụng camera", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                // Set up the preview use case
                Preview preview = new Preview.Builder().build();

                // Use front camera
                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                // Set up the capture use case
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

//                // Connect the preview to the PreviewView
//                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Unbind all use cases before rebinding
                cameraProvider.unbindAll();

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Không thể khởi tạo camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePictureAndCompare() {
        if (imageCapture == null) {
            Toast.makeText(this, "Camera chưa sẵn sàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create output file
        File outputDir = getExternalFilesDir(null);
        File photoFile = new File(outputDir, "face_scan_" + System.currentTimeMillis() + ".jpg");

        // Create output options object
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        // Take the picture
        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        // Process the saved image
                        try {
                            Uri photoUri = Uri.fromFile(photoFile);
                            FirebaseVisionImage image = FirebaseVisionImage.fromFilePath(FaceScanInterface.this, photoUri);
                            detectFaceAndCompare(image);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(FaceScanInterface.this, "Lỗi khi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        exception.printStackTrace();
                        Toast.makeText(FaceScanInterface.this, "Lỗi khi chụp ảnh: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void detectFaceAndCompare(FirebaseVisionImage image) {
        // Configure the face detector with high accuracy
        FirebaseVisionFaceDetectorOptions options = new FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .setMinFaceSize(0.15f) // For better detection of distant faces
                .enableTracking() // For tracking face movements
                .build();

        // Get instance of face detector
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector(options);

        // Start the face detection process
        detector.detectInImage(image)
                .addOnSuccessListener(faces -> {
                    if (faces.size() > 0) {
                        // Faces detected, proceed with comparison
                        Toast.makeText(FaceScanInterface.this, "Đã phát hiện " + faces.size() + " khuôn mặt", Toast.LENGTH_SHORT).show();
                        compareFace(faces);
                    } else {
                        Toast.makeText(FaceScanInterface.this, "Không phát hiện khuôn mặt", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(FaceScanInterface.this, "Lỗi quét khuôn mặt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void compareFace(List<FirebaseVisionFace> faces) {
        // Example user ID - in a real app, this would be determined by login or registration
        String userId = "user123";

        // Get face data from Firebase for the current user
        mDatabase.child("users").child(userId).child("face_data").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot snapshot = task.getResult();
                        if (snapshot.exists()) {
                            // Compare face with stored data
                            if (isFaceMatch(faces, snapshot)) {
                                Toast.makeText(FaceScanInterface.this, "Xác thực thành công", Toast.LENGTH_SHORT).show();

                                // Navigate to user interface
                                Intent intent = new Intent(FaceScanInterface.this, UserInterface.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(FaceScanInterface.this, "Không khớp. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(FaceScanInterface.this, "Không tìm thấy dữ liệu khuôn mặt", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(FaceScanInterface.this, "Lỗi khi truy cập dữ liệu: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isFaceMatch(List<FirebaseVisionFace> faces, DataSnapshot faceData) {
        // TODO: Implement actual face comparison logic
        // This is a simplified example - in a real app, you would:
        // 1. Extract facial features from the detected face
        // 2. Compare with stored facial features from the database
        // 3. Calculate similarity score and return true if above threshold

        // For demonstration, always return true
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}