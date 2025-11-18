package com.carloscode.gestorcam.activities;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
//import com.bumptech.glide.Glide;
import com.carloscode.gestorcam.R;
import com.carloscode.gestorcam.models.IncidentPhoto;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<IncidentPhoto> photoList; // Ahora usamos la lista de objetos, no solo Uris
    private Context context;

    public HistoryAdapter(Context context, List<IncidentPhoto> photoList) {
        this.context = context;
        this.photoList = photoList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IncidentPhoto photo = photoList.get(position);

        // 1. Ponemos la fecha REAL que viene del modelo
        holder.tvDate.setText(photo.getDate());

        // 2. Cargamos la imagen con Glide
        Glide.with(context)
                .load(photo.getUri())
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.ivThumb);

        // 3. Click para abrir pantalla completa
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ImageViewerActivity.class);
            intent.putExtra("image_uri", photo.getUri().toString());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.iv_history_thumb);
            tvDate = itemView.findViewById(R.id.tv_history_date);
        }
    }
}