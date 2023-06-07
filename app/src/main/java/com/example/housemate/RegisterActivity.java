package com.example.housemate;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

//import id.zelory.compressor.Compressor;
import de.hdodenhof.circleimageview.CircleImageView;
import io.grpc.Context;

public class RegisterActivity extends AppCompatActivity{

    //Elementos del RegisterActivity
    private Button btn_register;
    private Uri uri = null;

    private EditText name, username, email, password;
    private String userImgName;
    private DatabaseReference dbReference;
    private FirebaseAuth mAuth;
    private StorageReference sReference;
    private CircleImageView imagenUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Asignación de los elementos del activity a las variables previamente declaradas.
        dbReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        sReference = FirebaseStorage.getInstance().getReference();

        name = findViewById(R.id.userName);
        username = findViewById(R.id.userUsername);
        email = findViewById(R.id.userMail);
        password = findViewById(R.id.userPassword);
        btn_register = findViewById(R.id.btn_registro);
        imagenUser = findViewById(R.id.imgPicker);

        /**
         * Botón encargado de registrar un usuario en la base de datos y de subir la imagen seleccionada
         * siempre que los datos introducidos sean correctos o no sean nulos.
         */
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Almacenamos los datos introducidos en variables locales.
                String nameUser = name.getText().toString().trim();
                String usernameUser = username.getText().toString().trim();
                String emailUser = email.getText().toString().trim();
                String passUser = password.getText().toString().trim();

                //Comprobamos que los campos de texto no están vacíos.
                if (nameUser.isEmpty() || usernameUser.isEmpty() || emailUser.isEmpty() || passUser.isEmpty()) {

                    Toast.makeText(RegisterActivity.this, "Complete los datos", Toast.LENGTH_SHORT).show();

                }else if (uri == null){ //Comprobamos que se ha seleccionado una imagen de usuario.

                    Toast.makeText(RegisterActivity.this, "Debes elegir una imagen de usuario", Toast.LENGTH_SHORT).show();

                }else{

                    if(passUser.length() >= 6 & passUser.length() <= 12){ //Comprobamos que el tamaño de la contraseña es válido.

                        /**
                         * Bloque de código encargado de obtener los usuarios ya registrados en la BD
                         * para comprobar que el usuario introducido no existe ya.
                         */
                        dbReference.child("Users").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if(!checkUsername(usernameUser, snapshot)){ //Llamamos al método encargado de comprobar que el usuario no existe.

                                    userImgName = imgNameGenerator(); //Llamamos al método encargado de generar un nombre para la imagen subida

                                    /**
                                     * Bloque de código encargado de subir la imagen seleccionada por el usuario
                                     * al Storage de Firebase a la colección "images". En caso de que la imagen
                                     * se suba, se continua con el registro.
                                     */
                                    sReference = FirebaseStorage.getInstance().getReference("images/" + userImgName);
                                    sReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                            /**
                                             * Bloque de código encargado de obtener la URL de la imagen
                                             * subida al Storage para poder luego ponerla en el perfil del usuario.
                                             */
                                            sReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {

                                                    userImgName = uri.toString();
                                                    registerUser(nameUser, usernameUser, emailUser, passUser); //Llamamos al método encargado de registrar al usuario.

                                                }
                                            });



                                        }

                                    }).addOnFailureListener(new OnFailureListener() { //Caso en el que falle la subida de la imagen.
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            Toast.makeText(RegisterActivity.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();

                                        }

                                    });

                                }else{ //Caso en el que el usuario ya existe.

                                    Toast.makeText(RegisterActivity.this, "Ese nombre de usuario ya está en uso.", Toast.LENGTH_SHORT).show();

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    } else{ //Caso en el que la contraseña no sea válida.

                        Toast.makeText(RegisterActivity.this, "La contraseña debe ser de al menos 6 caracteres y máximo 12.", Toast.LENGTH_SHORT).show();

                    }

                }

            }

        });

        /**
         * Bloque de código encargado de llamar al método que nos permite seleccionar una imagen de la galería
         * cuando se presiona el botón de la cámara.
         */
        imagenUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                subirFoto(); //Llamada al método encargado de seleccionar imagenes de la galería.

            }

        });

    }

    /**
     *
     * Método encargado de comprobar si en la lista de usuarios obtenida existe un usuario con el nombre
     * introducido.
     *
     * @param username
     * @param dataSnapshot
     * @return
     */
    private boolean checkUsername(String username, DataSnapshot dataSnapshot){

        for (DataSnapshot ds : dataSnapshot.getChildren()){

            User user = ds.getValue(User.class); //Guardamos los datos obtenidos en forma de objeto User.

            if(user.getUsername().equals(username)){ //Si encontramos un usuario con dicho nombre, devolvemos true

                return true;

            }

        }
        return false; //Si no encontramos el usuario, devolvemos false.

    }

    /**
     *
     * Método encargado de generar un nombre para una imagen en base a los datos del post.
     *
     * @return
     */
    private String imgNameGenerator(){

        return (name.getText().toString() + email.getText().toString());

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
                        imagenUser.setImageURI(uri); //Asignamos la imagen al ImageView del post.

                    }else { //Caso de no obtener imagen de vuelta en el intent.

                        Toast.makeText(RegisterActivity.this, "Accion cancelada por el usuario", Toast.LENGTH_SHORT).show();

                    }

                }

            }

    );

    /**
     *
     * Método encargado de registrar un usuario mediante FirebaseAuth con los valores pasados por parámetros.
     *
     * @param nameUser
     * @param usernameUser
     * @param emailUser
     * @param passUser
     */
    private void registerUser(String nameUser, String usernameUser,String emailUser, String passUser) {

        /**
         * Bloque de código encargado de realizar el registro del usuario.
         */
        mAuth.createUserWithEmailAndPassword(emailUser, passUser).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){ //Caso de registro correcto.

                    //Almacenamos la información del registro en un HashMap para crear una colección de usuarios en la BD.
                    String id = mAuth.getCurrentUser().getUid();
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", nameUser);
                    map.put("username", usernameUser);
                    map.put("email", emailUser);
                    map.put("password", passUser);
                    map.put("img_name", userImgName);

                    /**
                     * Bloque de código encargado de crear un usuario nuevo en la colección Users.
                     * En caso de exito se muestra un mensaje de usuario registrado y nos envía al
                     * activity de inicio de sesión.
                     */
                    dbReference.child("Users").child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task2) {
                            if(task2.isSuccessful()){

                                FirebaseUser user = mAuth.getCurrentUser(); //Obtenemos el usuario actual.

                                user.sendEmailVerification(); //Enviamos el correo de confirmación al correo registrado.

                                Toast.makeText(RegisterActivity.this, "Usuario registrado correctamente, se ha enviado un correo electrónico de verificiacón", Toast.LENGTH_SHORT).show();

                                finish(); //Finalizamos este Activity
                                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(i); //Lanzamos el nuevo activity

                            } else { //Error al registrar el usuario.

                                Toast.makeText(RegisterActivity.this, "Error al registrar el usuario, inténtelo de nuevo mas tarde.", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

                }else { //Caso en el que el correo electrónico ya esté en uso.

                    Toast.makeText(RegisterActivity.this, "Ese correo ya está en uso.", Toast.LENGTH_SHORT).show();

                }

            }

        });

    }

}
