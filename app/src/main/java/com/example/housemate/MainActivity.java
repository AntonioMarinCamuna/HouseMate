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

/**
 * Clase MainActivity que extiende de AppCompatActivity. Esta es la encargada de gestionar la pantalla
 * principal, declara los elementos de la misma y contiene todas las funciones necesarias para el funcionamiento
 * de la app.
 */
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

    /**
     *
     * Método encargado de asignar valores y llamar a las funciones necesarias para que el
     * programa funcione de manera correcta.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Asignación de los elementos del Activity a los elentos previamente declarados.
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
            public void onItemClick(Room room) { //Acción llevada a cabo en caso de hacer click en un elemento del RecyclerView

                openPostDialog(room); //Llamada al método encargado de mostrar un Dialog con información del item seleccionado

            }
        });

        postRecycler.setAdapter(rAdapter);

        searchButton = findViewById(R.id.searchButton);
        button = findViewById(R.id.postingButton);

        /**
         * Bloque de código encargado de realizar la búsqueda de habitaciones cuando se hace click
         * en el botón searchButton, comprueba si los filtros tienen contenido o no y actúa en base
         * a ello llamando al método loadRoom con unos datos u otros.
         */
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                roomList.clear(); //Línea de código encargada de limpiar la lista cada vez que se hace una búsqueda. TODO

                if (priceFilter.getText().toString().isEmpty()){ //Caso en el que los filtros están vacíos

                    loadRoom(cityFilter.getText().toString(), "0"); //Llamada al método encargado de buscar las habitaciones.

                } else { //Caso en el que alguno de los filtros tienen contenido

                    loadRoom(cityFilter.getText().toString(), priceFilter.getText().toString()); //Llamada al método encargado de buscar las habitaciones.

                }

            }
        });

        /**
         * Bloque de código encargado de mostrar un Dialog para publicar una habitación, realiza una
         * llamada al método encargado de mostrarlo.
         */
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPublishDialog(); //Llamada al método encargado de mostrar el Dialog.
            }
        });

        /**
         * Bloque de código encargado de lanzar el activity UserInfoActivity cuando el botón del
         * navegador es pulsado.
         */
        userProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(MainActivity.this, UserInfoActivity.class);
                startActivity(i);

            }
        });

        /**
         * Bloque de código encargado de lanzar el activity MyBookingsActivity cuando el botón del
         * navegador es pulsado.
         */
        myBookingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(MainActivity.this, MyBookingsActivity.class);
                startActivity(i);

            }
        });

    }

    /**
     *
     * Método encargado de mostrar en pantalla el Dialog encargado de mostrar la información de la
     * habitación del RecyclerView seleccionado, también permitirá reservar dicha habitación si el
     * total de días introducidos es válido.
     *
     * @param room
     */
    private void openPostDialog(Room room){

        //Declaración y asignación del Dialog que se mostará por pantalla.
        dialog = new Dialog(MainActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        View customDialog = inflater.inflate(R.layout.fragment_post_info, null);

        //Asignación de los elementos del Dialog a los elementos declarados globalmente.
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

        //"Seteo" de datos obtenidos de la habitación a los elementos del Dialog.
        postTitle.setText(room.getTitle());
        postCity.setText(room.getCity());
        postDescription.setText(room.getDescription());

        //Diferenciación entre idioma Inglés y Español, en cada uno de los casos se asigna información
        //en un idioma u otro.
        if(Locale.getDefault().getLanguage().equals("es")){

            maximunDays.setText(room.getMaxDays() + " noches");

        }else{

            maximunDays.setText(room.getMaxDays() + " nights");

        }

        /**
         * Bloque de código encargado de buscar en la base de datos el nombre de la persona que ha
         * publicado una habitación en base a su ID.
         */
        dbReference.child("Users").child(room.getPublisherId()).child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String publisherName = snapshot.getValue(String.class); //Asignamos a un String el dato obtenido en la búsqueda de información.
                postPublisher.setText(publisherName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        postPrice.setText(room.getPrice());

        //Utilizamos el Glide para asignar la imagen en base a la referencia que tiene en la base de datos.
        Glide.with(roomImagePicker.getContext()).load(room.getImage()).into(roomImagePicker);

        /**
         * Bloque de código encargado de realizar la reserva del usuario que la realiza, en caso de
         * la persona que la intente realizar sea la misma que lo publica, no será disponible. Solo
         * permitirá la reserva si los datos introducidos son válidos.
         */
        bookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mAuth.getCurrentUser().getUid().equals(room.getPublisherId())){ //Comprobamos que la persona que intenta hacer la reserva no es la misma que lo publica.

                    Toast.makeText(MainActivity.this, "No puedes reservar una habitacion publicada por ti", Toast.LENGTH_SHORT).show();

                }else {

                    //Comprobamos que los datos introducidos no están vacíos
                    if(desiredDays.getText().toString().equals("") || desiredDays.getText().toString().equals(" ")){

                        Toast.makeText(MainActivity.this, "Debes introducir un numero de dias si quieres realizar la reserva.", Toast.LENGTH_SHORT).show();

                    }else{

                        //Comprobamos que el valor es válido, viendo si es mayor que 0 y menor que el máximo ofertado.
                        if(Integer.parseInt(desiredDays.getText().toString()) > 0 && Integer.parseInt(desiredDays.getText().toString()) <= Integer.parseInt(room.getMaxDays())){

                            String bookingUser = mAuth.getCurrentUser().getUid(); //Obtenemos el Id de la persona que quiere realizar la reserva.

                            //Añadimos información a la habitación guardada en la lista.
                            room.setBooked("yes");
                            room.setBookedBy(bookingUser);

                            //Modificamos la información de la habitación en la base de datos.
                            HashMap Room = new HashMap();
                            Room.put("booked", "yes");
                            Room.put("bookedBy", bookingUser);
                            Room.put("bookedDays", desiredDays.getText().toString());

                            /**
                             * Bloque de código encargado de hacer el Update de la información en la base de datos.
                             * En caso de éxito, se mostrará un mensaje informando de ello.
                             */
                            dbReference.child("Posts").child(postId).updateChildren(Room).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {

                                    if(task.isSuccessful()){

                                        Toast.makeText(MainActivity.this, "Habitacion reservada con exito", Toast.LENGTH_SHORT).show();
                                        dialog.cancel();

                                    }

                                }
                            });

                        }else{ //Caso en el que el número de días introducido no es válido.

                            Toast.makeText(MainActivity.this, "El numero de dias introducidos no es valido.", Toast.LENGTH_SHORT).show();

                        }

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
     * Método encargado de abrir el Dialog utilizado para publicar una habitación.
     */
    private void openPublishDialog() {

        //Declaración y asignación del Dialog que se mostará por pantalla.
        dialog = new Dialog(MainActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        View customDialog = inflater.inflate(R.layout.fragment_publishing, null);

        //Asignación de los elemtos del Dialog a los elementos declarados globalmente.
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

        /**
         * Bloque de código encargado de cerrar el Dialog cuando se presiona el botón Cancelar.
         */
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel(); //Cerramos el Dialog.
            }
        });

        /**
         * Bloque de código encargado de llamar al método que permite seleccionar una imagen del móvil.
         */
        roomImagePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subirFoto(); //Llamada al método encargado de seleccionar imagenes del dispositivo.
            }
        });

        /**
         * Bloque de código encargado de comprobar que los datos introducidos son válidos, en caso afirmativo
         * se publicará la habitación, almacenando los datos en la BD. En caso de error, se mostrará por
         * pantalla el error de turno.
         */
        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Guardamos en variables locales los datos introducidos en los campos de texto.
                String title = postTitle.getText().toString().trim();
                String city = postCity.getText().toString().trim();
                String address = postAddress.getText().toString().trim();
                String description = postDescription.getText().toString().trim();
                String price = postPrice.getText().toString().trim();
                String maxDays = maximunDays.getText().toString().trim();

                //Comprobamos que todos los datos están rellenos.
                if (title.isEmpty() || city.isEmpty() || address.isEmpty() || description.isEmpty() || price.isEmpty() || maxDays.isEmpty()){

                    Toast.makeText(MainActivity.this, "Complete los datos", Toast.LENGTH_SHORT).show();

                }else if (uri == null){ //Comprobamos que hay una imagen seleccionada.

                    Toast.makeText(MainActivity.this, "Debes subir una foto de la habitación", Toast.LENGTH_SHORT).show();

                }else{ //Caso en el que la información introducida es válida.

                    postImgName = imgNameGenerator(); //Llamada al método encargado de genera un nombre para la imagen en base a los datos del post.

                    FirebaseUser user = mAuth.getCurrentUser();
                    String publisherId = user.getUid();

                    /**
                     * Bloque de código encargado de subir la imagen al almacenamiento de Firebase.
                     * En caso de exito, se llamará al método que subirá los datos del post a la BD.
                     */
                    sReference = FirebaseStorage.getInstance().getReference("roomsImages/" + postImgName);
                    sReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            /**
                             * Bloque de código encargado de obtener el link de la imagen previamente subida.
                             */
                            sReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    String avatarUrl = uri.toString();

                                    //Llamada al método encargado de subir el post a la BD con los datos enviados por parámetro.
                                    postRoom(title, city, address, description, price, publisherId, avatarUrl, maxDays);

                                }
                            });

                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) { //Caso de error al subir la imagen al almacenamiento.

                            Toast.makeText(MainActivity.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();

                        }

                    });

                }

            }
        });

        //Mostramos el Dialog por pantalla.
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(customDialog);
        dialog.show();

    }

    /**
     *
     * Método encargado de añadir a la lista del RecyclerView las habitaciones encontradas en base a los
     * filtros completaodos.
     *
     * @param city
     * @param maxPrice
     */
    private void loadRoom(String city, String maxPrice){

        /**
         * Bloque de código encargado de consultar todos los post subidos en la BD.
         */
        dbReference.child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                roomList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){ //Recorremos los post encontrados en la búsqueda.

                    Room room = dataSnapshot.getValue(Room.class);
                    double price = Double.parseDouble(room.getPrice());

                    //Filtramos en base a los datos introducidos en los filtros.
                    if(city.isEmpty() && maxPrice.equals("0") && room.getBooked().equals("no")){

                        roomList.add(room); //Añadimos el post a la lista del RecyclerView

                    }else if(room.getCity().toLowerCase().equals(city.toLowerCase()) && price <= Double.parseDouble(maxPrice) && room.getBooked().equals("no")){

                        roomList.add(room); //Añadimos el post a la lista del RecyclerView

                    }else if(city.isEmpty()){

                        if(price <= Double.parseDouble(maxPrice) && room.getBooked().equals("no")){

                            roomList.add(room); //Añadimos el post a la lista del RecyclerView

                        }

                    }else if(maxPrice.equals("0")){

                        if(room.getCity().toLowerCase().equals(city.toLowerCase()) && room.getBooked().equals("no")){

                            roomList.add(room); //Añadimos el post a la lista del RecyclerView

                        }

                    }

                }
                rAdapter.notifyDataSetChanged(); //Notificamos al RecyclerView cada vez que se añada un post a la lista.

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    /**
     *
     * Método encargado de publicar la habitación con los datos pasados por parámetro. Comprobamos
     * que se ha añadido correctamente, en caso afirmativo mostramos un mensaje informando sobre ello.
     *
     * @param title
     * @param city
     * @param address
     * @param description
     * @param price
     * @param publisherId
     * @param postImage
     * @param maxDays
     */
    private void postRoom(String title, String city, String address, String description, String price, String publisherId, String postImage, String maxDays){

        //Usamos un HashMap para almacenar toda la información referente al post
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

        /**
         * Bloque de código encargado de subir el post con el HashMap previo. En caso de éxito, mostramos
         * un mensaje informativo.
         */
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

    /**
     *
     * Método encargado de generar un nombre para una imagen en base a los datos del post.
     *
     * @return
     */
    private String imgNameGenerator() {

        return (postTitle.getText().toString() + postCity.getText().toString());

    }

    /**
     * Método encargado de lanzar el intent encargado de seleccionar la imagen de la galería para los post.
     */
    private void subirFoto(){

        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        mObtenerImg.launch(i);

    }

    /**
     * Bloque de código encargado de mostrar el intent de selección de imagen de la galería. En caso
     * de obtener una imagen de vuelta, conseguimos la uri de la imagen y la asignamos al ImageView del post.
     */
    private ActivityResultLauncher<Intent> mObtenerImg = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == Activity.RESULT_OK){ //Caso de resultado correcto.

                        Intent data = result.getData();
                        assert data != null;
                        uri = data.getData();
                        roomImagePicker.setImageURI(uri); //Asignamos la imagen al ImageView del post.

                    }else { //Caso de no obtener imagen de vuelta en el intent.

                        Toast.makeText(MainActivity.this, "Accion cancelada por el usuario", Toast.LENGTH_SHORT).show();

                    }

                }

            }

    );

}