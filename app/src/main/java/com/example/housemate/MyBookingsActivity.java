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

    //Elementos globales
    private StorageReference sReference;
    private DatabaseReference dbReference;
    private FirebaseAuth mAuth;

    //Elementos del Activity
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

        //Asignación de los elementos del Activity a los elentos previamente declarados.
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

                openPostDialog(room); //En caso de click en un elemento del recycler, llamamos al método encargado de lanzar el Dialog del post.

            }
        });

        postRecycler.setAdapter(rAdapter);

        user = mAuth.getCurrentUser().getUid();

        loadRoom(); //Llamada al método encargado de cargar las imagenes en el RecyclerView del activity.

        /**
         * Bloque de código encargado de lanzar el MainActivity cuando el botón del navegador es presionado.
         */
        mainPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(MyBookingsActivity.this, MainActivity.class);
                startActivity(i);

            }
        });

        /**
         * Bloque de código en cargado de lanzar el UserInfoActivity cuando el botón de la barra de navegación es presionado.
         */
        userPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(MyBookingsActivity.this, UserInfoActivity.class);
                startActivity(i);

            }
        });

    }

    /**
     *
     * Método encargado de mostrar el Dialog encargado de enseñar la información relativa al post
     * seleccionado en el RecyclerView. Este contendrá un botón que permitirá cancelar la reserva realizada.
     *
     * @param room
     */
    private void openPostDialog(Room room){

        //Declaración y asignación del Dialog que se mostará por pantalla.
        dialog = new Dialog(MyBookingsActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        View customDialog = inflater.inflate(R.layout.fragment_booked_post, null);

        //Asignación de los elemtos del Dialog a los elementos declarados globalmente.
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

        //Comprobamos el total del precio, calculando el precio por noche por el total de noches reservado.
        double totalPrice = Integer.parseInt(room.getBookedDays()) * Double.parseDouble(room.getPrice());

        postPrice.setText(totalPrice + "");

        Glide.with(roomImagePicker.getContext()).load(room.getImage()).into(roomImagePicker);

        /**
         * Bloque de código encargado de cancelar la reserva hecha por el usuario cuando el botón
         * Cancelar Reserva es presionado.
         */
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Modificamos los datos de la habitación almacenada en la lista del RecyclerView.
                room.setBooked("no");
                room.setBookedBy("");
                room.setBookedDays("");

                //Guardamos la información que queremos actualizar en la BD.
                HashMap Room = new HashMap();
                Room.put("booked", "no");
                Room.put("bookedBy", "");
                Room.put("bookedDays", "");

                /**
                 * Bloque de código encargado de buscar en la base de datos el post seleccionado
                 * y actualizar los datos con los valores oportunos.
                 */
                dbReference.child("Posts").child(postId).updateChildren(Room).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        if(task.isSuccessful()){

                            //Mensaje de confirmación de cancelación de reserva.
                            Toast.makeText(MyBookingsActivity.this, "Reserva cancelada con exito", Toast.LENGTH_SHORT).show();
                            dialog.cancel();

                        }

                    }
                });

            }
        });

        //Mostramos el Dialog por pantalla.
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(customDialog);
        dialog.show();

    }

    /**
     * Método encargado de cargar las habitaciones reservadas por el usuario.
     */
    private void loadRoom(){

        /**
         * Bloque de código encargado de buscar en la BD las habitaciones reservadas por el usuario actual
         * añadiéndolos a la lista de habitaciones del RecyclerView
         */
        dbReference.child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                roomList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){ //Recorremos todos los datos obtenidos

                    Room room = dataSnapshot.getValue(Room.class);

                    //Buscamos los que están reservados por el usuario actual.
                    if(room.getBookedBy().equals(user) && room.getBooked().equals("yes")){

                        roomList.add(room);

                    }

                }
                rAdapter.notifyDataSetChanged(); //Notificamos al RecycleView que se han añadido elementos.

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
