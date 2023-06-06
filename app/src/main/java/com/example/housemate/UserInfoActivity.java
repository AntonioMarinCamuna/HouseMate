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

    private Button logOutButton, dataChangeButton;

//    //Elementos del dataChangeDialog
//    private TextView userNameDialog, userPasswordDialog, userUsernameDialog;
//    private ImageView userAvatarDialog;
//    private Button dataChangeButtonDialog;
//    private Uri uri = null;
//    private String userImgName;

    //Elementos Dialog
    private Button cancelButton;
    private ImageView roomImagePicker;
    private TextView postTitle, postCity, postAddress, postDescription, postPrice, bookedBy;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

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

                openPostDialog(room);

            }
        });

        postRecycler.setAdapter(rAdapter);

        loadRoom();

        logOutButton = findViewById(R.id.logOut);
        dataChangeButton = findViewById(R.id.changeUserData);

        user = mAuth.getCurrentUser().getUid();

        readData();

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
                startActivity(new Intent(UserInfoActivity.this, LoginActivity.class));

            }
        });

        mainPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(UserInfoActivity.this, MainActivity.class);
                startActivity(i);

            }
        });

        myBookings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(UserInfoActivity.this, MyBookingsActivity.class);
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

                    if(room.getPublisherId().equals(user)){

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

    private void openPostDialog(Room room){

        dialog = new Dialog(UserInfoActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        View customDialog = inflater.inflate(R.layout.fragment_owned_posts, null);

        postTitle = customDialog.findViewById(R.id.roomTitle);
        postCity = customDialog.findViewById(R.id.roomCity);
        postDescription = customDialog.findViewById(R.id.roomDescription);
        postAddress = customDialog.findViewById(R.id.roomAddress);
        postPrice = customDialog.findViewById(R.id.roomPrice);
        roomImagePicker = customDialog.findViewById(R.id.roomImage);
        bookedBy = customDialog.findViewById(R.id.bookedBy);

        cancelButton = customDialog.findViewById(R.id.bookButton);

        String postId = room.getPostId();

        postTitle.setText(room.getTitle());
        postCity.setText(room.getCity());
        postAddress.setText(room.getAddress());
        postDescription.setText(room.getDescription());

        dbReference.child("Users").child(room.getBookedBy()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                User user = snapshot.getValue(User.class);
                bookedBy.setText(user.getName());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        postPrice.setText(room.getPrice());

        Glide.with(roomImagePicker.getContext()).load(room.getImage()).into(roomImagePicker);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!mAuth.getCurrentUser().getUid().equals(room.getPublisherId())){

                    Toast.makeText(UserInfoActivity.this, "No puedes eliminar una habitacion publicada por otra persona", Toast.LENGTH_SHORT).show();

                }else {

                    if (room.getBooked().equals("yes")){

                        Toast.makeText(UserInfoActivity.this, "No puedes borrar una habitación reservada", Toast.LENGTH_SHORT).show();

                    } else{

                        dbReference.child("Posts").child(postId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                sReference.child("roomsImages").child(room.getTitle()+room.getCity()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                        dialog.cancel();
                                        Toast.makeText(UserInfoActivity.this, "Habitación eliminada correctamente", Toast.LENGTH_SHORT).show();

                                    }
                                });

                            }
                        });

                    }

                }

            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(customDialog);
        dialog.show();

    }

    public void readData(){

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

//        dataChangeButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                openDataChangeDialog();
//
//            }
//        });
//    }

//    private void openDataChangeDialog() {
//
//        dialog = new Dialog(UserInfoActivity.this);
//
//        LayoutInflater inflater = this.getLayoutInflater();
//        View customDialog = inflater.inflate(R.layout.fragment_info_editing, null);
//
//        userNameDialog = customDialog.findViewById(R.id.userNameDialog);
//        userUsernameDialog = customDialog.findViewById(R.id.userUsernameDialog);
//        userAvatarDialog = customDialog.findViewById(R.id.userAvatarDialog);
//
//        userNameDialog.setText(userName.getText());
//        userUsernameDialog.setText(userUsername.getText());
//
//        dbReference.child(user).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DataSnapshot> task) {
//
//                if(task.isSuccessful()){
//
//                    if(task.getResult().exists()){
//
//                        DataSnapshot ds = task.getResult();
//
//                        Glide.with(UserInfoActivity.this).load(String.valueOf(ds.child("img_name").getValue())).into(userAvatarDialog);
//
//                    }
//
//                }
//
//            }
//        });

//        dataChangeButtonDialog = customDialog.findViewById(R.id.dataChangeButton);

//        dataChangeButtonDialog.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                dbReference.child("Users").addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                        for (DataSnapshot ds : snapshot.getChildren()){
//
//                            if(!checkUsername(userUsernameDialog.getText().toString(), ds)){
//
//                                userImgName = imgNameGenerator();
//
//                                sReference = FirebaseStorage.getInstance().getReference("images/" + userImgName);
//                                sReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                                    @Override
//                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//
//                                        sReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                            @Override
//                                            public void onSuccess(Uri uri) {
//
//                                                userImgName = uri.toString();
//
//                                                HashMap<String, Object> map = new HashMap<>();
//                                                map.put("name", userNameDialog.getText().toString());
//                                                map.put("username", userUsernameDialog.getText().toString());
//                                                map.put("img_name", userImgName);
//
//                                                dbReference.child(user).updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
//                                                    @Override
//                                                    public void onComplete(@NonNull Task task) {
//
//                                                        dialog.cancel();
//                                                        Toast.makeText(UserInfoActivity.this, "Información actualizada", Toast.LENGTH_SHORT).show();
//
//                                                    }
//                                                }).addOnFailureListener(new OnFailureListener() {
//                                                    @Override
//                                                    public void onFailure(@NonNull Exception e) {
//
//                                                        Toast.makeText(UserInfoActivity.this, "Error", Toast.LENGTH_SHORT).show();
//
//                                                    }
//                                                });
//
//                                            }
//                                        });
//
//
//
//                                    }
//
//                                }).addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//
//                                        Toast.makeText(UserInfoActivity.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
//
//                                    }
//
//                                });
//
//                            }else{
//
//                                Toast.makeText(UserInfoActivity.this, "Ese nombre de usuario ya está en uso.", Toast.LENGTH_SHORT).show();
//
//                            }
//
//                        }
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//
//            }
//        });
//
//        userAvatarDialog.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                subirFoto();
//
//            }
//        });

//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        dialog.setContentView(customDialog);
//        dialog.show();
//
//    }

//    private String imgNameGenerator(){
//
//        return (userUsernameDialog.getText().toString() + userNameDialog.getText().toString());
//
//    }
//
//    private void subirFoto(){
//
//        Intent i = new Intent(Intent.ACTION_PICK);
//        i.setType("image/*");
//        mObtenerImg.launch(i);
//
//    }
//
//    private ActivityResultLauncher<Intent> mObtenerImg = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            new ActivityResultCallback<ActivityResult>() {
//                @Override
//                public void onActivityResult(ActivityResult result) {
//
//                    if (result.getResultCode() == Activity.RESULT_OK){
//
//                        Intent data = result.getData();
//                        assert data != null;
//                        uri = data.getData();
//                        userAvatarDialog.setImageURI(uri);
//
//                    }else {
//
//                        Toast.makeText(UserInfoActivity.this, "Accion cancelada por el usuario", Toast.LENGTH_SHORT).show();
//
//                    }
//
//                }
//
//            }
//
//    );
//
//    private boolean checkUsername(String username, DataSnapshot dataSnapshot){
//
//        for (DataSnapshot ds : dataSnapshot.getChildren()){
//
//            User user = ds.getValue(User.class);
//
//            if(user.getUsername().equals(username)){
//
//                return true;
//
//            }
//
//        }
//        return false;
//