package com.example.housemate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class MyBookingsActivity extends AppCompatActivity {

    private StorageReference sReference;
    private DatabaseReference dbReference;
    private FirebaseAuth mAuth;

    private RecyclerView postRecycler;
    private RecyclerAdapter rAdapter;
    private ArrayList<Room> roomList;
    private String user;
    private ImageView mainPage, userPage;

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

                Toast.makeText(MyBookingsActivity.this, "Hola", Toast.LENGTH_SHORT).show();

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
