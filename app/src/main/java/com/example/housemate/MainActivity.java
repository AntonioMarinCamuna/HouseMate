package com.example.housemate;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //Elementos globales
    private StorageReference sReference;
    private DatabaseReference dbReference;

    private FirebaseAuth mAuth;

    //Elementos MainActivity
    private FloatingActionButton button;
    private RecyclerView postRecycler;
    private RecyclerAdapter rAdapter;
    private ArrayList<Room> roomList;
    private Button searchButton;
    private TextView cityFilter, priceFilter;
    private ImageView userProfileButton, mainPageButton, myBookingsButton;

    //Elementos PublishDialog
    private Uri uri = null;
    private Button cancelButton, publishButton;
    private ImageView roomImagePicker;
    private TextView postTitle, postCity, postAddress, postDescription, postPrice, maximunDays;
    private String postImgName;
    private Dialog dialog;

    //Elementos PostDialog (elementos extra, algunos son compartidos con el otro dialog)
    private Button bookButton;
    private TextView postPublisher;
    private EditText desiredDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbReference = FirebaseDatabase.getInstance().getReference();

        postRecycler = findViewById(R.id.postRecycler);
        postRecycler.setLayoutManager(new LinearLayoutManager(this));

        cityFilter = findViewById(R.id.cityFilter);
        priceFilter = findViewById(R.id.priceFilter);

        userProfileButton = findViewById(R.id.userProfile);
        mainPageButton = findViewById(R.id.mainMenu);
        myBookingsButton = findViewById(R.id.myBookings);

        mAuth = FirebaseAuth.getInstance();

        roomList = new ArrayList<Room>();
        rAdapter = new RecyclerAdapter(this, roomList, new RecyclerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(Room room) {

                openPostDialog(room);

            }
        });

        postRecycler.setAdapter(rAdapter);

        searchButton = findViewById(R.id.searchButton);

        button = findViewById(R.id.postingButton);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (priceFilter.getText().toString().isEmpty()){
                    loadRoom(cityFilter.getText().toString(), "0");
                } else {
                    loadRoom(cityFilter.getText().toString(), priceFilter.getText().toString());
                }

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPublishDialog();
            }
        });

        userProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(MainActivity.this, UserInfoActivity.class);
                startActivity(i);

            }
        });

        myBookingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(MainActivity.this, MyBookingsActivity.class);
                startActivity(i);

            }
        });

    }

    private void openPostDialog(Room room){

        dialog = new Dialog(MainActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        View customDialog = inflater.inflate(R.layout.fragment_post_info, null);

        postTitle = customDialog.findViewById(R.id.roomTitle);
        postCity = customDialog.findViewById(R.id.roomCity);
        postDescription = customDialog.findViewById(R.id.roomDescription);
        postPublisher = customDialog.findViewById(R.id.roomPublisher);
        postPrice = customDialog.findViewById(R.id.roomPrice);
        roomImagePicker = customDialog.findViewById(R.id.roomImage);
        maximunDays = customDialog.findViewById(R.id.maximunDays);
        desiredDays = customDialog.findViewById(R.id.desiredDays);

        bookButton = customDialog.findViewById(R.id.bookButton);

        String postId = room.getPostId();

        postTitle.setText(room.getTitle());
        postCity.setText(room.getCity());
        postDescription.setText(room.getDescription());

        if(Locale.getDefault().getLanguage().equals("es")){

            maximunDays.setText(room.getMaxDays() + " noches");

        }else{

            maximunDays.setText(room.getMaxDays() + " nights");

        }

        dbReference.child("Users").child(room.getPublisherId()).child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String publisherName = snapshot.getValue(String.class);
                postPublisher.setText(publisherName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        postPrice.setText(room.getPrice());

        Glide.with(roomImagePicker.getContext()).load(room.getImage()).into(roomImagePicker);

        bookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mAuth.getCurrentUser().getUid().equals(room.getPublisherId())){

                    Toast.makeText(MainActivity.this, "No puedes reservar una habitacion publicada por ti", Toast.LENGTH_SHORT).show();

                }else {

                    if(desiredDays.getText().toString().equals("") || desiredDays.getText().toString().equals(" ")){

                        Toast.makeText(MainActivity.this, "Debes introducir un numero de dias si quieres realizar la reserva.", Toast.LENGTH_SHORT).show();

                    }else{

                        if(Integer.parseInt(desiredDays.getText().toString()) > 0 && Integer.parseInt(desiredDays.getText().toString()) <= Integer.parseInt(room.getMaxDays())){

                            String bookingUser = mAuth.getCurrentUser().getUid();

                            room.setBooked("yes");
                            room.setBookedBy(bookingUser);

                            HashMap Room = new HashMap();
                            Room.put("booked", "yes");
                            Room.put("bookedBy", bookingUser);
                            Room.put("bookedDays", desiredDays.getText().toString());

                            dbReference.child("Posts").child(postId).updateChildren(Room).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {

                                    if(task.isSuccessful()){

                                        Toast.makeText(MainActivity.this, "Habitacion reservada con exito", Toast.LENGTH_SHORT).show();
                                        dialog.cancel();

                                    }

                                }
                            });

                        }else{

                            Toast.makeText(MainActivity.this, "El numero de dias introducidos no es valido.", Toast.LENGTH_SHORT).show();

                        }

                    }

                }

            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(customDialog);
        dialog.show();

    }

    private void openPublishDialog() {

        dialog = new Dialog(MainActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        View customDialog = inflater.inflate(R.layout.fragment_publishing, null);

        cancelButton = customDialog.findViewById(R.id.cancelButton);
        publishButton = customDialog.findViewById(R.id.publishButton);
        roomImagePicker = customDialog.findViewById(R.id.roomImage);

        postTitle = customDialog.findViewById(R.id.roomTitle);
        postCity = customDialog.findViewById(R.id.roomCity);
        postAddress = customDialog.findViewById(R.id.roomAddress);
        postDescription = customDialog.findViewById(R.id.roomDescription);
        postPrice = customDialog.findViewById(R.id.roomPrice);
        maximunDays = customDialog.findViewById(R.id.maxDays);

        mAuth = FirebaseAuth.getInstance();
        sReference = FirebaseStorage.getInstance().getReference();
        dbReference = FirebaseDatabase.getInstance().getReference();

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        roomImagePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subirFoto();
            }
        });

        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String title = postTitle.getText().toString().trim();
                String city = postCity.getText().toString().trim();
                String address = postAddress.getText().toString().trim();
                String description = postDescription.getText().toString().trim();
                String price = postPrice.getText().toString().trim();
                String maxDays = maximunDays.getText().toString().trim();
                //HACER COMPROBACIÓN DE VALORES VÁLIDOS

                if (title.isEmpty() || city.isEmpty() || address.isEmpty() || description.isEmpty() || price.isEmpty() || maxDays.isEmpty()){

                    Toast.makeText(MainActivity.this, "Complete los datos", Toast.LENGTH_SHORT).show();

                }else if (uri == null){

                    Toast.makeText(MainActivity.this, "Debes elegir una imagen de usuario", Toast.LENGTH_SHORT).show();

                }else{

                    postImgName = imgNameGenerator();

                    FirebaseUser user = mAuth.getCurrentUser();
                    String publisherId = user.getUid();

                    sReference = FirebaseStorage.getInstance().getReference("roomsImages/" + postImgName);
                    sReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            sReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    String avatarUrl = uri.toString();
                                    postRoom(title, city, address, description, price, publisherId, avatarUrl, maxDays);

                                }
                            });

                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(MainActivity.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();

                        }

                    });

                }

            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(customDialog);
        dialog.show();

    }

    private void loadRoom(String city, String maxPrice){

        dbReference.child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                roomList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){

                    Room room = dataSnapshot.getValue(Room.class);
                    double price = Double.parseDouble(room.getPrice());

                    if(city.isEmpty() && maxPrice.equals("0") && room.getBooked().equals("no")){

                        roomList.add(room);

                    }else if(room.getCity().toLowerCase().equals(city.toLowerCase()) && price <= Double.parseDouble(maxPrice) && room.getBooked().equals("no")){

                        roomList.add(room);

                    }else if(city.isEmpty()){

                        if(price <= Double.parseDouble(maxPrice) && room.getBooked().equals("no")){

                            roomList.add(room);

                        }

                    }else if(maxPrice.equals("0")){

                        if(room.getCity().toLowerCase().equals(city.toLowerCase()) && room.getBooked().equals("no")){

                            roomList.add(room);

                        }

                    }

                }
                rAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void postRoom(String title, String city, String address, String description, String price, String publisherId, String postImage, String maxDays){

        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("city", city);
        map.put("address", address);
        map.put("description", description);
        map.put("price", price);
        map.put("publisherId", publisherId);
        map.put("image", postImage);
        map.put("booked", "no");
        map.put("bookedBy", "");
        map.put("maxDays", maxDays);
        map.put("bookedDays", "");
        map.put("postId", postImgName);

        dbReference.child("Posts").child(postImgName).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task2) {
                if(task2.isSuccessful()){

                    dialog.cancel();
                    Toast.makeText(MainActivity.this, "Habitacion publicada correctamente", Toast.LENGTH_SHORT).show();

                } else {

                    Toast.makeText(MainActivity.this, "Error al crear la colección.", Toast.LENGTH_SHORT).show();

                }
            }
        });

        uri = null;

    }

    private String imgNameGenerator() {

        return (postTitle.getText().toString() + postCity.getText().toString());

    }

    private void subirFoto(){

        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        mObtenerImg.launch(i);

    }

    private ActivityResultLauncher<Intent> mObtenerImg = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == Activity.RESULT_OK){

                        Intent data = result.getData();
                        assert data != null;
                        uri = data.getData();
                        roomImagePicker.setImageURI(uri);

                    }else {

                        Toast.makeText(MainActivity.this, "Accion cancelada por el usuario", Toast.LENGTH_SHORT).show();

                    }

                }

            }

    );

}