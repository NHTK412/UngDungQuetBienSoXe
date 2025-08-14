package com.example.doanmonhocltm.adapter;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doanmonhocltm.AccidentDetailActivity;
import com.example.doanmonhocltm.LoginActivity;
import com.example.doanmonhocltm.callapi.ApiClient;
import com.example.doanmonhocltm.callapi.ApiService;
import com.example.doanmonhocltm.model.Accident;
import com.google.android.material.button.MaterialButton;
import com.example.doanmonhocltm.R;

import org.checkerframework.checker.units.qual.C;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccidentListAdapter extends RecyclerView.Adapter<AccidentListAdapter.AccidentViewHolder> {

    private static final String TAG = "AccidentAdapter";

    private Context context;
    private List<Accident> accidents;
    private List<Accident> accidentsFiltered;
    private OnItemClickListener listener;

    private ApiService apiService;

    public interface OnItemClickListener {
        void onItemClick(Accident accident);

        void onViewDetailsClick(Accident accident);
    }

    public AccidentListAdapter(Context context, List<Accident> accidents) {
        this.context = context;
        this.accidents = new ArrayList<>(accidents);
        this.accidentsFiltered = new ArrayList<>(accidents);

        this.apiService = apiService = ApiClient.getClient(context).create(ApiService.class);


        Log.d(TAG, "Adapter created with " + accidents.size() + " accidents");
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public AccidentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder called");
        View view = LayoutInflater.from(context).inflate(R.layout.item_accident, parent, false);
        return new AccidentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccidentViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder called for position: " + position);

        if (position >= accidentsFiltered.size()) {
            Log.e(TAG, "Position out of bounds: " + position + ", size: " + accidentsFiltered.size());
            return;
        }

        Accident accident = accidentsFiltered.get(position);
        Log.d(TAG, "Binding accident: ID=" + accident.getAccident_id() + ", Road=" + accident.getRoad_name());

        // Set basic info
        holder.tvAccidentId.setText("ID: #" + accident.getAccident_id());
        holder.tvRoadName.setText(accident.getRoad_name());
        holder.tvTimestamp.setText(accident.getFormattedTime());
        holder.tvDate.setText(accident.getFormattedDate());
        holder.tvAccidentType.setText(accident.getAccidentTypeText());

        // Set status với background color
        holder.tvStatus.setText(accident.getStatusText());
        GradientDrawable statusBackground = (GradientDrawable) holder.tvStatus.getBackground().mutate();
        statusBackground.setColor(accident.getStatusColor());

        // Set accident type icon
        int iconResource = getAccidentTypeIcon(accident.getAccident_type());
        holder.ivAccidentTypeIcon.setImageResource(iconResource);

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(accident);
            }
        });

        holder.btnViewDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetailsClick(accident);
            } else {
                Toast.makeText(context, "Xem chi tiết tai nạn #" + accident.getAccident_id(),
                        Toast.LENGTH_SHORT).show();

//                Call<ResponseBody> callImageAccident = apiService.getImageAccident(accident.getImage_url());
//
//                callImageAccident.enqueue(new Callback<ResponseBody>() {
//                    @Override
//                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                        try {
//                            if (response.isSuccessful() && response.body() != null) {
//                                Toast.makeText(context, "Xem chi tiết tai nạn #" + accident.getAccident_id(),
//                                        Toast.LENGTH_SHORT).show();
//
//                                byte[] imageBytes = response.body().bytes();
//                                Log.e(TAG, "Image bytes length: " + imageBytes.length);
//
//                                Bundle bundle = new Bundle();
//                                bundle.putString("roadName", accident.getRoad_name());
//                                bundle.putString("timestamp", accident.getFormattedTime());
//                                bundle.putInt("accidentId", accident.getAccident_id());
//                                bundle.putString("status", accident.getStatus());
//                                bundle.putString("accidentType", accident.getAccident_type());
//                                bundle.putByteArray("imageAccident", imageBytes);
//
//                                Intent intent = new Intent(context, AccidentDetailActivity.class);
//                                intent.putExtra("data", bundle);
//
//                                // Nếu context không phải Activity, thêm flag này
////                                if (!(context instanceof Activity)) {
////                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////                                }
//
//                                context.startActivity(intent);
//                                Log.e(TAG, "Started AccidentDetailActivity");
//
//                            } else {
//                                Log.e(TAG, "Response not successful or body is null");
//                                Toast.makeText(context, "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show();
//                            }
//                        } catch (IOException e) {
//                            Log.e(TAG, "Error processing image: " + e.getMessage());
//                            e.printStackTrace();
//                            Toast.makeText(context, "Lỗi xử lý hình ảnh", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//
//
//                    @Override
//                    public void onFailure(Call<ResponseBody> call, Throwable t) {
//
//                        Log.e("API", "Request failed: " + t.getMessage());
//                    }
//                });


            }
        });

        Log.d(TAG, "Binding completed for position: " + position);
    }

    @Override
    public int getItemCount() {
        int count = accidentsFiltered.size();
        Log.d(TAG, "getItemCount called, returning: " + count);
        return count;
    }

    public void updateData(List<Accident> newAccidents) {
        Log.d(TAG, "updateData called with " + newAccidents.size() + " accidents");

        // Clear and update both lists
        this.accidents.clear();
        this.accidents.addAll(newAccidents);

        this.accidentsFiltered.clear();
        this.accidentsFiltered.addAll(newAccidents);

        Log.d(TAG, "After update - accidents: " + this.accidents.size() + ", filtered: " + this.accidentsFiltered.size());

        // Notify adapter
        notifyDataSetChanged();
        Log.d(TAG, "notifyDataSetChanged() called");
    }

    public void filterByStatus(String status) {
        Log.d(TAG, "filterByStatus called with: " + status);

        accidentsFiltered.clear();

        if (status.equals("all")) {
            accidentsFiltered.addAll(accidents);
        } else {
            for (Accident accident : accidents) {
                // Map filter status to actual status values từ JSON data
                String actualStatus = mapFilterToStatus(status);
                if (accident.getStatus().equals(actualStatus)) {
                    accidentsFiltered.add(accident);
                }
            }
        }

        Log.d(TAG, "After filter - original size: " + accidents.size() + ", filtered size: " + accidentsFiltered.size());
        notifyDataSetChanged();
    }

    private String mapFilterToStatus(String filterStatus) {
        switch (filterStatus) {
            case "en_route":
                return "en_route";
            case "arrived":
                return "arrived";
            default:
                return "wait";
        }
    }

    private int getAccidentTypeIcon(String accidentType) {
        switch (accidentType) {
            case "car_crash":
                return R.drawable.ic_car_crash;
            case "truck_rollover":
                return R.drawable.ic_truck_rollover;
            case "motorcycle_accident":
                return R.drawable.ic_motorcycle;
            case "pedestrian_accident":
                return R.drawable.ic_pedestrian;
            default:
                return R.drawable.ic_warning;
        }
    }

    public static class AccidentViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAccidentTypeIcon;
        TextView tvAccidentId, tvRoadName, tvTimestamp, tvDate, tvAccidentType, tvStatus;
        MaterialButton btnViewDetails;

        public AccidentViewHolder(@NonNull View itemView) {
            super(itemView);

            ivAccidentTypeIcon = itemView.findViewById(R.id.ivAccidentTypeIcon);
            tvAccidentId = itemView.findViewById(R.id.tvAccidentId);
            tvRoadName = itemView.findViewById(R.id.tvRoadName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAccidentType = itemView.findViewById(R.id.tvAccidentType);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }
    }
}