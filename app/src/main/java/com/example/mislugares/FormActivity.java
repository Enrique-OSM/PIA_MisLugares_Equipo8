package com.example.mislugares;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

//firebse imports


public class FormActivity extends AppCompatActivity {
    private String title;
    private EditText nombre;
    Spinner tipoDeLugar;

    private String tipoElecto;
    private EditText direccion;
    private EditText telefono;
    private EditText url;
    private EditText comentario;

    private Button guardarBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edicion_lugar);
        setup();
    }

    private void setup() {
        nombre = findViewById(R.id.nombre);

        tipoDeLugar = (Spinner) findViewById(R.id.tipo);
        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(this, R.array.tipos_de_lugares, android.R.layout.simple_spinner_item);

        tipoDeLugar.setAdapter(adapter);

        tipoDeLugar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tipoElecto=parent.getItemAtPosition(position).toString();
                Toast.makeText(parent.getContext(), "Selecion: " + parent.getItemAtPosition(position).toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        direccion = findViewById(R.id.direccion);
        telefono = findViewById(R.id.telefono);
        url = findViewById(R.id.url);
        comentario = findViewById(R.id.comentario);
        guardarBtn = findViewById(R.id.guardar);

        ConfigurarBotones();
    }

    private void ConfigurarBotones() {
        guardarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guardarLugar();
            }
        });
    }

    private void guardarLugar() {
        if (nombre.getText().toString().isEmpty() ||
                direccion.getText().toString().isEmpty() ||
                telefono.getText().toString().isEmpty() ||
                url.getText().toString().isEmpty() ||
                tipoElecto.isEmpty() ||
                comentario.getText().toString().isEmpty()){
            Toast.makeText(FormActivity.this, "Por favor, ingrese todos los campos", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(FormActivity.this, "Lugar guardado", Toast.LENGTH_SHORT).show();
        }

    }
}