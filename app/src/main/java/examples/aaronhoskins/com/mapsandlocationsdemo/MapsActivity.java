package examples.aaronhoskins.com.mapsandlocationsdemo;

import androidx.fragment.app.FragmentActivity;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, PermissionManager.IPermissionManager {
    private GoogleMap mMap;
    private EditText etLat;
    private EditText etLng;
    private EditText etAddress;
    private PermissionManager permissionManager;
    LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        permissionManager = new PermissionManager(this);
        permissionManager.requestPermission();
        permissionManager.checkPermission();
        bindViews();

    }

    private void bindViews() {
        etLat = findViewById(R.id.etLat);
        etLng = findViewById(R.id.etLng);
        etAddress = findViewById(R.id.etUserInputAddress);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        moveCameraToLatLng(sydney, "Marker in Sydney");
    }

    public void onGotoLatLngClicked(View view) {
        final LatLng latLng = getUserEnteredLatLng();
        moveCameraToLatLng(latLng, "LAT LNG Enter locale");
    }

    private LatLng getUserEnteredLatLng() {
        final double lat = Double.parseDouble(etLat.getText().toString());
        final double lng = Double.parseDouble(etLng.getText().toString());
        return new LatLng(lat, lng);
    }

    public void onGotoAddressClicked(View view) {
        final String addressEntered = etAddress.getText().toString();
        try {
            final LatLng latLng = getLatLngFromAddress(addressEntered);
            final String addressString = getAddressOfLatLng(latLng);
            moveCameraToLatLng(latLng, addressString);
            Log.d("TAG", "onGotoAddressClicked: " + addressString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Geocoding
    private LatLng getLatLngFromAddress(final String address) throws IOException {
        final Geocoder geocoder = new Geocoder(this);
        final Address addressObject = geocoder.getFromLocationName(address, 1).get(0);
        final double lat = addressObject.getLatitude();
        final double lng = addressObject.getLongitude();
        return new LatLng(lat, lng);
    }

    //Reverse Geocoding
    private String getAddressOfLatLng(LatLng latLng) throws IOException {
        final Geocoder geocoder = new Geocoder(this);
        return geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1).get(0).getAddressLine(0);
    }

    //Move and place marker
    private void moveCameraToLatLng(final LatLng latLng, final String title) {
        mMap.addMarker(new MarkerOptions().position(latLng).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.setMinZoomPreference(16.0f);//0 - 20
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }

    @Override
    public void onPermissionRequestResponse(boolean isGranted) {
        if(isGranted) {
            updateLocation();
        } else {
            Toast.makeText(this, "YOU MUST GRANT PERMISSION", Toast.LENGTH_LONG).show();
        }
    }

    //This will return the last recorded location queried by any app in the system
    public void getLastKnowLocation() {
        FusedLocationProviderClient locationProviderClient
                = new FusedLocationProviderClient(this);

        locationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                final double lat = location.getLatitude();
                final double lng = location.getLongitude();
                final LatLng latLng = new LatLng(lat, lng);
                moveCameraToLatLng(latLng, "Last Known Location");
            }
        });
    }

    private void updateLocation() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1);
        locationRequest.setNumUpdates(100);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);

        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = new SettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest,
                new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                final double lat = locationResult.getLocations().get(0).getLatitude();
                final double lng = locationResult.getLocations().get(0).getLongitude();
                final LatLng latLng = new LatLng(lat, lng);
                moveCameraToLatLng(latLng, "Updated Location");
                Log.d("TAG", "onLocationResult: " +lat + " " + lng);
            }
        }, Looper.myLooper());

    }

    public void onGotoLastLocationClicked(View view) {
        getLastKnowLocation();

    }
}
