package com.example.afinal;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private List<Marker> markers;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean markersVisible = true;
    private FloatingActionButton fabShowDistances;
    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        markers = new ArrayList<>();

        FloatingActionButton fabToggleMarkers = findViewById(R.id.fab_toggle_markers);
        fabToggleMarkers.setOnClickListener(view -> toggleMarkers());

        fabShowDistances = findViewById(R.id.fab_show_distances);
        fabShowDistances.setOnClickListener(view -> showDistancesPopup());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void showDistancesPopup() {
        // Inflar el layout para el popup
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_distances, null);
        LinearLayout container = popupView.findViewById(R.id.distances_container);

        // Obtener la ubicación actual
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location myLocation = mMap.getMyLocation();
            if (myLocation != null) {
                // Mostrar las distancias de todos los marcadores
                for (Marker marker : markers) {
                    float[] results = new float[1];
                    Location.distanceBetween(myLocation.getLatitude(), myLocation.getLongitude(),
                            marker.getPosition().latitude, marker.getPosition().longitude, results);
                    float distanceKm = results[0] / 1000;

                    TextView distanceText = new TextView(this);
                    distanceText.setText(String.format("Marcador %d: %.2f km", markers.indexOf(marker) + 1, distanceKm));
                    distanceText.setPadding(20, 10, 20, 10);
                    container.addView(distanceText);
                }
            } else {
                TextView noLocationText = new TextView(this);
                noLocationText.setText("Esperando ubicación actual...");
                noLocationText.setPadding(20, 10, 20, 10);
                container.addView(noLocationText);
            }
        }

        // Crear y mostrar el PopupWindow
        popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        // Mostrar el popup encima del botón
        popupWindow.showAtLocation(fabShowDistances, Gravity.CENTER, 0, 0);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        setupMap();
        requestLocationPermission();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // Eliminar el marcador cuando se hace clic en él
        marker.remove();
        markers.remove(marker);
        Toast.makeText(this, "Marcador eliminado", Toast.LENGTH_SHORT).show();
        return true;
    }

    private void setupMap() {
        if (mMap != null) {
            try {
                mMap.setMyLocationEnabled(true);
                
                // Configurar el listener para agregar marcadores al hacer clic
                mMap.setOnMapClickListener(latLng -> {
                    // Obtener la ubicación actual
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Location myLocation = mMap.getMyLocation();
                        if (myLocation != null) {
                            // Calcular la distancia
                            float[] results = new float[1];
                            Location.distanceBetween(myLocation.getLatitude(), myLocation.getLongitude(),
                                    latLng.latitude, latLng.longitude, results);
                            
                            // Convertir a kilómetros y redondear a dos decimales
                            float distanceKm = results[0] / 1000;
                            String distance = String.format("%.2f km", distanceKm);
                            
                            // Agregar marcador con la distancia
                            Marker marker = mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title("Distancia")
                                    .snippet(distance));
                            markers.add(marker);
                        } else {
                            Toast.makeText(this, "Esperando ubicación actual...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private void toggleMarkers() {
        markersVisible = !markersVisible;
        for (Marker marker : markers) {
            marker.setVisible(markersVisible);
        }
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupMap();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}