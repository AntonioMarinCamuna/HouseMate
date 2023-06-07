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

    //Elementos utilizados en el activity
    private Button btn_register, btn_login;
    private EditText email, password;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Asignación de los elementos del activity a las variables previamente declaradas.
        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.correoLogin);
        password = findViewById(R.id.contrasenaLogin);
        btn_login = findViewById(R.id.loginButton);
        btn_register = findViewById(R.id.registerButton);


        /**
         * Bloque de código encargado de actuar cuando el botón de login
         * es pulsado, comprobando que ninguno de los valores esté vacío y
         * haciendo una llamada a un método encargado de iniciar la sesión.
         */
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String emailUser = email.getText().toString().trim();
                String passUser = password.getText().toString().trim();

                if (emailUser.isEmpty() || passUser.isEmpty()) { //Comprobamos que la entrada de datos no están vacías
                    Toast.makeText(LoginActivity.this, "Complete los datos", Toast.LENGTH_SHORT).show();
                } else {
                    loginUser(emailUser, passUser); //Llamada al método encargado de iniciar sesión con los datos pasados por parámetro
                }
            }
        });

        /**
         * Bloque de código encargado de actuar cuando el botón de registro se pulsa
         * lanzando el activity RegisterActivity.
         */
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));

            }
        });

    }

    /**
     *
     * Método encargado de hacer el inicio de sesión con los usuarios que llegan por parámetros
     * en caso de inicio de sesión correcto se lanza el MainActivity.
     * En caso de error, aparecerán una serie de mensajes en función del tipo de error.
     *
     * @param emailUser
     * @param passUser
     */
    private void loginUser(String emailUser, String passUser) {

        /**
         * Método de la clase FirebaseAuth encargado de hacer el login con los datos introducidos,
         * lleva un addOnCompleteListener que ejecuta un código u otro en base al resultado del inicio de sesión.
         */
        mAuth.signInWithEmailAndPassword(emailUser, passUser).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) { //Caso de inicio de sesión correcto
                if (task.isSuccessful()) {

                    FirebaseUser user = mAuth.getCurrentUser(); //Con esta linea obtenemos el usuario actual con el que se ha iniciado sesión.

                    if(user.isEmailVerified()){ //Comprobamos si usuario tiene el email verificado.

                        finish();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));

                    } else {

                        //Mensaje informando de la necesida de verificar el correo electrónico.
                        Toast.makeText(LoginActivity.this, "Debes verificar el correo electrónico", Toast.LENGTH_SHORT).show();

                    }

                } else {

                    //Mensaje que indica que los datos introducidos no son válidos.
                    Toast.makeText(LoginActivity.this, "Credenciales erróneas.", Toast.LENGTH_SHORT).show();

                }
            }
        }).addOnFailureListener(new OnFailureListener() { //Caso en el que el inicio de sesión falla.
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, "Error al iniciar.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}