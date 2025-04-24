package com.example.mislugares;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;


import com.google.firebase.analytics.FirebaseAnalytics;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edicion_lugar);

        // Inicializar Firebase Analytics
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Enviar un evento de prueba
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.METHOD, "manual_test");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
    }
}