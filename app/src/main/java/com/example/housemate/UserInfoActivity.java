package com.example.housemate;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class UserInfoActivity extends AppCompatActivity {

    //Elementos de UserInfoActivity
    private FirebaseAuth mAuth;
    private StorageReference sReference;
    private DatabaseReference dbReference;

    private TextView emailUser, userName, userUsername;
    private ImageView userAvatar, mainPage, myBookings;
    private String user;

    private RecyclerView postRecycler;
    private RecyclerAdapter rAdapter;
    private ArrayList<Room> roomList;

    private Button logOutButton;


    //Elementos Dialog
    private Button cancelButton;
    private ImageView roomImagePicker;
    private TextView postTitle, postCity, postAddress, postDescription, postPrice, bookedBy;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        //Asignación de los elementos del Activity a los elentos previamente declarados.
        sReference = FirebaseStorage.getInstance().getReference();
        dbReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        userName = findViewById(R.id.userName);
        emailUser = findViewById(R.id.userMail);
        userAvatar = findViewById(R.id.userAvatar);
        userUsername = findViewById(R.id.userUsername);

        mainPage = findViewById(R.id.mainMenu);
        myBookings = findViewById(R.id.myBookings);

        postRecycler = findViewById(R.id.postsRecycler);
        postRecycler.setLayoutManager(new LinearLayoutManager(this));

        roomList = new ArrayList<Room>();
        rAdapter = new RecyclerAdapter(this, roomList, new RecyclerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(Room room) {

                openPostDialog(room); //Llamada al método encargado demostrar un Dialog con información de una publicación.

            }
        });

        postRecycler.setAdapter(rAdapter);

        loadRoom(); //Llamada al método encargado de cargar en el RecyclerView todas las habitaciónes del usuario.

        logOutButton = findViewById(R.id.logOut);

        user = mAuth.getCurrentUser().getUid();

        readData(); //Llamada al método encargado de leer los datos de información del usuario

        /**
         * Bloque de código encargado de cerrar la sesión del usuario. Nos enviará al LoginActivity
         */
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
                startActivity(new Intent(UserInfoActivity.this, LoginActivity.class));

            }
        });

        /**
         * Bloque de código encargado de enviarnos al MainActivity cuando el botón de navegación es
         * presionado.
         */
        mainPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(UserInfoActivity.this, MainActivity.class);
                startActivity(i);

            }
        });

        /**
         * Bloque de código encargado de enviarnos al MyBookingsActivity cuando el botón de navegación
         * es presionado.
         */
        myBookings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(UserInfoActivity.this, MyBookingsActivity.class);
                startActivity(i);

            }
        });

    }

    /**
     * Método encargado de cargar todas las habitaciones publicadas por el usuario en el ReyclerView.
     */
    private void loadRoom(){

        /**
         * Bloque de código encargado de buscar en la collección de Posts las habitaciones del usuario.
         */
        dbReference.child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                roomList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){ //Recorremos la lista de habitaciones.

                    Room room = dataSnapshot.getValue(Room.class);

                    if(room.getPublisherId().equals(user)){ //Obtenemos solo las que son del usuario actual.

                        roomList.add(room); //Añadimos las habitaciones a la lista del RecyclerView.

                    }

                }
                rAdapter.notifyDataSetChanged(); //Notificamos al Recycler cada vez que se añada información.

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    /**
     *
     * Método encargado de mostrar y gestionar un Dialog cuando se presiona un item del RecyclerView.
     *
     * @param room
     */
    private void openPostDialog(Room room){

        //Declaración y asignación del Dialog que se mostará por pantalla.
        dialog = new Dialog(UserInfoActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        View customDialog = inflater.inflate(R.layout.fragment_owned_posts, null);

        //Asignación de los elementos del Dialog a los elementos declarados globalmente.
        postTitle = customDialog.findViewById(R.id.roomTitle);
        postCity = customDialog.findViewById(R.id.roomCity);
        postDescription = customDialog.findViewById(R.id.roomDescription);
        postAddress = customDialog.findViewById(R.id.roomAddress);
        postPrice = customDialog.findViewById(R.id.roomPrice);
        roomImagePicker = customDialog.findViewById(R.id.roomImage);
        bookedBy = customDialog.findViewById(R.id.bookedBy);

        cancelButton = customDialog.findViewById(R.id.bookButton);

        String postId = room.getPostId();

        //"Seteo" de datos obtenidos de la habitación a los elementos del Dialog.
        postTitle.setText(room.getTitle());
        postCity.setText(room.getCity());
        postAddress.setText(room.getAddress());
        postDescription.setText(room.getDescription());

        /**
         * Bloque de código encargado de añadir al Dialog información referente a la persona que ha realizado una reserva.
         */
        dbReference.child("Users").child(room.getBookedBy()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                User user = snapshot.getValue(User.class); //Obtenemos los datos del usuario obtenido.
                bookedBy.setText(user.getName());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        postPrice.setText(room.getPrice());

        Glide.with(roomImagePicker.getContext()).load(room.getImage()).into(roomImagePicker);

        /**
         * Bloque de código encargado de eliminar una habitación de la colección siempre que esta
         * no esté reservada.
         */
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!mAuth.getCurrentUser().getUid().equals(room.getPublisherId())){ //Comprobamos que la habitación publicada es nuestra.

                    Toast.makeText(UserInfoActivity.this, "No puedes eliminar una habitacion publicada por otra persona", Toast.LENGTH_SHORT).show();

                }else {

                    if (room.getBooked().equals("yes")){ //Comprobamos que la habitación no está reservada

                        Toast.makeText(UserInfoActivity.this, "No puedes borrar una habitación reservada", Toast.LENGTH_SHORT).show();

                    } else{

                        /**
                         * Bloque de código encargado de eliminar de la colección Posts la habitacione.
                         */
                        dbReference.child("Posts").child(postId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                /**
                                 * Buscamos la imagen del post en el Storage de Firebase y la eliminamos.
                                 */
                                sReference.child("roomsImages").child(room.getTitle()+room.getCity()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                        dialog.cancel();

                                        //Una vez eliminada mostramos un mensaje informativo.
                                        Toast.makeText(UserInfoActivity.this, "Habitación eliminada correctamente", Toast.LENGTH_SHORT).show();

                                    }
                                });

                            }
                        });

                    }

                }

            }
        });

        //Mostramos el Dialog por pantalla.
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(customDialog);
        dialog.show();

    }

    /**
     * Método encargado de leer información a cerca del usuario actual, que asigna esta información
     * a los campos de texto de información del usuario del Activity
     */
    public void readData(){

        /**
         * Bloque de código encargado de buscar en la colección al usuario actual.
         */
        dbReference.child("Users").child(user).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                emailUser.setText(String.valueOf(snapshot.child("email").getValue()));
                userName.setText(String.valueOf(snapshot.child("name").getValue()));
                userUsername.setText(String.valueOf(snapshot.child("username").getValue()));

                Glide.with(UserInfoActivity.this).load(String.valueOf(snapshot.child("img_name").getValue())).into(userAvatar);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}