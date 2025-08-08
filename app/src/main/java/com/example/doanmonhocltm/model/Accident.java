package com.example.doanmonhocltm.model;

import com.google.gson.annotations.SerializedName;

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

    // Default constructor (required for Gson)
    public Accident() {
    }

    // Constructor
    public Accident(int accident_id, String road_name, String timestamp,
                    String accident_type, String image_url, String status) {
        this.accident_id = accident_id;
        this.road_name = road_name;
        this.timestamp = timestamp;
        this.accident_type = accident_type;
        this.image_url = image_url;
        this.status = status;
    }

    // Getters
    public int getAccident_id() {
        return accident_id;
    }

    public String getRoad_name() {
        return road_name;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getAccident_type() {
        return accident_type;
    }

    public String getImage_url() {
        return image_url;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setAccident_id(int accident_id) {
        this.accident_id = accident_id;
    }

    public void setRoad_name(String road_name) {
        this.road_name = road_name;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setAccident_type(String accident_type) {
        this.accident_type = accident_type;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Helper methods with null checks
    public String getFormattedTime() {
        // Extract time from timestamp "2025-08-07T23:23:46"
        if (timestamp != null && timestamp.contains("T")) {
            String timePart = timestamp.split("T")[1];
            if (timePart.length() >= 5) {
                return timePart.substring(0, 5); // Get HH:MM
            }
        }
        return "00:00";
    }

    public String getFormattedDate() {
        // Extract date from timestamp "2025-08-07T23:23:46"
        if (timestamp != null && timestamp.contains("T")) {
            String datePart = timestamp.split("T")[0];
            String[] parts = datePart.split("-");
            if (parts.length == 3) {
                return parts[2] + "/" + parts[1] + "/" + parts[0]; // DD/MM/YYYY
            }
        }
        return "01/01/2025";
    }

    public String getStatusText() {
        if (status == null) {
            return "Không xác định";
        }

        switch (status.toLowerCase()) {
            case "wait":
                return "Chờ xử lý";
            case "en_route":
                return "Đang đến";
            case "arrived":
                return "Đã đến";
            case "pending":
                return "Chờ xử lý";
            default:
                return "Không xác định";
        }
    }

    public int getStatusColor() {
        if (status == null) {
            return 0xFF757575; // Dark grey
        }

        switch (status.toLowerCase()) {
            case "wait":
                return 0xFFFF9800; // Orange
            case "en_route":
                return 0xFFFF9800; // Orange
            case "arrived":
                return 0xFF4CAF50; // Green
            case "pending":
                return 0xFF9E9E9E; // Grey
            default:
                return 0xFF757575; // Dark grey
        }
    }

    public String getAccidentTypeText() {
        if (accident_type == null) {
            return "Tai nạn khác";
        }

        switch (accident_type.toLowerCase()) {
            case "car_crash":
                return "Tai nạn xe ô tô";
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
}