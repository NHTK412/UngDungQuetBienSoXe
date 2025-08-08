package com.example.doanmonhocltm.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doanmonhocltm.LoginActivity;
import com.example.doanmonhocltm.callapi.ApiClient;
import com.example.doanmonhocltm.callapi.ApiService;
import com.example.doanmonhocltm.model.Accident;
import com.google.android.material.button.MaterialButton;
import com.example.doanmonhocltm.R;

import java.util.ArrayList;
import java.util.List;

public class AccidentListAdapter extends RecyclerView.Adapter<AccidentListAdapter.AccidentViewHolder> {

    private static final String TAG = "AccidentAdapter";

    private Context context;
    private List<Accident> accidents;
    private List<Accident> accidentsFiltered;
    private OnItemClickListener listener;


    public interface OnItemClickListener {
        void onItemClick(Accident accident);
        void onViewDetailsClick(Accident accident);
    }

    public AccidentListAdapter(Context context, List<Accident> accidents) {
        this.context = context;
        this.accidents = new ArrayList<>(accidents); // Create new list
        this.accidentsFiltered = new ArrayList<>(accidents); // Create new list

        Log.d(TAG, "Adapter created with " + accidents.size() + " accidents");
        Log.d(TAG, "accidentsFiltered size: " + this.accidentsFiltered.size());
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

        // Set status
        holder.tvStatus.setText(accident.getStatusText());
        GradientDrawable statusBackground = (GradientDrawable) holder.tvStatus.getBackground().mutate();
        statusBackground.setColor(accident.getStatusColor());

        // Set accident type icon
        int iconResource = getAccidentTypeIcon(accident.getAccident_type());
        holder.ivAccidentTypeIcon.setImageResource(iconResource);

        // Set placeholder image
        holder.ivAccidentImage.setImageResource(R.drawable.ic_accident_placeholder);

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
                if (accident.getStatus().equals(status)) {
                    accidentsFiltered.add(accident);
                }
            }
        }

        Log.d(TAG, "After filter - filtered size: " + accidentsFiltered.size());
        notifyDataSetChanged();
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
        ImageView ivAccidentImage, ivAccidentTypeIcon;
        TextView tvAccidentId, tvRoadName, tvTimestamp, tvDate, tvAccidentType, tvStatus;
        MaterialButton btnViewDetails;

        public AccidentViewHolder(@NonNull View itemView) {
            super(itemView);

            ivAccidentImage = itemView.findViewById(R.id.ivAccidentImage);
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