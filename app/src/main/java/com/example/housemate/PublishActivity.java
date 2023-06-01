package com.example.housemate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class PublishActivity extends AppCompatActivity {

    EditText t1;
    EditText t2;
    EditText t3;
    EditText t4;

    String nameUser;
    String emailUser;
    String passUser;
    String a;


    Button b1;
    Button b2;

    Button cancelDialog;

    private DatabaseReference dbReference;
    private FirebaseAuth mAuth;
    private StorageReference sReference;
    private String storage_path = "userImgs/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        dbReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        t1 = findViewById(R.id.editTextText);
        t2 = findViewById(R.id.editTextText2);
        t3 = findViewById(R.id.editTextText3);
        t4 = findViewById(R.id.editTextText4);

        b1 = findViewById(R.id.button);
        b2 = findViewById(R.id.button2);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nameUser = t1.getText().toString().trim();
                emailUser = t2.getText().toString().trim();
                passUser = t3.getText().toString().trim();
                a = t4.getText().toString().trim();

                if (t1.equals("") || t2.equals("") || t3.equals("") || a.equals("")) {

                    Toast.makeText(PublishActivity.this, "Complete los datos", Toast.LENGTH_SHORT).show();

                }else {

                    String id = mAuth.getCurrentUser().getUid();
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", id);
                    map.put("email", emailUser);
                    map.put("password", passUser);
                    map.put("a", a);

                    dbReference.child("Posts").child("post").setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task2) {
                            if(task2.isSuccessful()){

                                FirebaseUser user = mAuth.getCurrentUser();

                                user.sendEmailVerification();

                                Intent i = new Intent(PublishActivity.this, LoginActivity.class);
                                startActivity(i);

                            } else {

                                Toast.makeText(PublishActivity.this, "Error al crear la colección.", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

                }

            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }
}