package com.example.housemate;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
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

import java.io.File;
import java.io.IOException;

public class UserInfoActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    StorageReference sReference;
    DatabaseReference dbReference;

    TextView emailUser;
    TextView fechaNacimientoUser;
    TextView textView6;
    String user;
    ImageView avatarUsuario;

    Button cerrarSesionBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        getSupportActionBar().hide();

        sReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        emailUser = findViewById(R.id.correoUsuario);
        fechaNacimientoUser = findViewById(R.id.fechaNacimientoUsuario);
        avatarUsuario = findViewById(R.id.avatarUsuario);
        textView6 = findViewById(R.id.textView6);

        cerrarSesionBtn = findViewById(R.id.cerrarSesion);

        user = mAuth.getCurrentUser().getUid();

        readData();

        /*DocumentReference docRef = mFirestore.collection("Users").document(user);
        docRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot document, @Nullable FirebaseFirestoreException error) {

                emailUser.setText(document.getString("email"));
                fechaNacimientoUser.setText(document.getString("name"));

            }
        });
        /*/
        cerrarSesionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
                startActivity(new Intent(UserInfoActivity.this, LoginActivity.class));

            }
        });
    }

    //Leer del realtime database info relacionada a un mensaje en específico.

    public void readMessages(){

        dbReference = FirebaseDatabase.getInstance().getReference("Posts");
        dbReference.orderByChild("id").equalTo(user).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot d : snapshot.getChildren()){

                    Post p = d.getValue(Post.class);
                    textView6.setText(p.getId());

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void readData(){

        dbReference = FirebaseDatabase.getInstance().getReference("Users");
        dbReference.child(user).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if(task.isSuccessful()){

                    if(task.getResult().exists()){

                        DataSnapshot dSnapshot = task.getResult();

                        emailUser.setText(String.valueOf(dSnapshot.child("email").getValue()));
                        fechaNacimientoUser.setText(String.valueOf(dSnapshot.child("name").getValue()));

                        sReference = FirebaseStorage.getInstance().getReference("images/" + dSnapshot.child("img_name").getValue().toString());
                        try {

                            File localFile = File.createTempFile("tempfile", ".jpg");
                            sReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                    avatarUsuario.setImageBitmap(bitmap);

                                }
                            });

                        } catch (IOException e){

                            e.printStackTrace();

                        }


                    }

                }

            }
        });

    }

}