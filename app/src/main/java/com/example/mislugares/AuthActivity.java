package com.example.mislugares;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class AuthActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private String title;
    private Button SingUpButton;
    private Button LogInButton;
    private EditText emailEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_auth);
        setup();
    }

    private void setup() {
        title = "Autenticación";

        // Obtén una referencia a los EditText
        emailEditText = findViewById(R.id.editTextTextEmailAddress);
        passwordEditText = findViewById(R.id.editTextTextPassword);

        // Obtén una referencia al botón Registrar
        SingUpButton = findViewById(R.id.SingUpButton);
        LogInButton = findViewById(R.id.LogInButton);

        // Llama a la función para configurar el listener del botón
        ConfiguracionBotones();
    }

    private void ConfiguracionBotones() {
        SingUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                RegistrarUsuario(email, password);
            }
        });
        LogInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                IngresoUsuario(email, password);
            }
        });
    }

    private void RegistrarUsuario(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(AuthActivity.this, "Por favor, ingrese un correo electrónico y una contraseña", Toast.LENGTH_SHORT).show();
        }
        else {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(AuthActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Registro exitoso
                                Toast.makeText(AuthActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                            } else {
                                // Si el registro falla, muestra un mensaje al usuario.
                                Toast.makeText(AuthActivity.this, "Registro fallido: " + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void IngresoUsuario(String email, String password){
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(AuthActivity.this, "Por favor, ingrese un correo electrónico y una contraseña", Toast.LENGTH_SHORT).show();
        }
        else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Inicio de sesión exitoso
                                Toast.makeText(AuthActivity.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(AuthActivity.this, MenuActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // Si el inicio de sesión falla, muestra un mensaje al usuario
                                Toast.makeText(AuthActivity.this, "Inicio de sesión fallido: " + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}