package com.example.doanmonhocltm.model;

import com.google.gson.annotations.SerializedName;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.graphics.Color;

public class Accident {

    @SerializedName("accidentId")
    private int accident_id;

    @SerializedName("roadName")
    private String road_name;

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("accidentType")
    private String accident_type;

    @SerializedName("imageUrl")
    private String image_url;

    @SerializedName("status")
    private String status;

    // Constructor
    public Accident() {}

    public Accident(int accident_id, String road_name, String timestamp,
                    String accident_type, String image_url, String status) {
        this.accident_id = accident_id;
        this.road_name = road_name;
        this.timestamp = timestamp;
        this.accident_type = accident_type;
        this.image_url = image_url;
        this.status = status;
    }

    // Getters and Setters
    public int getAccident_id() {
        return accident_id;
    }

    public void setAccident_id(int accident_id) {
        this.accident_id = accident_id;
    }

    public String getRoad_name() {
        return road_name;
    }

    public void setRoad_name(String road_name) {
        this.road_name = road_name;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getAccident_type() {
        return accident_type;
    }

    public void setAccident_type(String accident_type) {
        this.accident_type = accident_type;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Helper methods để format dữ liệu hiển thị
    public String getFormattedDate() {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            // Xử lý timestamp có thể có microseconds
            String cleanTimestamp = timestamp;
            if (timestamp.contains(".")) {
                cleanTimestamp = timestamp.substring(0, timestamp.indexOf("."));
            }

            Date date = inputFormat.parse(cleanTimestamp);
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "N/A";
        }
    }

    public String getFormattedTime() {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            // Xử lý timestamp có thể có microseconds
            String cleanTimestamp = timestamp;
            if (timestamp.contains(".")) {
                cleanTimestamp = timestamp.substring(0, timestamp.indexOf("."));
            }

            Date date = inputFormat.parse(cleanTimestamp);
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "N/A";
        }
    }

    public String getAccidentTypeText() {
        switch (accident_type) {
            case "car_crash":
                return "Tai nạn xe hơi";
            case "truck_rollover":
                return "Xe tải lật";
            case "motorcycle_accident":
                return "Tai nạn xe máy";
            case "pedestrian_accident":
                return "Tai nạn người đi bộ";
            default:
                return "Tai nạn khác";
        }
    }

    public String getStatusText() {
        switch (status) {
            case "wait":
                return "Đang chờ";
            case "en_route":
                return "Đang đến";
            case "arrived":
                return "Đã đến";
            case "resolved":
                return "Đã xử lý";
            case "completed":
                return "Hoàn thành";
            case "cancelled":
                return "Đã hủy";
            default:
                return "Không xác định";
        }
    }

    public int getStatusColor() {
        switch (status) {
            case "wait":
                return Color.parseColor("#FF9800"); // Orange - cho status "wait" từ JSON
            case "en_route":
                return Color.parseColor("#2196F3"); // Blue
            case "arrived":
                return Color.parseColor("#4CAF50"); // Green
            case "resolved":
            case "completed":
                return Color.parseColor("#9E9E9E"); // Grey
            case "cancelled":
                return Color.parseColor("#757575"); // Dark Grey
            default:
                return Color.parseColor("#757575"); // Dark Grey
        }
    }

    @Override
    public String toString() {
        return "Accident{" +
                "accident_id=" + accident_id +
                ", road_name='" + road_name + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", accident_type='" + accident_type + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}