package com.example.doanmonhocltm.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocationResponse {

//    {
//        "longitude": 106.705544,
//            "timestamp": "2025-08-14T15:03:04.069504",
//            "distance": 2.119,
//            "latitude": 10.935257
//    }


    private Double longitude;
    private Double latitude;
    private Double distance;

    private String timestamp;

    public Double getLongitude() {
        return longitude;
    }

    public LocationResponse(Double longitude) {
        this.longitude = longitude;
    }

    public LocationResponse(Double longitude, Double latitude, Double distance, String timestamp) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.distance = distance;
        this.timestamp = timestamp;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getDistance() {
        return distance;
    }

    public String getDistanceWithUnit() {
        if (distance == null) return "N/A";

        // Làm tròn 2 chữ số thập phân
        String formattedDistance = String.format(Locale.getDefault(), "%.2f", distance);

        return formattedDistance + " km";
    }


    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

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


}
