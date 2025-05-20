package com.example.mislugares;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class ListaLugaresActivity extends AppCompatActivity {
    private ImageView toMap;
    private RecyclerView recyclerView;
    private LugarAdapter lugarAdapter;
    private boolean notificacionesPreferencia;
    private static final String CHANNEL_ID = "proximity_notification_channel";
    // El ID de notificación lo generaremos dinámicamente para cada lugar.
    private static final double UN_KILOMETRO_EN_METROS = 1000.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_lugares);

        createNotificationChannel();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1001
            );
        }

        toMap = findViewById(R.id.tomapview);
        recyclerView = findViewById(R.id.recyclerLugares);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        lugarAdapter = new LugarAdapter(new ArrayList<>());
        recyclerView.setAdapter(lugarAdapter);

        String orden = sharedPreferences.getString("orden", "0"); // default es "0"
        String maximoString = sharedPreferences.getString("maximo", "4");
        notificacionesPreferencia = sharedPreferences.getBoolean("notificaciones", false);

        Log.d("ListaLugaresActivity", "Orden: " + orden + ", Máximo: " + maximoString);
        try {
            int maximo = Integer.parseInt(maximoString);
            switch (orden) {
                case "0":
                    cargarLugaresOrdenadosPorFecha(maximo);
                    break;
                case "1":
                    cargarLugaresOrdenadosPorValoracion(maximo);
                    break;
                case "2":
                    Log.d("ListaLugaresActivity", "funcion lugares ordenados por distancia...");
                    cargarLugaresOrdenadosPorDistancia(maximo); // Esta requiere ubicación
                    break;
                default:
                    cargarLugaresDesdeFirestore(maximo);
            }
        } catch (NumberFormatException e) {
            cargarLugaresDesdeFirestore(5);
        }
        ConfigurarBotones();
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notificaciones de Proximidad";
            String description = "Notificaciones cuando estás cerca de un lugar de interés.";
            // IMPORTANCE_HIGH hará que la notificación sea más visible (e.g., head-up notification)
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void cargarLugaresOrdenadosPorValoracion(int limite) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference lugaresRef = db.collection("lugares");

        lugaresRef.orderBy("valoracionPromedio", Query.Direction.DESCENDING) // Ordena por el campo "fecha" de forma descendente (más reciente primero)
                .limit(limite)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Lugar> lista = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getId();
                        String nombre = doc.getString("nombre");
                        String direccion = doc.getString("direccion");
                        String tipo = doc.getString("tipo");
                        Number val = doc.getDouble("valoracionPromedio");
                        float valoracion = val != null ? val.floatValue() : 0.0f;
                        lista.add(new Lugar(id, nombre, direccion, tipo, valoracion, 0.0));
                    }
                    lugarAdapter.actualizarLista(lista);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error al obtener lugares", e);
                    Toast.makeText(this, "Error al cargar lugares", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1001) { // Código para permiso de ubicación
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso de ubicación concedido, carga los lugares por distancia
                cargarLugaresOrdenadosPorDistancia(10); // Llama a la función que inicia la carga
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado. No se pueden ordenar lugares por distancia.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 1002) { // Código para permiso de notificaciones (nuevo)
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de notificaciones concedido.", Toast.LENGTH_SHORT).show();
                // Opcional: Podrías volver a llamar a cargarLugaresOrdenadosPorDistancia() aquí
                // si quieres que se reintente la notificación inmediatamente después de conceder el permiso.
            } else {
                Toast.makeText(this, "Permiso de notificaciones denegado. No se enviarán alertas de proximidad.", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void cargarLugaresOrdenadosPorDistancia(int maximo) {
        Log.d("ListaLugaresActivity", "Cargando lugares ordenados por distancia...");
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.e("ListaLugaresActivity", "Permiso de ubicación denegado");
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            return;
        }
        // --- Manejo del permiso POST_NOTIFICATIONS para Android 13+ ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // TIRAMISU es API 33
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Si el permiso de notificaciones no está concedido, lo solicitamos
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1002); // Usa un requestCode diferente para este permiso
                // Puedes optar por no continuar si el permiso no está dado para notificaciones, o mostrar un Toast
                Toast.makeText(this, "Se necesita permiso de notificaciones para alertarte de lugares cercanos.", Toast.LENGTH_LONG).show();
                return; // Salir si el permiso no está dado
            }
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    Log.d("ListaLugaresActivity", "Ubicación obtenida: " + location);
                    if (location == null) {
                        Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double latUser = location.getLatitude();
                    double lonUser = location.getLongitude();

                    Log.d("ListaLugaresActivity", "Latitud: " + latUser + ", Longitud: " + lonUser);

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("lugares").get().addOnSuccessListener(snapshot -> {
                        List<Lugar> lugaresByD = new ArrayList<>();

                        for (QueryDocumentSnapshot doc : snapshot) {
                            Double lat = doc.getDouble("latitud");
                            Double lon = doc.getDouble("longitud");

                            if (lat != null && lon != null) {
                                String id = doc.getId();
                                String nombre = doc.getString("nombre");
                                String direccion = doc.getString("direccion");
                                String tipo = doc.getString("tipo");
                                Number val = doc.getDouble("valoracionPromedio");
                                float valoracion = val != null ? val.floatValue() : 0.0f;
                                double distancia = calcularDistancia(latUser, lonUser, lat, lon);
                                if (distancia == 0) {
                                    distancia = 0.0000001;
                                }
                                if (notificacionesPreferencia) {
                                    if (distancia < UN_KILOMETRO_EN_METROS) {
                                        Log.d("ListaLugaresActivity", "Lugar cerca: " + nombre + " a " + distancia + " metros.");
                                        sendProximityNotification(nombre, distancia);
                                    }
                                }

                                lugaresByD.add(new Lugar(id, nombre, direccion, tipo, valoracion, distancia));
                            }
                        }

                        // Ordenar por distancia
                        lugaresByD.sort((a, b) -> Double.compare(a.getDistancia(), b.getDistancia()));

                        // Limitar
                        List<Lugar> listaFinal = new ArrayList<>();
                        for (int i = 0; i < Math.min(maximo, lugaresByD.size()); i++) {
                            listaFinal.add(lugaresByD.get(i));
                        }

                        lugarAdapter.actualizarLista(listaFinal);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("ListaLugaresActivity", "Error al obtener ubicación", e);
                    Toast.makeText(this, "Error al obtener ubicación", Toast.LENGTH_SHORT).show();
                });
    }

    private double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0]; // distancia en metros
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

        lugaresRef.orderBy("fecha", Query.Direction.DESCENDING) // Ordena por el campo "fecha" de forma descendente (más reciente primero)
                .limit(limite)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Lugar> lista = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getId();
                        String nombre = doc.getString("nombre");
                        String direccion = doc.getString("direccion");
                        String tipo = doc.getString("tipo");
                        Number val = doc.getDouble("valoracionPromedio");
                        float valoracion = val != null ? val.floatValue() : 0.0f;
                        lista.add(new Lugar(id, nombre, direccion, tipo, valoracion, 0.0));
                    }
                    lugarAdapter.actualizarLista(lista);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error al obtener lugares", e);
                    Toast.makeText(this, "Error al cargar lugares", Toast.LENGTH_SHORT).show();
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
                        Number val = doc.getDouble("valoracionPromedio");
                        float valoracion = val != null ? val.floatValue() : 0.0f;
                        lista.add(new Lugar(id, nombre, direccion, tipo, valoracion, 0.0));
                    }
                    lugarAdapter.actualizarLista(lista);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error al obtener lugares", e);
                    Toast.makeText(this, "Error al cargar lugares", Toast.LENGTH_SHORT).show();
                });
    }
    private void sendProximityNotification(String nombreLugar, double distanciaEnMetros) {
        String mensaje = String.format("¡Estás cerca de %s! A %.0f metros de distancia.", nombreLugar, distanciaEnMetros);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_locationicon) // Puedes cambiar este icono por uno propio (e.g., R.drawable.ic_map_marker)
                .setContentTitle("Lugar Cercano Detectado")
                .setContentText(mensaje)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mensaje)) // Muestra el mensaje completo si es largo
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Alta prioridad para mayor visibilidad
                .setAutoCancel(true); // La notificación se cierra al tocarla

        // Opcional: Si quieres que al tocar la notificación abra tu MapsActivity
        Intent intent = new Intent(this, MapsActivity.class); // Cambia a MapsActivity o la actividad que quieras abrir
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Usa un ID de notificación único para cada lugar (basado en el hashCode del nombre)
        // Esto permite que se muestren múltiples notificaciones si el usuario está cerca de varios lugares.
        notificationManager.notify(nombreLugar.hashCode(), builder.build());
    }
}