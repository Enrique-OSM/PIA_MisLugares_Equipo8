package com.example.mislugares;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {
    private String title;
    private Button NuevoLugarButton;
    private Button MostrarLugaresButton;
    private Button PreferenciasButton;
    private Button AcercaDeButton;
    private Button SalirButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        setup();
    }

    private void setup() {
        title = "Menu";
        NuevoLugarButton = findViewById(R.id.btn_nuevoLugar);
        MostrarLugaresButton = findViewById(R.id.btn_mostrar_lugares);
        PreferenciasButton = findViewById(R.id.btn_preferencias);
        AcercaDeButton = findViewById(R.id.btn_acerca_de);
        SalirButton = findViewById(R.id.btn_salir);
        configurarbotones();
    }

    private void configurarbotones() {
        NuevoLugarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FormParaCrearLugar();
            }
        });
        PreferenciasButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MostrarPreferencias();
            }
        });
        AcercaDeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MostrarAcercaDe();
            }
        });
        SalirButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CerrarSesion();
            }
        });
    }

    private void MostrarPreferencias() {
        Intent intent = new Intent(MenuActivity.this, PreferenciasActivity.class);
        startActivity(intent);
    }

    private void MostrarAcercaDe() {
        Intent intent = new Intent(MenuActivity.this, AcercaDeActivity.class);
        startActivity(intent);
    }

    private void FormParaCrearLugar() {
        Intent intent = new Intent(MenuActivity.this, FormActivity.class);
        startActivity(intent);
        finish();
    }

    private void CerrarSesion() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MenuActivity.this, AuthActivity.class);
        startActivity(intent);
        finish();
    }
}