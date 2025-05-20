package com.example.mislugares;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.os.Build;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class ShowActivity extends AppCompatActivity {
    private static final int PERMISSION_CODE = 101;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher; // Nuevo launcher para la cámara
    private Uri cameraImageUri; // Variable para el Uri de la cámara

    private String lugarId;
    private FirebaseFirestore db;
    private TextView nombreLugarTxt;
    private TextView tipoText;
    private TextView direccionText;
    private TextView phoneText;
    private TextView paginaWebText;
    private TextView comentarioText;
    private TextView fechaText;
    private TextView horaText;
    private TextView volverbtn;
    private TextView editarbtn;
    private RatingBar ratingBar;
    private ImageView camaraIcon;
    private ImageView galeryIcon;
    private ImageView fotoLugar;
    private ImageView typeIcon;
    private LatLng lugarUbicacion;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitiy_vistalugar);
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        subirImagenAFirebase(imageUri);
                    }
                }
        );
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // La imagen ya está guardada en 'cameraImageUri'
                        if (cameraImageUri != null) {
                            subirImagenAFirebase(cameraImageUri);
                        } else {
                            Toast.makeText(this, "Error al capturar la imagen de la cámara", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
        setup();
    }



    private void setup() {
        db = FirebaseFirestore.getInstance();

        // Se Asignan los TextView
        lugarId = getIntent().getStringExtra("lugarId");
        nombreLugarTxt = findViewById(R.id.NombreLugarTxt);
        tipoText = findViewById(R.id.tipoText);
        direccionText = findViewById(R.id.direccionText);
        phoneText = findViewById(R.id.phoneText);
        paginaWebText = findViewById(R.id.paginaWebText);
        comentarioText = findViewById(R.id.comentarioText);
        fechaText = findViewById(R.id.fechaText);
        horaText = findViewById(R.id.horaText);
        ratingBar = findViewById(R.id.ratingBar);
        camaraIcon = findViewById(R.id.camaraicon);
        galeryIcon = findViewById(R.id.galeryicon);
        fotoLugar = findViewById(R.id.imageView);
        typeIcon = findViewById(R.id.tipoIcon);
        volverbtn = findViewById(R.id.tvVolver);
        editarbtn = findViewById(R.id.tvEditar);
        Log.d("LugarID", "llego del recy fuera del if: " + lugarId);
        //Asignamos los valores de la base de datos a los TextView
        // Verificamos que el lugarId no sea nulo
        if (lugarId != null && !lugarId.isEmpty()) {
            Log.d("LugarID", "llego del recy: " + lugarId);
            db.collection("lugares").document(lugarId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Asignamos valores del documento a los TextView
                            nombreLugarTxt.setText(documentSnapshot.getString("nombre"));
                            tipoText.setText(documentSnapshot.getString("tipo"));
                            direccionText.setText(documentSnapshot.getString("direccion"));
                            phoneText.setText(documentSnapshot.getString("telefono"));
                            paginaWebText.setText(documentSnapshot.getString("url"));
                            comentarioText.setText(documentSnapshot.getString("comentario"));

                            actualizarIconoPorTipo(documentSnapshot.getString("tipo"));

                            Timestamp timestamp = documentSnapshot.getTimestamp("fecha");
                            if (timestamp != null) {
                                Date date = timestamp.toDate();

                                // Formato de fecha y hora
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

                                fechaText.setText(dateFormat.format(date));
                                horaText.setText(timeFormat.format(date));

                                if(documentSnapshot.getString("imagen") != "#"){
                                    setImagen(documentSnapshot.getString("imagen"));
                                }
                                lugarUbicacion = new LatLng(documentSnapshot.getDouble("latitud"), documentSnapshot.getDouble("longitud"));

                            } else {
                                fechaText.setText("Sin fecha");
                                horaText.setText("Sin hora");
                            }
                        } else {
                            nombreLugarTxt.setText("Lugar no encontrado");
                        }
                    })
                    .addOnFailureListener(e -> {
                        nombreLugarTxt.setText("Error al obtener datos");
                    });
        }

        ConfigurarElementos();
    }

    private void setImagen(String imagen) {
        Log.d("ImagenURL", "Imagen: " + imagen);
        Glide.with(this)
                .load(imagen)
                .placeholder(R.mipmap.imagedefault)
                .into(fotoLugar);
    }

    private void ConfigurarElementos() {
        camaraIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Pedir permiso de cámara
                if (ContextCompat.checkSelfPermission(ShowActivity.this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    abrirCamara();
                } else {
                    ActivityCompat.requestPermissions(ShowActivity.this, new String[]{android.Manifest.permission.CAMERA}, PERMISSION_CODE);
                }
            }
        });
        galeryIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subirImagenGaleria();
            }
        });
        volverbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float valoracion = ratingBar.getRating();
                guardarValoracionEnFirestore(lugarId, valoracion);
                Intent intent = new Intent(ShowActivity.this, MenuActivity.class);
                startActivity(intent);
                finish();
            }
        });
        editarbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShowActivity.this, FormActivity.class);
                intent.putExtra("lugarId", lugarId);
                startActivity(intent);
                finish();
            }
        });
        direccionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double latitud = lugarUbicacion.latitude;
                double longitud = lugarUbicacion.longitude;
                Intent intent = new Intent(ShowActivity.this, MapsActivity.class);
                intent.putExtra("latitud", latitud);
                intent.putExtra("longitud", longitud);
                startActivity(intent);
                finish();
            }
        });
        phoneText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = phoneText.getText().toString();
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + phoneNumber));
                if (dialIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(dialIntent);
                } else {
                    Toast.makeText(ShowActivity.this, "No se puede realizar la llamada", Toast.LENGTH_SHORT).show();
                }
            }
        });
        paginaWebText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = paginaWebText.getText().toString();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });
    }

    private void guardarValoracionEnFirestore(String lugarId, float valoracion) {
        // Creamos un nuevo documento de valoración o actualizamos uno existente para el usuario actual
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        FirebaseAuth auth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();
        Map<String, Object> data = new HashMap<>();
        data.put("valoracion", valoracion);
        data.put("usuarioId", currentUser.getEmail()); // ¡Importante! Identificar al usuario

        // Puedes tener una subcolección "valoraciones" dentro del documento del lugar
        db.collection("lugares").document(lugarId)
                .collection("valoraciones")
                .document(currentUser.getUid()) // Usamos el ID del usuario como ID del documento de valoración
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ShowActivity.this, "Valoración guardada", Toast.LENGTH_SHORT).show();
                    // Opcional: Actualizar la valoración promedio del lugar
                    actualizarValoracionPromedio(lugarId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ShowActivity.this, "Error al guardar la valoración", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    private void actualizarValoracionPromedio(String lugarId) {
        db.collection("lugares").document(lugarId)
                .collection("valoraciones")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        double sumaValoraciones = 0;
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Number val = doc.getDouble("valoracion");
                            if (val != null) {
                                sumaValoraciones += val.doubleValue();
                            }
                        }
                        double promedio = sumaValoraciones / queryDocumentSnapshots.size();

                        // Actualizamos el campo "valoracionPromedio" en el documento del lugar
                        Map<String, Object> promedioData = new HashMap<>();
                        promedioData.put("valoracionPromedio", promedio);
                        db.collection("lugares").document(lugarId)
                                .update(promedioData)
                                .addOnFailureListener(e -> Log.e("FirestoreError", "Error al actualizar promedio", e));
                    } else {
                        // Si no hay valoraciones, puedes establecer el promedio en 0 o null
                        Map<String, Object> promedioData = new HashMap<>();
                        promedioData.put("valoracionPromedio", 0.0);
                        db.collection("lugares").document(lugarId).update(promedioData);
                    }
                });
    }

    private void abrirCamara() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Asegurarse de que haya una actividad de cámara para manejar el intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Crear el archivo donde la foto debería ir
            File photoFile = null;
            try {
                photoFile = crearArchivoImagen();
            } catch (IOException ex) {
                // Error al crear el archivo
                Toast.makeText(this, "Error al crear archivo para la foto", Toast.LENGTH_SHORT).show();
                Log.e("ShowActivity", "Error creando archivo de imagen: " + ex.getMessage());
            }

            // Continuar solo si el archivo fue creado exitosamente
            if (photoFile != null) {
                cameraImageUri = androidx.core.content.FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                cameraLauncher.launch(takePictureIntent);
            }
        } else {
            Toast.makeText(this, "No se encontró aplicación de cámara", Toast.LENGTH_SHORT).show();
        }
    }

    private File crearArchivoImagen() throws IOException {
        // Crear un nombre de archivo de imagen único
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES); // Guarda en Pictures
        File image = File.createTempFile(
                imageFileName,  /* prefijo */
                ".jpg",         /* sufijo */
                storageDir      /* directorio */
        );
        return image;
    }

    // Sobreescribir onRequestPermissionsResult para manejar el permiso de cámara
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_CODE) {
            // Verificar si todos los permisos fueron concedidos
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                // Determinar qué permiso(s) fueron concedidos y realizar la acción correspondiente
                if (Arrays.asList(permissions).contains(android.Manifest.permission.CAMERA)) {
                    // El permiso de la cámara fue concedido, abrir la cámara
                    abrirCamara();
                } else if (Arrays.asList(permissions).contains(android.Manifest.permission.READ_MEDIA_IMAGES) ||
                        Arrays.asList(permissions).contains(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // El permiso de la galería fue concedido, abrir la galería
                    subirImagenGaleria();
                }
                //Si se otorgan ambos permisos, se ejecutará primero la cámara, ya que está primero en la condición.
            } else {
                // Si algún permiso fue denegado, mostrar un mensaje
                Toast.makeText(this, "Permiso denegado.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void subirImagenGaleria() {
        if (tienePermisos()) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        } else {
            pedirPermisos();
        }
    }
    private boolean tienePermisos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void pedirPermisos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_CODE);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CODE);
        }
    }
    private void subirImagenAFirebase(Uri imageUri) {
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference()
                .child("imagenes_lugares/" + UUID.randomUUID().toString());

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(this, "Imagen subida exitosamente", Toast.LENGTH_SHORT).show();

                    // Si quieres guardar la URL en Firestore después de subir:
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String urlImage = uri.toString();
                        Glide.with(this)
                                .load(urlImage)
                                .into(fotoLugar);
                        if (lugarId != null && !lugarId.isEmpty()) {
                            db.collection("lugares").document(lugarId)
                                    .update("imagen", urlImage)
                                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "URL guardada en Firestore", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar URL", Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void actualizarIconoPorTipo(String tipoLugar) {
        switch (tipoLugar) {
            case "Bar":
                typeIcon.setImageResource(R.drawable.tipo_icon_bar);
                break;
            case "Compras":
                typeIcon.setImageResource(R.drawable.tipo_icon_compras);
                break;
            case "Cafe":
                typeIcon.setImageResource(R.drawable.tipo_icon_cup);
                break;
            case "Deporte":
                typeIcon.setImageResource(R.drawable.tipo_icon_deporte);
                break;
            case "Educacion":
                typeIcon.setImageResource(R.drawable.tipo_icon_educacion);
                break;
            case "Espectaculo":
                typeIcon.setImageResource(R.drawable.tipo_icon_espectaculo);
                break;
            case "Gasolineria":
                typeIcon.setImageResource(R.drawable.tipo_icon_gas);
                break;
            case "Hotel":
                typeIcon.setImageResource(R.drawable.tipo_icon_hotel);
                break;
            case "Naturaleza":
                typeIcon.setImageResource(R.drawable.tipo_icon_naturaleza);
                break;
            case "Restaurante":
                typeIcon.setImageResource(R.drawable.tipo_icon_restaurant);
                break;
            default:
                typeIcon.setImageResource(R.drawable.tipo_icon_restaurant); // o algún ícono por defecto
                break;
        }
    }


}