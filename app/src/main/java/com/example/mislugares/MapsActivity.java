package com.example.mislugares;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.mislugares.databinding.ActivityMapsBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Coordenadas de Monterrey, México
        LatLng monterrey = new LatLng(25.6866, -100.3161);

        // Añadir un marcador en Monterrey
        mMap.addMarker(new MarkerOptions().position(monterrey).title("Monterrey, México"));

        // Mover la cámara a Monterrey con un zoom adecuado
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(monterrey, 12));

        // Habilitar gestos para mover el mapa si aún no están habilitados (opcional, usualmente ya están por defecto)
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("lugares")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String tipo = document.getString("tipo");
                        String direccion = document.getString("direccion");

                        // Si ya tienes lat y lng, sáltate el geocoding
                        double lat = document.getDouble("lat");
                        double lng = document.getDouble("lng");

                        LatLng ubicacion = new LatLng(lat, lng);

                        int iconoResId = obtenerIconoPorTipo(tipo);

                        mMap.addMarker(new MarkerOptions()
                                .position(ubicacion)
                                .title(tipo)
                                .icon(BitmapDescriptorFactory.fromResource(iconoResId)));
                    }
                });
    }
    private int obtenerIconoPorTipo(String tipo) {
        switch (tipo) {
            case "Restaurante":
                return R.drawable.tipo_icon_restaurant;
            case "Bar":
                return R.drawable.tipo_icon_bar;
            case "Cafe":
                return R.drawable.tipo_icon_cup;
            case "Espectaculo":
                return R.drawable.tipo_icon_espectaculo;
            case "Hotel":
                return R.drawable.tipo_icon_hotel;
            case "Compras":
                return R.drawable.tipo_icon_compras;
            case "Educacion":
                return R.drawable.tipo_icon_educacion;
            case "Deporte":
                return R.drawable.tipo_icon_deporte;
            case "Naturaleza":
                return R.drawable.tipo_icon_naturaleza;
            case "Gasolineria":
                return R.drawable.tipo_icon_gas;
            default:
                return R.drawable.tipo_icon_hotel;
        }
    }

}