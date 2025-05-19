package com.example.mislugares;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Locale;
public class LocationUtils {
    public interface LatLngCallback {
        void onCoordinatesFound(LatLng latLng);
    }

    public static void getLatLngFromAddress(Context context, String addressStr, LatLngCallback callback) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                List<Address> addressList = geocoder.getFromLocationName(addressStr, 1);
                if (addressList != null && !addressList.isEmpty()) {
                    Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    callback.onCoordinatesFound(latLng);
                } else {
                    callback.onCoordinatesFound(null);
                }
            } catch (Exception e) {
                Log.e("GeocodingError", "Error al obtener LatLng: " + e.getMessage());
                callback.onCoordinatesFound(null);
            }
        }).start();
    }
}
