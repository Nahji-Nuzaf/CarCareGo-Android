package com.example.carcarego.activity;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.carcarego.R;
import com.example.carcarego.databinding.ActivityDirectionsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DirectionsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng destination;
    private String stationName;

    private ActivityDirectionsBinding binding; // This matches your activity_directions.xml

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDirectionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        double lat = getIntent().getDoubleExtra("dest_lat", 0);
        double lng = getIntent().getDoubleExtra("dest_lng", 0);
        stationName = getIntent().getStringExtra("station_name");
        destination = new LatLng(lat, lng);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.directionsMap);
        mapFragment.getMapAsync(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient =
                com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(this);

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                mMap.addMarker(new MarkerOptions()
                        .position(userLocation)
                        .title("My Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                mMap.addMarker(new MarkerOptions()
                        .position(destination)
                        .title(stationName));

                fetchRoute(userLocation, destination);

                LatLngBounds bounds = new LatLngBounds.Builder()
                        .include(userLocation)
                        .include(destination)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));

            } else {
                LatLng fallback = new LatLng(6.8511, 79.8640);
                fetchRoute(fallback, destination);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fallback, 15));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onMapReady(mMap);
        }
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0; result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            poly.add(new LatLng((double) lat / 1E5, (double) lng / 1E5));
        }
        return poly;
    }

    private void fetchRoute(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String key = "key=" + getString(R.string.google_maps_key); // Use your API Key here
        String url = "https://maps.googleapis.com/maps/api/directions/json?"
                + str_origin + "&" + str_dest + "&" + key;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray routes = response.getJSONArray("routes");
                        JSONObject route = routes.getJSONObject(0);
                        JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                        String encodedPoints = overviewPolyline.getString("points");

                        List<LatLng> decodedPath = decodePolyline(encodedPoints);
                        drawPolyline(decodedPath);

                        JSONArray legs = route.getJSONArray("legs");
                        JSONObject leg = legs.getJSONObject(0);
                        String distance = leg.getJSONObject("distance").getString("text");
                        String duration = leg.getJSONObject("duration").getString("text");

                        binding.tvDistanceTime.setText(distance + " • " + duration);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Routing failed", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void drawPolyline(List<LatLng> points) {
        PolylineOptions options = new PolylineOptions()
                .addAll(points)
                .width(15)
                .color(Color.parseColor("#3B82F6")) // Use your Electric Blue
                .geodesic(true);

        mMap.addPolyline(options);
    }
}