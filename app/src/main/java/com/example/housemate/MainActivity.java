package com.example.housemate;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //Elementos MainActivity
    FloatingActionButton button;

    //Elementos PublishDialog
    private Uri uri = null;
    Button cancelButton, publishButton;
    ImageView roomImagePicker;
    TextView postTitle, postCity, postAddress, postDescription, postPrice;
    String postImgName;
    private FirebaseAuth mAuth;
    private StorageReference sReference;
    private DatabaseReference dbReference;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.postingButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

    }

    private void openDialog() {

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
                String price = postPrice.getText().toString().trim(); //HACER COMPROBACIÓN DE VALORES VÁLIDOS

                if (title.isEmpty() || city.isEmpty() || address.isEmpty() || description.isEmpty() || price.isEmpty()) {

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

                            postRoom(title, city, address, description, price, publisherId);

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

    private void postRoom(String title, String city, String address, String description, String price, String publisherId){

        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("city", city);
        map.put("address", address);
        map.put("description", description);
        map.put("price", price);
        map.put("publisher", publisherId);

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