package com.example.mislugares;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;



public class ListaLugaresActivity extends AppCompatActivity {
    private ImageView toMap;
    private RecyclerView recyclerView;
    private LugarAdapter lugarAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_lugares);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        toMap = findViewById(R.id.tomapview);
        recyclerView = findViewById(R.id.recyclerLugares);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        lugarAdapter = new LugarAdapter(new ArrayList<>());
        recyclerView.setAdapter(lugarAdapter);

        String orden = sharedPreferences.getString("orden", "0"); // default es "0"
        String maximoString = sharedPreferences.getString("maximo", "4");

        try {
            int maximo = Integer.parseInt(maximoString);
            switch (orden) {
                case "0":
                    cargarLugaresOrdenadosPorFecha(maximo);
                    break;
                case "1":
                    //cargarLugaresOrdenadosPorValoracion(maximo);
                    break;
                case "2":
                    //cargarLugaresOrdenadosPorDistancia(maximo); // Esta requiere ubicación
                    break;
                default:
                    cargarLugaresDesdeFirestore(maximo);
            }
        } catch (NumberFormatException e) {
            cargarLugaresDesdeFirestore(12);
        }
        ConfigurarBotones();
    }

    private void ConfigurarBotones() {
        toMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListaLugaresActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void cargarLugaresOrdenadosPorFecha(int limite) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference lugaresRef = db.collection("lugares");

        lugaresRef.orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limite)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Lugar> lista = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getId();
                        String nombre = doc.getString("nombre");
                        String direccion = doc.getString("direccion");
                        String tipo = doc.getString("tipo");
                        Number val = doc.getDouble("valoracion");
                        float valoracion = val != null ? val.floatValue() : 0.0f;

                        lista.add(new Lugar(id, nombre, direccion, tipo, valoracion));
                    }
                    lugarAdapter.actualizarLista(lista);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error al cargar por fecha", e);
                    Toast.makeText(this, "Error al cargar lugares por fecha", Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarLugaresDesdeFirestore(int limite) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference lugaresRef = db.collection("lugares");

        lugaresRef.limit(limite).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Lugar> lista = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getId();
                        String nombre = doc.getString("nombre");
                        String direccion = doc.getString("direccion");
                        String tipo = doc.getString("tipo");
                        Number val = doc.getDouble("valoracion");
                        float valoracion = val != null ? val.floatValue() : 0.0f;

                        lista.add(new Lugar(id, nombre, direccion, tipo, valoracion));
                    }
                    lugarAdapter.actualizarLista(lista);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error al obtener lugares", e);
                    Toast.makeText(this, "Error al cargar lugares", Toast.LENGTH_SHORT).show();
                });
    }

//    public void setListaDesdeOtraClase(List<Lugar> lugares) {
//        lugarAdapter.actualizarLista(lugares);
//    }
}