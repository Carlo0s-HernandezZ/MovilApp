package com.carloscode.gestorcam.activities;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.carloscode.gestorcam.R;
import com.carloscode.gestorcam.models.IncidentPhoto;

import java.util.List;

public class IncidentHistoryAdapter extends RecyclerView.Adapter<IncidentHistoryAdapter.ViewHolder> {

    private List<IncidentPhoto> photoList;
    private Context context;

    public IncidentHistoryAdapter(Context context, List<IncidentPhoto> photoList) {
        this.context = context;
        this.photoList = photoList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_incident_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IncidentPhoto photo = photoList.get(position);

        holder.tvTitle.setText("Incidencia " + (position + 1));
        holder.tvDate.setText(photo.getDate());

        Glide.with(context)
                .load(photo.getUri())
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.ivThumb);

        // Al clic, mostramos el diálogo con los datos REALES
        holder.itemView.setOnClickListener(v -> showDetailDialog(photo, position));
    }

    private void showDetailDialog(IncidentPhoto photo, int position) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_incident_detail);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageView ivPhoto = dialog.findViewById(R.id.iv_dialog_photo);
        TextView tvDesc = dialog.findViewById(R.id.tv_dialog_description);
        Button btnClose = dialog.findViewById(R.id.btn_close_dialog);

        // Foto Grande
        Glide.with(context).load(photo.getUri()).into(ivPhoto);

        // Descripción con los datos que capturó el guardabosques
        String detalleCompleto = "Reporte #" + (position + 1) + "\n" +
                "Fecha: " + photo.getDate() + "\n\n" +
                "Detalles:\n" + photo.getDescription();

        tvDesc.setText(detalleCompleto);

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvTitle, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.iv_incident_thumb);
            tvTitle = itemView.findViewById(R.id.tv_incident_title);
            tvDate = itemView.findViewById(R.id.tv_incident_date);
        }
    }
}