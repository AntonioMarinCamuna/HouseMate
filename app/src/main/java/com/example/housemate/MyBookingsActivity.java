package com.example.housemate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class MyBookingsActivity extends AppCompatActivity {

    private StorageReference sReference;
    private DatabaseReference dbReference;
    private FirebaseAuth mAuth;

    private RecyclerView postRecycler;
    private RecyclerAdapter rAdapter;
    private ArrayList<Room> roomList;
    private String user;
    private ImageView mainPage, userPage;

    //Elementos Dialog
    private Button cancelButton;
    private ImageView roomImagePicker;
    private TextView postTitle, postCity, postAddress, postDescription, postPrice;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        sReference = FirebaseStorage.getInstance().getReference();
        dbReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        postRecycler = findViewById(R.id.postRecyclerView);
        postRecycler.setLayoutManager(new LinearLayoutManager(this));

        mainPage = findViewById(R.id.mainMenu);
        userPage = findViewById(R.id.userProfile);

        roomList = new ArrayList<Room>();
        rAdapter = new RecyclerAdapter(this, roomList, new RecyclerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(Room room) {

                openPostDialog(room);

            }
        });

        postRecycler.setAdapter(rAdapter);

        user = mAuth.getCurrentUser().getUid();

        loadRoom();

        mainPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(MyBookingsActivity.this, MainActivity.class);
                startActivity(i);

            }
        });

        userPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(MyBookingsActivity.this, UserInfoActivity.class);
                startActivity(i);

            }
        });

    }

    private void openPostDialog(Room room){

        dialog = new Dialog(MyBookingsActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        View customDialog = inflater.inflate(R.layout.fragment_booked_post, null);

        postTitle = customDialog.findViewById(R.id.roomTitle);
        postCity = customDialog.findViewById(R.id.roomCity);
        postDescription = customDialog.findViewById(R.id.roomDescription);
        postAddress = customDialog.findViewById(R.id.roomAddress);
        postPrice = customDialog.findViewById(R.id.roomPrice);
        roomImagePicker = customDialog.findViewById(R.id.roomImage);

        cancelButton = customDialog.findViewById(R.id.bookButton);

        String postId = room.getPostId();

        postTitle.setText(room.getTitle());
        postCity.setText(room.getCity());
        postAddress.setText(room.getAddress());

        postDescription.setText(room.getDescription());

        double totalPrice = Integer.parseInt(room.getBookedDays()) * Double.parseDouble(room.getPrice());

        postPrice.setText(totalPrice + "");

        Glide.with(roomImagePicker.getContext()).load(room.getImage()).into(roomImagePicker);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mAuth.getCurrentUser().getUid().equals(room.getPublisherId())){

                    Toast.makeText(MyBookingsActivity.this, "No puedes reservar una habitacion publicada por ti", Toast.LENGTH_SHORT).show();

                }else {

                    String bookingUser = mAuth.getCurrentUser().getUid();

                    room.setBooked("no");
                    room.setBookedBy("");
                    room.setBookedDays("");

                    HashMap Room = new HashMap();
                    Room.put("booked", "no");
                    Room.put("bookedBy", "");
                    Room.put("bookedDays", "");

                    dbReference.child("Posts").child(postId).updateChildren(Room).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {

                            if(task.isSuccessful()){

                                Toast.makeText(MyBookingsActivity.this, "Reserva cancelada con exito", Toast.LENGTH_SHORT).show();
                                dialog.cancel();

                            }

                        }
                    });

                }

            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(customDialog);
        dialog.show();

    }

    private void loadRoom(){

        dbReference.child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                roomList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){

                    Room room = dataSnapshot.getValue(Room.class);

                    if(room.getBookedBy().equals(user) && room.getBooked().equals("yes")){

                        roomList.add(room);

                    }

                }
                rAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
