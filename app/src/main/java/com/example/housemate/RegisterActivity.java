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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import io.grpc.Context;


public class RegisterActivity extends AppCompatActivity{

    private Button btn_register;
    private Button selectorImg;

    private Uri uri = null;

    private EditText name, email, password;
    private ImageView imagenUser;
    private String userImgName;

    private FirebaseFirestore mFirestore;
    private DatabaseReference dbReference;
    private FirebaseAuth mAuth;
    private StorageReference sReference;
    private String storage_path = "userImgs/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().hide();

        //mFirestore = FirebaseFirestore.getInstance();
        dbReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        sReference = FirebaseStorage.getInstance().getReference();

        name = findViewById(R.id.nombre);
        email = findViewById(R.id.correo);
        password = findViewById(R.id.contrasena);
        btn_register = findViewById(R.id.btn_registro);
        selectorImg = findViewById(R.id.selectorImg);
        imagenUser = findViewById(R.id.imagenUser);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String nameUser = name.getText().toString().trim();
                String emailUser = email.getText().toString().trim();
                String passUser = password.getText().toString().trim();

                if (nameUser.isEmpty() || emailUser.isEmpty() || passUser.isEmpty()) {

                    Toast.makeText(RegisterActivity.this, "Complete los datos", Toast.LENGTH_SHORT).show();

                }else if (uri == null){

                    Toast.makeText(RegisterActivity.this, "Debes elegir una imagen de usuario", Toast.LENGTH_SHORT).show();

                }else{

                    if(passUser.length() >= 6 & passUser.length() <= 12){

                        userImgName = imgNameGenerator();

                        sReference = FirebaseStorage.getInstance().getReference("images/" + userImgName);
                        sReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                registerUser(nameUser, emailUser, passUser);

                            }

                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(RegisterActivity.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();

                            }

                        });

                    }

                }

            }

        });

        selectorImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                subirFoto();

            }

        });

    }

    private String imgNameGenerator(){

        return (name.getText().toString() + email.getText().toString());

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
                        imagenUser.setImageURI(uri);

                    }else {

                        Toast.makeText(RegisterActivity.this, "Accion cancelada por el usuario", Toast.LENGTH_SHORT).show();

                    }

                }

            }

    );

    private void registerUser(String nameUser, String emailUser, String passUser) {

        mAuth.createUserWithEmailAndPassword(emailUser, passUser).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    String id = mAuth.getCurrentUser().getUid();
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", nameUser);
                    map.put("email", emailUser);
                    map.put("password", passUser);

                    dbReference.child("Users").child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task2) {
                            if(task2.isSuccessful()){

                                FirebaseUser user = mAuth.getCurrentUser();

                                user.sendEmailVerification();

                                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(i);

                            } else {

                                Toast.makeText(RegisterActivity.this, "Error al crear la colección.", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

                }else {

                    Toast.makeText(RegisterActivity.this, "Error al registrar el user.", Toast.LENGTH_SHORT).show();

                }

            }

        });

    }

}
