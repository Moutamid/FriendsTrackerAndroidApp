package com.moutamid.friendsmeetingtracker.Fragments;

import android.os.Bundle;
import android.text.TextUtils;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.friendsmeetingtracker.Adapters.UserListAdapter;
import com.moutamid.friendsmeetingtracker.Constants.Constants;
import com.moutamid.friendsmeetingtracker.Constants.ItemClickListener;
import com.moutamid.friendsmeetingtracker.Model.Room;
import com.moutamid.friendsmeetingtracker.Model.User;
import com.moutamid.friendsmeetingtracker.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeScreen extends Fragment {

    private TextView nameTxt,createBtn;
    private CircleImageView profileImg;
    private FirebaseUser user;
    private DatabaseReference db,roomDB;
    private RecyclerView recyclerView;
    private UserListAdapter adapter;
    private ArrayList<User> userArrayList;
    private boolean isCreated = false;
    private ArrayList<String> userId = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_hom_screen,container,false);
        nameTxt = root.findViewById(R.id.name);
        profileImg = root.findViewById(R.id.profile);
        recyclerView = root.findViewById(R.id.recyclerView);
        createBtn = root.findViewById(R.id.create);
        user = Constants.auth().getCurrentUser();
        db = Constants.databaseReference().child("Users");
        roomDB = Constants.databaseReference().child("Rooms");
        userArrayList = new ArrayList<>();
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (createBtn.getText().equals("Create Meeting Room")) {
                    isCreated = true;
                    getUserList();
                    createBtn.setText("Next");
                }else if (createBtn.getText().equals("Next")){
                    //isCreated = false;
                    //getUserList();
                    //createBtn.setText("Create Meeting Room");
                    showMeetingRoom();
                }
            }
        });
        getUserData();
        getUserList();

        return root;
    }

    private void showMeetingRoom() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View add_view = inflater.inflate(R.layout.banned_alert_dialog_screen,null);
        EditText roomNameTxt = add_view.findViewById(R.id.name);
        AppCompatButton addBtn = add_view.findViewById(R.id.ok);
        AppCompatButton cancelBtn = add_view.findViewById(R.id.cancel);
        builder.setView(add_view);
        AlertDialog alertDialog = builder.create();
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String room = roomNameTxt.getText().toString();
                if (!TextUtils.isEmpty(room)) {
                    saveRoom(room);
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

    private void saveRoom(String room) {
        String key = roomDB.child(user.getUid()).push().getKey();

        Room model = new Room(key,room,userId);
        roomDB.child(user.getUid()).child(key).setValue(model);
    }

    private void getUserList() {
        db.addValueEventListener(new ValueEventListener() {
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
                            Glide.with(getActivity())
                                    .load(model.getImageUrl())
                                    .into(profileImg);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}
