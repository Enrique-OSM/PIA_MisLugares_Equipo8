package com.example.mislugares;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FormActivity extends AppCompatActivity {
    private String lugarId;
    private FirebaseFirestore db;
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
        lugarId = getIntent().getStringExtra("lugarId");

        db = FirebaseFirestore.getInstance();

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

        if (lugarId != null && !lugarId.isEmpty()) {
            setLastData();
        }

        ConfigurarBotones();
    }

    private void setLastData() {
        db.collection("lugares").document(lugarId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Asignamos valores del documento a los TextView
                        nombre.setText(documentSnapshot.getString("nombre"));
                        direccion.setText(documentSnapshot.getString("direccion"));
                        telefono.setText(documentSnapshot.getString("telefono"));
                        url.setText(documentSnapshot.getString("url"));
                        comentario.setText(documentSnapshot.getString("comentario"));
                    }
                });
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
        FirebaseUser usuarioEmail = FirebaseAuth.getInstance().getCurrentUser();
        String nombreStr = nombre.getText().toString();
        String direccionStr = direccion.getText().toString();
        String telefonoStr = telefono.getText().toString();
        String urlStr = url.getText().toString();
        String comentarioStr = comentario.getText().toString();

        if (nombreStr.isEmpty() || direccionStr.isEmpty() || telefonoStr.isEmpty() ||
                urlStr.isEmpty() || tipoElecto.isEmpty() || comentarioStr.isEmpty()) {
            Toast.makeText(FormActivity.this, "Por favor, ingrese todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Map<String, Object> lugar = new HashMap<>();
        lugar.put("usuarioEmail", usuarioEmail.getEmail());
        lugar.put("nombre", nombreStr);
        lugar.put("direccion", direccionStr);
        lugar.put("telefono", telefonoStr);
        lugar.put("url", urlStr);
        lugar.put("tipo", tipoElecto);
        lugar.put("comentario", comentarioStr);
        lugar.put("fecha", FieldValue.serverTimestamp());

        if (lugarId != null) {
            // Editar documento existente
            db.collection("lugares").document(lugarId)
                    .update(lugar)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(FormActivity.this, "Lugar actualizado correctamente", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(FormActivity.this, ShowActivity.class);
                        intent.putExtra("lugarId", lugarId);
                        startActivity(intent);
                        finish();
                    });
        } else {
            // Crear nuevo documento
            lugar.put("imagen", "#");
            db.collection("lugares")
                    .add(lugar)
                    .addOnSuccessListener(documentReference -> {
                        String nuevoLugarId = documentReference.getId();
                        Intent intent = new Intent(FormActivity.this, ShowActivity.class);
                        intent.putExtra("lugarId", nuevoLugarId);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(FormActivity.this, "Error al guardar lugar", Toast.LENGTH_SHORT).show();
                    });

        }

    }
}