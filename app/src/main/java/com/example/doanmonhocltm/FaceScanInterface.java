package com.example.doanmonhocltm;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Base64;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FaceScanInterface extends AppCompatActivity implements SurfaceHolder.Callback {

    // UI
    private SurfaceView cameraPreview;
    private Button btnScan;
    private Button btnHistory;
    private ImageView imgViewScan;

    // Camera2 API
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CameraManager cameraManager;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size previewSize;
    private ImageReader imageReader;

    // Background Thread
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private final Semaphore cameraOpenCloseLock = new Semaphore(1);

    // Constants
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final String TAG = "FaceScanActivity";
    private boolean isProcessing = false;

    // API-related
    private static final String API_ENDPOINT = "YOUR_API_ENDPOINT_HERE"; // Replace with your actual API endpoint
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_scan_interface);  // Set layout

        initViews();
        setupListeners();
        checkCameraPermission();
    }

    private void initViews() {
        // UI
        cameraPreview = findViewById(R.id.imgViewScan);
        btnScan = findViewById(R.id.btnScan);
        btnHistory = findViewById(R.id.btnHistory);
        imgViewScan = findViewById(R.id.capturedImageView);

        //SurfaceView
        SurfaceHolder holder = cameraPreview.getHolder();
        holder.addCallback(this);
    }

    private void setupListeners() {
        //Scan
        btnScan.setOnClickListener(v -> {
            if (!isProcessing) {
                captureImage();
            }
        });

        btnHistory.setOnClickListener(v -> {
            // History
            Toast.makeText(this, "Lịch sử được nhấn", Toast.LENGTH_SHORT).show();
        });
    }

    private void checkCameraPermission() {
        // Kiểm tra và yêu cầu quyền camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            Log.d(TAG, "Requesting camera permission");
        } else {
            Log.d(TAG, "Camera permission already granted");
        }
    }

    private void captureImage() {
        if (cameraDevice == null || !cameraPreview.getHolder().getSurface().isValid()) {
            Toast.makeText(this, "Camera chưa sẵn sáng", Toast.LENGTH_SHORT).show();
            return;
        }

        isProcessing = true;
        btnScan.setEnabled(false);
        Toast.makeText(this, "Đang chụp khuôn mặt...", Toast.LENGTH_SHORT).show();

        try {
            // Create image reader for high-res capture
            if (imageReader == null) {
                imageReader = ImageReader.newInstance(
                        previewSize.getWidth(),
                        previewSize.getHeight(),
                        ImageFormat.JPEG,
                        1);
            }

            List<Surface> outputSurfaces = new ArrayList<>();
            outputSurfaces.add(imageReader.getSurface());

            // Thêm mặt vào preview
            outputSurfaces.add(cameraPreview.getHolder().getSurface());

            // Thiết lập trình nghe + chụp ảnh
            imageReader.setOnImageAvailableListener(reader -> {
                Image image = null;
                try {
                    image = reader.acquireLatestImage();
                    if (image != null) {
                        processCapturedImage(image);
                    }
                } finally {
                    if (image != null) {
                        image.close();
                    }
                }
            }, backgroundHandler);

            // tao request thu thập
            final CaptureRequest.Builder captureBuilder =
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageReader.getSurface());

            //
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            //
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, 90); // Portrait orientation

            // tạo phiên chụp ảnh mới
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                            @Override
                            public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                                           @NonNull CaptureRequest request,
                                                           @NonNull TotalCaptureResult result) {
                                Log.d(TAG, "Image captured successfully");
                            }

                            @Override
                            public void onCaptureFailed(@NonNull CameraCaptureSession session,
                                                        @NonNull CaptureRequest request,
                                                        @NonNull CaptureFailure failure) {
                                Log.e(TAG, "Image capture failed: " + failure.getReason());
                                runOnUiThread(() -> {
                                    Toast.makeText(FaceScanInterface.this,
                                            "Failed to capture image", Toast.LENGTH_SHORT).show();
                                    isProcessing = false;
                                    btnScan.setEnabled(true);
                                });
                            }
                        }, backgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            Toast.makeText(FaceScanInterface.this,
                                    "Error during image capture", Toast.LENGTH_SHORT).show();
                            isProcessing = false;
                            btnScan.setEnabled(true);
                        });
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.e(TAG, "Failed to configure capture session");
                    runOnUiThread(() -> {
                        Toast.makeText(FaceScanInterface.this,
                                "Cannot configure camera for capture", Toast.LENGTH_SHORT).show();
                        isProcessing = false;
                        btnScan.setEnabled(true);
                    });
                }
            }, backgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error capturing still image: " + e.getMessage());
            isProcessing = false;
            btnScan.setEnabled(true);
        }
    }

    private void processCapturedImage(Image image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);

        // Chuyển đổi sang bitmap để hiển thị
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        // Hiển thị hình ảnh đã chụp
        runOnUiThread(() -> {
            imgViewScan.setVisibility(View.VISIBLE);
            imgViewScan.setImageBitmap(bitmap);
            Toast.makeText(this, "Đang gửi khuôn mặt lên server...", Toast.LENGTH_SHORT).show();
        });

        // gửi tới A Minh
        sendImageToApi(bytes);
    }

    //chat GPT help m ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~e
    private void sendImageToApi(byte[] imageBytes) {
        // Chuyển đổi byte hình ảnh sang chuỗi Base64 để truyền API
        String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        try {
            // tạo request json
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("image", base64Image);
            // can them data khac vao

            RequestBody requestBody = RequestBody.create(
                    jsonRequest.toString(), JSON);

            Request request = new Request.Builder()
                    .url(API_ENDPOINT)
                    .post(requestBody)
                    .build();

            // goi API
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "API call failed: " + e.getMessage());
                    runOnUiThread(() -> {
                        Toast.makeText(FaceScanInterface.this,
                                "Không thể kết nối với server", Toast.LENGTH_SHORT).show();
                        isProcessing = false;
                        btnScan.setEnabled(true);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String responseBody = response.body().string();
                            JSONObject jsonResponse = new JSONObject(responseBody);

                            // Handle response according to your API's response format
                            boolean success = jsonResponse.optBoolean("success", false);

                            if (success) {
                                // Navigate to next page on success
                                runOnUiThread(() -> {
                                    Toast.makeText(FaceScanInterface.this,
                                            "Xác thực khuôn mặt thành công", Toast.LENGTH_SHORT).show();

                                    // Navigate to user interface
                                    Intent intent = new Intent(FaceScanInterface.this, UserInterface.class);
                                    startActivity(intent);
                                    finish();
                                });
                            } else {
                                String errorMessage = jsonResponse.optString("message", "Xác thực thất bại");
                                runOnUiThread(() -> {
                                    Toast.makeText(FaceScanInterface.this,
                                            errorMessage, Toast.LENGTH_SHORT).show();
                                    isProcessing = false;
                                    btnScan.setEnabled(true);
                                });
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing API response: " + e.getMessage());
                            runOnUiThread(() -> {
                                Toast.makeText(FaceScanInterface.this,
                                        "Lỗi xử lý phản hồi từ server", Toast.LENGTH_SHORT).show();
                                isProcessing = false;
                                btnScan.setEnabled(true);
                            });
                        }
                    } else {
                        Log.e(TAG, "API returned error: " + response.code());
                        runOnUiThread(() -> {
                            Toast.makeText(FaceScanInterface.this,
                                    "Server trả về lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                            isProcessing = false;
                            btnScan.setEnabled(true);
                        });
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON request: " + e.getMessage());
            runOnUiThread(() -> {
                Toast.makeText(this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                isProcessing = false;
                btnScan.setEnabled(true);
            });
        }
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try {
                backgroundThread.join();
                backgroundThread = null;
                backgroundHandler = null;
            } catch (InterruptedException e) {
                Log.e(TAG, "Error stopping background thread: " + e.getMessage());
            }
        }
    }

    private void startCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "No camera permission");
            return;
        }

        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            // Get front camera ID (typically selfie camera)
            String cameraId = getFrontCameraId();
            if (cameraId == null) {
                cameraId = cameraManager.getCameraIdList()[0]; // Fallback to first camera
            }

            // Get camera characteristics
            StreamConfigurationMap map = cameraManager.getCameraCharacteristics(cameraId)
                    .get(android.hardware.camera2.CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            if (map == null) {
                throw new RuntimeException("Cannot get camera configuration");
            }

            // Choose optimal preview size
            previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                    cameraPreview.getWidth(), cameraPreview.getHeight());

            // Open camera with callback
            cameraManager.openCamera(cameraId, stateCallback, backgroundHandler);
            Log.d(TAG, "Opening camera ID: " + cameraId);

        } catch (CameraAccessException e) {
            Log.e(TAG, "Camera access error: " + e.getMessage());
            Toast.makeText(this, "Cannot access camera", Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Log.e(TAG, "Camera permission error: " + e.getMessage());
        }
    }

    private String getFrontCameraId() {
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                Integer facing = cameraManager.getCameraCharacteristics(cameraId)
                        .get(android.hardware.camera2.CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == android.hardware.camera2.CameraCharacteristics.LENS_FACING_FRONT) {
                    return cameraId;
                }
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error finding front camera: " + e.getMessage());
        }
        return null;
    }

    private Size chooseOptimalSize(Size[] choices, int width, int height) {
        List<Size> bigEnough = new ArrayList<>();

        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * height / width &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, (o1, o2) -> Long.signum((long) o1.getWidth() * o1.getHeight() - (long) o2.getWidth() * o2.getHeight()));
        } else if (choices.length > 0) {
            return choices[0];
        } else {
            return new Size(640, 480);
        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG, "Camera opened");
            cameraOpenCloseLock.release();
            cameraDevice = camera;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.d(TAG, "Camera disconnected");
            cameraOpenCloseLock.release();
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG, "Camera open error: " + error);
            cameraOpenCloseLock.release();
            camera.close();
            cameraDevice = null;
            Toast.makeText(FaceScanInterface.this, "Error opening camera", Toast.LENGTH_SHORT).show();
        }
    };

    private void createCameraPreviewSession() {
        try {
            SurfaceHolder holder = cameraPreview.getHolder();
            Surface previewSurface = holder.getSurface();

            // check surface ok chua
            if (!previewSurface.isValid()) {
                Log.e(TAG, "Surface is not valid");
                return;
            }

            // tạo CaptureRequest.Builder cho preview
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(previewSurface);

            // tao CameraCaptureSession
            cameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            if (cameraDevice == null) {
                                Log.e(TAG, "CameraDevice is null when configuring session");
                                return;
                            }

                            cameraCaptureSession = session;
                            try {
                                // Configure auto mode for camera
                                captureRequestBuilder.set(CaptureRequest.CONTROL_MODE,
                                        android.hardware.camera2.CameraMetadata.CONTROL_MODE_AUTO);
                                // Start displaying camera preview
                                cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(),
                                        null, backgroundHandler);
                                Log.d(TAG, "Camera preview configured successfully");
                            } catch (CameraAccessException e) {
                                Log.e(TAG, "Error configuring camera preview: " + e.getMessage());
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Log.e(TAG, "Camera preview configuration failed");
                            Toast.makeText(FaceScanInterface.this, "Cannot configure camera",
                                    Toast.LENGTH_SHORT).show();
                        }
                    },
                    backgroundHandler);

        } catch (CameraAccessException e) {
            Log.e(TAG, "Error creating camera preview session: " + e.getMessage());
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Log.d(TAG, "Surface created");
        startBackgroundThread();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "Surface changed: " + width + "x" + height);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        Log.d(TAG, "Surface destroyed");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission granted");
                startCamera();
            } else {
                Log.e(TAG, "Camera permission denied");
                Toast.makeText(this, "App needs camera permission to function", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if (cameraPreview.getHolder().getSurface().isValid()) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            }
        }
        isProcessing = false;
        btnScan.setEnabled(true);
    }

    @Override
    protected void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void closeCamera() {
        try {
            cameraOpenCloseLock.acquire();
            if (cameraCaptureSession != null) {
                cameraCaptureSession.close();
                cameraCaptureSession = null;
            }
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (imageReader != null) {
                imageReader.close();
                imageReader = null;
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted when closing camera: " + e.getMessage());
        } finally {
            cameraOpenCloseLock.release();
        }
    }
}