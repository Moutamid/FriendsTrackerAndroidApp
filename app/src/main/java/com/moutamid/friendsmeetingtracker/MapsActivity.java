package com.moutamid.friendsmeetingtracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.ui.IconGenerator;
import com.moutamid.friendsmeetingtracker.Constants.Constants;
import com.moutamid.friendsmeetingtracker.Model.ClusterMarker;
import com.moutamid.friendsmeetingtracker.Model.User;
import com.moutamid.friendsmeetingtracker.databinding.ActivityMapsBinding;

import java.util.ArrayList;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerDragListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private ArrayList<String> userList;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private DatabaseReference db,roomDB;
    private String uId;
    double currentLat, currentLng = 0;
    private static final int REQUEST_LOCATION = 1;
    private ClusterManager<ClusterMarker> mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();
    private LatLngBounds mMapBoundary;
    private String loc = "";
    private String roomId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        userList = getIntent().getStringArrayListExtra("list");
        loc = getIntent().getStringExtra("loc");
        roomId = getIntent().getStringExtra("roomId");
        uId = Constants.auth().getCurrentUser().getUid();
        db = Constants.databaseReference().child("Users");
        roomDB = Constants.databaseReference().child("Rooms");
        checkInternetAndGPSConnection();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            bulidGoogleApiClient();

        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CALL_PHONE}, REQUEST_LOCATION);
            showGPSDialogBox();
        }

    }

    private void showGPSDialogBox() {
        LocationManager enable_gps = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!enable_gps.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder gps = new AlertDialog.Builder(this);
            gps.setMessage("Turn on GPS to find Location").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(MapsActivity.this, "No location updation without GPS", Toast.LENGTH_SHORT).show();
                }
            }).show();
        }
    }


    private void checkInternetAndGPSConnection() {
        ConnectivityManager connect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connect.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (!connect.isActiveNetworkMetered() && !info.isConnected()) {
            AlertDialog.Builder internet = new AlertDialog.Builder(MapsActivity.this);
            internet.setMessage("Turn on Internet to see rider location")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                            startActivity(intent);
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MapsActivity.this, "No route description without Internet", Toast.LENGTH_SHORT).show();
                        }
                    }).show();

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                bulidGoogleApiClient();
                // Toast.makeText(MainScreen.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(MainScreen.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //  bulidGoogleApiClient();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (loc.equals("address")){
            mMap.setOnMapClickListener(this);
            mMap.setOnMarkerDragListener(this);
        }else {
            getMyMarker();
            addMapMarkers();
        }

    }

    private void getMyMarker() {

        if(mClusterManager == null){
            mClusterManager = new ClusterManager<ClusterMarker>(this, mMap);
        }
        if(mClusterManagerRenderer == null){
            mClusterManagerRenderer = new MyClusterManagerRenderer(
                    this,
                    mMap,
                    mClusterManager
            );
            mClusterManager.setRenderer(mClusterManagerRenderer);
        }
        db.child(uId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            User model = snapshot.getValue(User.class);

                            String avatar = model.getImageUrl();

                            ClusterMarker newClusterMarker = new ClusterMarker(
                                    new LatLng(model.getLat(), model.getLng()),
                                    model.getFullname(),
                                    "THIS IS ME",
                                    avatar
                            );
                            mClusterManager.addItem(newClusterMarker);
                            mClusterMarkers.add(newClusterMarker);
                            mClusterManager.cluster();
                            setCameraView(model);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void addMapMarkers(){
         if(mClusterManager == null){
                mClusterManager = new ClusterManager<ClusterMarker>(this, mMap);
            }
            if(mClusterManagerRenderer == null){
                mClusterManagerRenderer = new MyClusterManagerRenderer(
                        this,
                        mMap,
                        mClusterManager
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }
            for(String id: userList){
                db.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        User users = ds.getValue(User.class);
                                        if (users.getId().equals(id)) {
                                            String avatar = users.getImageUrl();

                                            ClusterMarker newClusterMarker = new ClusterMarker(
                                                    new LatLng(users.getLat(), users.getLng()),
                                                    users.getFullname(),
                                                    "snippet",
                                                    avatar
                                            );
                                            mClusterManager.addItem(newClusterMarker);
                                            mClusterMarkers.add(newClusterMarker);

                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }

        mClusterManager.cluster();
        setCameraViews();
    }

    private void setCameraViews() {
        for (String id : userList){
            db.child(id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        User mUserPosition = snapshot.getValue(User.class);

                        // Set a boundary to start
                        double bottomBoundary = mUserPosition.getLat() - .1;
                        double leftBoundary = mUserPosition.getLng() - .1;
                        double topBoundary = mUserPosition.getLat() + .1;
                        double rightBoundary = mUserPosition.getLng() + .1;

                        mMapBoundary = new LatLngBounds(
                                new LatLng(bottomBoundary, leftBoundary),
                                new LatLng(topBoundary, rightBoundary)
                        );

                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void setCameraView(User mUserPosition) {

        // Set a boundary to start
        double bottomBoundary = mUserPosition.getLat() - .1;
        double leftBoundary = mUserPosition.getLng() - .1;
        double topBoundary = mUserPosition.getLat() + .1;
        double rightBoundary = mUserPosition.getLng() + .1;

        mMapBoundary = new LatLngBounds(
                new LatLng(bottomBoundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary)
        );

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
    }


    protected synchronized void bulidGoogleApiClient() {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        client.connect();

    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(100);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        currentLat = location.getLatitude();
        currentLng = location.getLongitude();


        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("lat",currentLat);
        hashMap.put("lng",currentLng);
        db.child(uId).updateChildren(hashMap);

    }


    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .draggable(true));
    }

    @Override
    public void onMarkerDrag(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {

        double lat = marker.getPosition().latitude;
        double lng = marker.getPosition().longitude;

        LatLng latLng = new LatLng(lat, lng);
        //   mMap.addMarker(new MarkerOptions()
        //         .position(latLng)
        //       .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("meeting_lat",lat);
        hashMap.put("meeting_lng",lng);
        roomDB.child(uId).child(roomId).updateChildren(hashMap);
    }

    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {

    }
}