package com.example.friendsmeetingtracker.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.example.friendsmeetingtracker.Adapters.RoomListAdapter;
import com.example.friendsmeetingtracker.Constants.Constants;
import com.example.friendsmeetingtracker.Model.Room;
import com.example.friendsmeetingtracker.R;

import java.util.ArrayList;

public class MeetingRooms extends Fragment {

    private FirebaseUser user;
    private DatabaseReference db;
    private RecyclerView recyclerView;
    private RoomListAdapter adapter;
    private ArrayList<Room> roomArrayList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_room_screen,container,false);
        if (isAdded()) {
            recyclerView = root.findViewById(R.id.recyclerView);
            user = Constants.auth().getCurrentUser();
            db = Constants.databaseReference().child("Rooms");
            roomArrayList = new ArrayList<>();
            getRooms();
        }
        //checkRooms();
        return root;
    }

    private void checkRooms() {
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    roomArrayList.clear();
                    for (DataSnapshot ds: snapshot.getChildren()){
                        String key = ds.getKey().toString();
                        db.child(key).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                        if (snapshot1.exists()){
                                            for (DataSnapshot dataSnapshot : snapshot1.getChildren()){
                                                Room model = dataSnapshot.getValue(Room.class);
                                                for (int i = 0; i < model.getUsers().size(); i++){
                                                    String users = model.getUsers().get(i);
                                                    if (users.equals(user.getUid())){
                                                        roomArrayList.add(model);
                                                    }
                                                }
                                                if (key.equals(user.getUid())){
                                                    roomArrayList.add(model);
                                                }

                                                adapter = new RoomListAdapter(getActivity(),roomArrayList);
                                                recyclerView.setAdapter(adapter);
                                                adapter.notifyDataSetChanged();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getRooms() {
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    for (DataSnapshot ds : snapshot.getChildren()){
                        Room model = ds.getValue(Room.class);
                        for (int i = 0; i < model.getUsers().size(); i++){
                            String users = model.getUsers().get(i);
                            if (users.equals(user.getUid())){
                                roomArrayList.add(model);
                            }
                        }
                    }
                    adapter = new RoomListAdapter(getActivity(),roomArrayList);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
