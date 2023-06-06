package com.example.housemate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ktx.Firebase;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    Button btn_register, btn_login;
    EditText name, email, password;
    TextView prueba;
    FirebaseFirestore mFirestore;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.correoLogin);
        password = findViewById(R.id.contrasenaLogin);
        btn_login = findViewById(R.id.loginButton);
        btn_register = findViewById(R.id.registerButton);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String emailUser = email.getText().toString().trim();
                String passUser = password.getText().toString().trim();

                if (emailUser.isEmpty() || passUser.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Complete los datos", Toast.LENGTH_SHORT).show();
                } else {
                    loginUser(emailUser, passUser);
                }
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));

            }
        });

    }

    private void loginUser(String emailUser, String passUser) {

        mAuth.signInWithEmailAndPassword(emailUser, passUser).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    FirebaseUser user = mAuth.getCurrentUser();

                    if(user.isEmailVerified()){

                        finish();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));

                    } else {

                        Toast.makeText(LoginActivity.this, "Debes verificar el correo electrónico", Toast.LENGTH_SHORT).show();

                    }

                } else {

                    Toast.makeText(LoginActivity.this, "Credenciales erróneas.", Toast.LENGTH_SHORT).show();

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, "Error al iniciar.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}