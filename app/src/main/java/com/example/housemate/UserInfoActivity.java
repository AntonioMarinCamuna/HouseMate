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

import com.bumptech.glide.Glide;
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

    private FirebaseAuth mAuth;
    private StorageReference sReference;
    private DatabaseReference dbReference;

    private TextView emailUser, userName, userUsername;
    private ImageView userAvatar;
    private String user;

    private Button cerrarSesionBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        sReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        userName = findViewById(R.id.userName);
        emailUser = findViewById(R.id.userMail);
        userAvatar = findViewById(R.id.userAvatar);
        userUsername = findViewById(R.id.userUsername);

        cerrarSesionBtn = findViewById(R.id.logOut);

        user = mAuth.getCurrentUser().getUid();

        readData();

        cerrarSesionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
                startActivity(new Intent(UserInfoActivity.this, LoginActivity.class));

            }
        });
    }

    //Leer del realtime database info relacionada a un mensaje en específico.

//    public void readMessages(){
//
//        dbReference = FirebaseDatabase.getInstance().getReference("Posts");
//        dbReference.orderByChild("id").equalTo(user).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                for(DataSnapshot d : snapshot.getChildren()){
//
//                    Post p = d.getValue(Post.class);
//                    textView6.setText(p.getId());
//
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//    }

    public void readData(){

        dbReference = FirebaseDatabase.getInstance().getReference("Users");
        dbReference.child(user).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if(task.isSuccessful()){

                    if(task.getResult().exists()){

                        DataSnapshot dSnapshot = task.getResult();

                        emailUser.setText(String.valueOf(dSnapshot.child("email").getValue()));
                        userName.setText(String.valueOf(dSnapshot.child("name").getValue()));
                        userUsername.setText(String.valueOf(dSnapshot.child("username").getValue()));

                        Glide.with(userAvatar.getContext()).load(dSnapshot.child("img_name")).into(userAvatar);

//                        sReference = FirebaseStorage.getInstance().getReference("images/" + dSnapshot.child("img_name").getValue().toString());
//                        try {
//
//                            File localFile = File.createTempFile("tempfile", ".jpg");
//                            sReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
//                                @Override
//                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//
//                                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
//                                    userAvatar.setImageBitmap(bitmap);
//
//                                }
//                            });
//
//                        } catch (IOException e){
//
//                            e.printStackTrace();
//
//                        }


                    }

                }

            }
        });

    }

}