package com.example.housemate;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.telecom.Call;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder> {

    private Context context;

    private ArrayList<Room> roomList;

    private StorageReference sReference;
    private ItemClickListener mItemListener;


    public RecyclerAdapter(Context context, ArrayList<Room> roomList, ItemClickListener itemClickListener) {
        this.context = context;
        this.roomList = roomList;
        this.mItemListener = itemClickListener;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);
        return new RecyclerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {

        Room room = roomList.get(position);

        Glide.with(holder.roomImage.getContext()).load(room.getImage()).into(holder.roomImage);

        holder.roomTitle.setText(room.getTitle());
        holder.roomCity.setText(room.getCity());
        holder.roomPrice.setText(room.getPrice() + "");
        holder.roomDescription.setText(room.getDescription());

        holder.itemView.setOnClickListener(view -> {
            mItemListener.onItemClick(roomList.get(position));
        });


    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public interface ItemClickListener{

        void onItemClick(Room room);

    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder{

        private ImageView roomImage;
        private TextView roomTitle, roomCity, roomPrice, roomDescription;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            roomImage = itemView.findViewById(R.id.postImage);
            roomTitle = itemView.findViewById(R.id.postTitle);
            roomCity = itemView.findViewById(R.id.postCity);
            roomPrice = itemView.findViewById(R.id.postPrice);
            roomDescription = itemView.findViewById(R.id.postDescription);

        }
    }


}
