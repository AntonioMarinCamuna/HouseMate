package com.example.housemate;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.telecom.Call;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Locale;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder> {

    //Elementos del RecyclerAdapter
    private Context context;

    private ArrayList<Room> roomList;
    private ItemClickListener mItemListener;

    /**
     *
     * Constructor del RecyclerAdapter, encargado de asignar los valores pasados por parámetro a los elementos del Recycler.
     *
     * @param context
     * @param roomList
     * @param itemClickListener
     */
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

    /**
     *
     * Método encargado de asignar información a un elemento del RecyclerView en base a su posición.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {

        Room room = roomList.get(position); //Obtenemos la habitación por su posición

        //Asignamos los valores de la habitación a los elementos definidos en el RecyclerViewHolder.
        Glide.with(holder.roomImage.getContext()).load(room.getImage()).into(holder.roomImage);

        holder.roomTitle.setText(room.getTitle());
        holder.roomCity.setText(room.getCity());
        holder.roomPrice.setText(room.getPrice() + "");
        holder.roomDescription.setText(room.getDescription());

        //Asignamos unos valores u otros en base a comprobar si está reservada o no.
        if (room.getBooked().equals("yes")){

            holder.isReserved.setVisibility(View.VISIBLE);

            //Asignamos unos valores u otros en base al idioma del dispositivo.
            if(Locale.getDefault().getLanguage().equals("es")){

                holder.maxDays.setText("Estancia reservada: " + room.getBookedDays() + " noches");

            }else{

                holder.maxDays.setText("Booked stay: " + room.getBookedDays() + " nights");

            }

        }else{

            holder.isReserved.setVisibility(View.INVISIBLE);

            if(Locale.getDefault().getLanguage().equals("es")){

                holder.maxDays.setText("Estancia máxima: " + room.getMaxDays() + " noches");

            }else{

                holder.maxDays.setText("Maximum stay: " + room.getMaxDays() + " nights");

            }

        }

        //Este bloque de código establece un onClickListener al item de la lista de la posición actual.
        holder.itemView.setOnClickListener(view -> {
            mItemListener.onItemClick(roomList.get(position));
        });

    }

    /**
     *
     * Método encargado de obtener el tamaño de la lista del RecyclewView.
     *
     * @return
     */
    @Override
    public int getItemCount() {
        return roomList.size();
    }

    /**
     * Interfaz para el onItemClick.
     */
    public interface ItemClickListener{

        void onItemClick(Room room);

    }

    /**
     * Clase RecyclerViewHolder con los elementos del cada item del RecyclerView.
     */
    public static class RecyclerViewHolder extends RecyclerView.ViewHolder{

        //Elementos del recycler
        private ImageView roomImage;
        private TextView roomTitle, roomCity, roomPrice, roomDescription, isReserved, maxDays;

        /**
         *
         * Constructor del RecyclerViewHolder.
         *
         * @param itemView
         */
        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            roomImage = itemView.findViewById(R.id.postImage);
            roomTitle = itemView.findViewById(R.id.postTitle);
            roomCity = itemView.findViewById(R.id.postCity);
            roomPrice = itemView.findViewById(R.id.postPrice);
            roomDescription = itemView.findViewById(R.id.postDescription);
            isReserved = itemView.findViewById(R.id.isReserved);
            maxDays = itemView.findViewById(R.id.maxDaysAvailable);

        }
    }

}
