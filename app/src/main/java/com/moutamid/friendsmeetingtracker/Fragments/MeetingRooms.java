package com.moutamid.friendsmeetingtracker.Fragments;

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
import com.moutamid.friendsmeetingtracker.Adapters.RoomListAdapter;
import com.moutamid.friendsmeetingtracker.Adapters.UserListAdapter;
import com.moutamid.friendsmeetingtracker.Constants.Constants;
import com.moutamid.friendsmeetingtracker.Constants.ItemClickListener;
import com.moutamid.friendsmeetingtracker.Model.Room;
import com.moutamid.friendsmeetingtracker.Model.User;
import com.moutamid.friendsmeetingtracker.R;

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
        recyclerView = root.findViewById(R.id.recyclerView);
        user = Constants.auth().getCurrentUser();
        db = Constants.databaseReference().child("Rooms");
        roomArrayList = new ArrayList<>();
        getRooms();
        return root;
    }

    private void getRooms() {
        db.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    roomArrayList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Room model = ds.getValue(Room.class);
                        roomArrayList.add(model);
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