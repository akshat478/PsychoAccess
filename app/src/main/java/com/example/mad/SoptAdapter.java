package com.example.mad;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SoptAdapter extends RecyclerView.Adapter<SoptAdapter.SoptViewHolder> {
    private List<Integer> imageIds;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int imageId);
    }

    public SoptAdapter(List<Integer> imageIds, OnItemClickListener listener) {
        this.imageIds = imageIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SoptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sopt, parent, false);
        return new SoptViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SoptViewHolder holder, int position) {
        int imageId = imageIds.get(position);
        holder.imageView.setImageResource(imageId);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(imageId));
    }

    @Override
    public int getItemCount() { return imageIds.size(); }

    static class SoptViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        SoptViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivSoptItem);
        }
    }
}