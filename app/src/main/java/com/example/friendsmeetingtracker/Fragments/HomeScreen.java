package com.example.friendsmeetingtracker.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.example.friendsmeetingtracker.Adapters.UserListAdapter;
import com.example.friendsmeetingtracker.Constants.Constants;
import com.example.friendsmeetingtracker.Constants.ItemClickListener;
import com.example.friendsmeetingtracker.MapsActivity;
import com.example.friendsmeetingtracker.Model.Room;
import com.example.friendsmeetingtracker.Model.User;
import com.example.friendsmeetingtracker.R;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeScreen extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private TextView nameTxt,createBtn;
    private CircleImageView profileImg;
    private FirebaseUser user;
    private DatabaseReference db,roomDB;
    private RecyclerView recyclerView;
    private UserListAdapter adapter;
    private ArrayList<User> userArrayList;
    private boolean isCreated = false;
    private ArrayList<String> userId = new ArrayList<>();
    private String description = "";
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    double currentLat, currentLng = 0;
    private static final int REQUEST_LOCATION = 1;
    private Activity mActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_hom_screen,container,false);
        if (isAdded()) {
            nameTxt = root.findViewById(R.id.name);
            profileImg = root.findViewById(R.id.profile);
            recyclerView = root.findViewById(R.id.recyclerView);
            createBtn = root.findViewById(R.id.create);
            user = Constants.auth().getCurrentUser();
            db = Constants.databaseReference().child("Users");
            roomDB = Constants.databaseReference().child("Rooms");
            userArrayList = new ArrayList<>();
            mActivity = getActivity();
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                bulidGoogleApiClient();
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.CALL_PHONE}, REQUEST_LOCATION);
            }
            createBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (createBtn.getText().equals("Create Meeting Room")) {
                        isCreated = true;
                        getUserList();
                        createBtn.setText("Next");
                    } else if (createBtn.getText().equals("Next")) {
                        //isCreated = false;
                        //getUserList();
                        //createBtn.setText("Create Meeting Room");
                        showMeetingRoom();
                    }
                }
            });
            getUserData();
            getUserList();
        }
        return root;
    }



    protected synchronized void bulidGoogleApiClient() {
        client = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        client.connect();

    }


    private void showMeetingRoom() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View add_view = inflater.inflate(R.layout.banned_alert_dialog_screen,null);
        EditText roomNameTxt = add_view.findViewById(R.id.name);
        EditText roomDescTxt = add_view.findViewById(R.id.description);
        AppCompatButton addBtn = add_view.findViewById(R.id.ok);
        AppCompatButton cancelBtn = add_view.findViewById(R.id.cancel);
        builder.setView(add_view);
        AlertDialog alertDialog = builder.create();
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String room = roomNameTxt.getText().toString();
                description = roomDescTxt.getText().toString();
                if (!TextUtils.isEmpty(room)) {
                    saveRoom(room,description);
                    alertDialog.dismiss();
                }else {
                    Toast.makeText(getActivity(), "Enter your room name", Toast.LENGTH_SHORT).show();
                }
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();

    }

    private void saveRoom(String room, String desc) {
        String key = roomDB.push().getKey();
        userId.add(user.getUid());
        Room model = new Room(key,room, desc,userId,0.0,0.0);
        roomDB.child(key).setValue(model);

        Intent intent = new Intent(getActivity(), MapsActivity.class);
        intent.putExtra("loc","address");
        intent.putExtra("roomId",key);
        startActivity(intent);
    }

    private void getUserList() {
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    userArrayList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()){
                        User model = ds.getValue(User.class);
                        if (!model.getId().equals(user.getUid())){
                            userArrayList.add(model);
                        }
                    }
                    adapter = new UserListAdapter(getActivity(),userArrayList,isCreated);
                    recyclerView.setAdapter(adapter);
                    adapter.setItemClickListener(new ItemClickListener() {
                        @Override
                        public void onItemClick(int position, boolean isChecked) {
                            if (isChecked){
                                User user1 = userArrayList.get(position);
                                userId.add(user1.getId());
                                isChecked = false;
                            }else {
                                userId.remove(position);
                                isChecked = true;
                            }
                        }
                    });
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUserData() {
        db.child(user.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            User model = snapshot.getValue(User.class);
                            nameTxt.setText(model.getFullname());
                            if (mActivity != null) {
                                Glide.with(mActivity)
                                        .load(model.getImageUrl())
                                        .into(profileImg);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
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
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(100);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
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

        Log.d("lat",""+ currentLat);
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("lat",currentLat);
        hashMap.put("lng",currentLng);
        db.child(user.getUid()).updateChildren(hashMap);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (client != null) {
            client.disconnect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (client != null) {
            client.disconnect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (client != null) {
            client.disconnect();
        }
    }
}
