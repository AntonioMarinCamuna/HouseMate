package com.example.housemate;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class UserInfoActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore mFirestore;

    TextView emailUser;
    TextView fechaNacimientoUser;

    Button cerrarSesionBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        emailUser = findViewById(R.id.correoUsuario);
        fechaNacimientoUser = findViewById(R.id.fechaNacimientoUsuario);

        cerrarSesionBtn = findViewById(R.id.cerrarSesion);

        String user = mAuth.getCurrentUser().getUid();

        DocumentReference docRef = mFirestore.collection("Users").document(user);
        docRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot document, @Nullable FirebaseFirestoreException error) {

                emailUser.setText(document.getString("email"));
                fechaNacimientoUser.setText(document.getString("name"));

            }
        });

        cerrarSesionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
                startActivity(new Intent(UserInfoActivity.this, LoginActivity.class));

            }
        });
    }
}